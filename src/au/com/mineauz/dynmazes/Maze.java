package au.com.mineauz.dynmazes;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BlockVector;

import au.com.mineauz.dynmazes.algorithm.Algorithm;
import au.com.mineauz.dynmazes.algorithm.GrowingTreeAlgorithm;
import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.styles.StoredBlock;


public abstract class Maze<T extends INode>
{
	private Algorithm mAlgorithm;
	private String mName;
	
	private String mType;
	
	private World mWorld;
	private UUID mWorldId;
	
	private BlockVector mMin;
	private BlockVector mMax;
	
	protected Collection<T> allNodes;
	
	private boolean mIsGenerating = false;
	private boolean mIsDrawing = false;
	
	public Maze(String name, String type, World world)
	{
		mType = type;
		
		mAlgorithm = new GrowingTreeAlgorithm();
		((GrowingTreeAlgorithm)mAlgorithm).setRandomChance(0.5);
		
		mName = name;
		
		mWorld = world;
		mWorldId = mWorld.getUID();
	}
	
	protected Maze(String type)
	{
		mType = type;
	}
	
	protected void setBounds(BlockVector min, BlockVector max)
	{
		mMin = min;
		mMax = max;
	}
	
	public String getName()
	{
		return mName;
	}
	
	public Algorithm getAlgorithm()
	{
		return mAlgorithm;
	}
	
	public void setAlgorithm(Algorithm algorithm)
	{
		mAlgorithm = algorithm;
	}

	public void prepareArea(final StoredBlock baseType, final Callback callback)
	{
		Validate.notNull(getWorld());
		
		final BlockVector min = mMin.clone();
		min.setY(min.getBlockY() - 1);
		
		new ClearingTask(mMin, mMax, getWorld(), new Callback()
		{
			@Override
			public void onFailure( Throwable exception )
			{
				if(callback != null)
					callback.onFailure(exception);
				else
					throw new RuntimeException(exception);
			}
			
			@Override
			public void onComplete()
			{
				BlockVector max = mMax.clone();
				max.setY(min.getY()+1);
				new FillTask<INode>(min, max, getWorld(), baseType, new Callback()
				{
					@Override
					public void onFailure( Throwable exception )
					{
						if(callback != null)
							callback.onFailure(exception);
						else
							throw new RuntimeException(exception);
					}
					
					@Override
					public void onComplete()
					{
						if(callback != null)
							callback.onComplete();
					}
				}).start();
			}
		}).start(); 
	}
	
	public void draw(final Callback callback)
	{
		Validate.notNull(getWorld());
		Validate.isTrue(!mIsDrawing);
		
		if(allNodes == null)
		{
			if(callback != null)
				callback.onFailure(new IllegalStateException("Maze not generated"));
			
			return;
		}
		
		mIsDrawing = true;
		
		new DrawingTask<T>(this, allNodes, new Callback()
		{
			@Override
			public void onFailure( Throwable exception )
			{
				mIsDrawing = false;
				
				if(callback != null)
					callback.onFailure(exception);
				else
					throw new RuntimeException(exception);
			}
			
			@Override
			public void onComplete()
			{
				mIsDrawing = false;

				if(callback != null)
					callback.onComplete();
			}
		}).start();
	}
	
	public void clear(final Callback callback)
	{
		Validate.isTrue(!mIsDrawing);
		Validate.notNull(getWorld());
		
		mIsDrawing = true;
		
		BlockVector min = mMin.clone();
		min.setY(min.getBlockY() - 1);
		new ClearingTask(min, mMax, getWorld(), new Callback()
		{
			@Override
			public void onFailure( Throwable exception )
			{
				if(callback != null)
					callback.onFailure(exception);
				else
					throw new RuntimeException(exception);
			}
			
			@Override
			public void onComplete()
			{
				mIsDrawing = false;
				if(callback != null)
					callback.onComplete();
			}
		}).start();
	}
	
	void setDrawComplete()
	{
		mIsDrawing = false;
	}
	
	public void generate(long seed, Callback callback)
	{
		Validate.notNull(mAlgorithm);
		Validate.isTrue(!mIsGenerating);
		
		mIsGenerating = true;
		GenerationThread<T> thread = new GenerationThread<T>(this, callback);
		thread.start();
	}
	
	void setGenerationComplete()
	{
		mIsGenerating = false;
	}
	
	public boolean isGenerating()
	{
		return mIsGenerating;
	}
	
	public boolean isDrawing()
	{
		return mIsDrawing;
	}
	
	public World getWorld()
	{
		if(mWorld != null)
			return mWorld;
		
		mWorld = Bukkit.getWorld(mWorldId);
		return mWorld;
	}
	
	public BlockVector getMinCorner()
	{
		return mMin;
	}
	
	public BlockVector getMaxCorner()
	{
		return mMax;
	}

	protected abstract void buildNodes();
	
	protected abstract void processMaze(T root); 
	
	protected abstract T findStart();

	protected abstract void placeNode( T node, List<StoredBlock> blocks );

	
	public final void save(File file)
	{
		try
		{
			YamlConfiguration output = new YamlConfiguration();

			save(output);
			
			output.save(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	protected void save(ConfigurationSection root)
	{
		root.set("name", mName);
		root.set("type", mType);
		root.set("world", mWorldId.toString());
		if(mAlgorithm != null)
		{
			ConfigurationSection algo = root.createSection("algorithm");
			algo.set("type", mAlgorithm.getType());
			mAlgorithm.save(algo);
		}
		
		root.set("min", mMin);
		root.set("max", mMax);
	}
	
	public static Maze<?> read(File file)
	{
		try
		{
			YamlConfiguration input = new YamlConfiguration();
			input.load(file);
			
			Maze<?> maze = MazeManager.createEmptyMaze(input.getString("type"));
			
			maze.read(input);
			
			return maze;
		}
		catch(IOException e)
		{
			DynamicMazePlugin.getInstance().getLogger().severe("Failed to load maze " + file.getName() + ". IO Error:");
			e.printStackTrace();
			return null;
		}
		catch(InvalidConfigurationException e)
		{
			DynamicMazePlugin.getInstance().getLogger().severe("Failed to load maze " + file.getName() + ". Error in file:");
			DynamicMazePlugin.getInstance().getLogger().severe(e.getMessage());
			return null;
		}
		catch(IllegalArgumentException e)
		{
			DynamicMazePlugin.getInstance().getLogger().severe("Failed to load maze " + file.getName() + ". Unknown maze type");
			return null;
		}
	}
	
	protected void read(ConfigurationSection section) throws InvalidConfigurationException
	{
		mName = section.getString("name");
		mWorldId = UUID.fromString(section.getString("world"));
		
		if(section.isConfigurationSection("algorithm"))
		{
			ConfigurationSection algo = section.getConfigurationSection("algorithm");
			mAlgorithm = MazeManager.getAlgorithm(algo.getString("type"));
			if(mAlgorithm == null)
				DynamicMazePlugin.getInstance().getLogger().warning("Unknown algorithm type " + algo.getString("type") + " in maze " + mName);
			else
				mAlgorithm.read(algo);
		}
		
		mMin = section.getVector("min").toBlockVector();
		mMax = section.getVector("max").toBlockVector();
	}
	
	protected static int getDepth(INode node)
	{
		int depth = -1;
		while(node != null)
		{
			++depth;
			node = node.getParent();
		}
		
		return depth;
	}
}