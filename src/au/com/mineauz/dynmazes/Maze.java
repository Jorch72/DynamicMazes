package au.com.mineauz.dynmazes;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import au.com.mineauz.dynmazes.flags.AlgorithmFlag;
import au.com.mineauz.dynmazes.flags.Flag;
import au.com.mineauz.dynmazes.flags.FlagIO;
import au.com.mineauz.dynmazes.misc.BlockLocation;
import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.styles.StoredBlock;


public abstract class Maze<T extends INode>
{
	private String mName;
	
	private String mType;
	
	private World mWorld;
	private UUID mWorldId;
	
	private BlockVector mMin;
	private BlockVector mMax;
	
	protected Collection<T> allNodes;
	
	private boolean mIsGenerating = false;
	private boolean mIsDrawing = false;
	
	private AlgorithmFlag mAlgorithm = new AlgorithmFlag();
	
	private HashMap<String, Flag<?>> mFlags;
	
	public Maze(String name, String type, World world)
	{
		mType = type;
		
		mFlags = new HashMap<String, Flag<?>>();
		mAlgorithm.setValue(new GrowingTreeAlgorithm());
		((GrowingTreeAlgorithm)mAlgorithm.getValue()).setRandomChance(0.5);
		mFlags.put("algorithm", mAlgorithm);
		
		mName = name;
		
		mWorld = world;
		mWorldId = mWorld.getUID();
	}
	
	protected Maze(String type)
	{
		mType = type;
		mFlags = new HashMap<String, Flag<?>>();
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
	
	public String getType()
	{
		return mType;
	}
	
	public Algorithm getAlgorithm()
	{
		return mAlgorithm.getValue();
	}
	
	public void setAlgorithm(Algorithm algorithm)
	{
		mAlgorithm.setValue(algorithm);
	}

	public void prepareArea(final StoredBlock baseType, final Callback callback)
	{
		Validate.notNull(getWorld());
		Validate.isTrue(!mIsDrawing);
		
		final BlockVector min = mMin.clone();
		min.setY(min.getBlockY() - 1);
		
		mIsDrawing = true;
		
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
						mIsDrawing = false;
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
		
		clear(false, new Callback()
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
				new DrawingTask<T>(Maze.this, allNodes, mMin.getBlockY(), new Callback()
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
		});
	}
	
	public void clear(boolean includeBase, final Callback callback)
	{
		Validate.isTrue(!mIsDrawing);
		Validate.notNull(getWorld());
		
		mIsDrawing = true;
		
		BlockVector min = mMin.clone();
		if(includeBase)
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
	
	public void generate(long seed, Callback callback)
	{
		Validate.notNull(mAlgorithm);
		Validate.isTrue(!mIsGenerating);
		Validate.isTrue(!mIsDrawing);
		
		mIsGenerating = true;
		if(seed != -1)
			mAlgorithm.getValue().setSeed(seed);
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
		
		root.set("min", mMin);
		root.set("max", mMax);
		
		FlagIO.saveFlags(mFlags, root.createSection("flags"));
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
		
		mMin = section.getVector("min").toBlockVector();
		mMax = section.getVector("max").toBlockVector();
		
		if(section.isConfigurationSection("flags"))
		{
			mFlags.clear();
			mFlags.putAll(FlagIO.loadFlags(section.getConfigurationSection("flags")));
			mAlgorithm = (AlgorithmFlag)getFlag("algorithm");
		}
	}
	
	public Map<String, Flag<?>> getFlags()
	{
		if(mAlgorithm.getValue() != null)
			return Collections.unmodifiableMap(Util.union(mFlags, mAlgorithm.getValue().getFlags()));
		return Collections.unmodifiableMap(mFlags);
	}
	
	public void addFlag(String name, Flag<?> flag)
	{
		if(mFlags.containsKey(name.toLowerCase()))
			throw new IllegalArgumentException("Duplicate flag " + name);
		mFlags.put(name.toLowerCase(), flag);
	}
	
	public Flag<?> getFlag(String name)
	{
		return getFlags().get(name.toLowerCase());
	}
	
	public boolean hasFlag(String name)
	{
		return getFlags().containsKey(name.toLowerCase());
	}
	
	public <Type> void onFlagChanged(String name, Flag<Type> flag, Type oldValue)
	{
		
	}
	
	public abstract BlockLocation getStartPoint();
	public abstract BlockLocation getEndPoint();
}