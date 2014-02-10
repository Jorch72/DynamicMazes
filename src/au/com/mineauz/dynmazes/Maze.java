package au.com.mineauz.dynmazes;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.BlockVector;

import au.com.mineauz.dynmazes.algorithm.Algorithm;
import au.com.mineauz.dynmazes.algorithm.DepthFirstAlgorithm;


public abstract class Maze<T extends INode>
{
	private Algorithm mAlgorithm;
	private String mName;
	
	private World mWorld;
	private UUID mWorldId;
	
	private BlockVector mMin;
	private BlockVector mMax;
	
	protected Collection<T> allNodes;
	
	private boolean mIsGenerating = false;
	private boolean mIsDrawing = false;
	
	public Maze(String name, Location min, Location max)
	{
		mAlgorithm = new DepthFirstAlgorithm(-1);
		
		mName = name;
		
		mWorld = min.getWorld();
		mWorldId = mWorld.getUID();
		
		mMin = min.toVector().toBlockVector();
		mMax = max.toVector().toBlockVector();
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

	public void draw()
	{
		Validate.isTrue(!mIsDrawing);
		
		if(allNodes == null)
			return;
		
		mIsDrawing = true;
		
		// push to drawing task
		new DrawingTask<T>(this, allNodes).start();
	}
	
	void setDrawComplete()
	{
		mIsDrawing = false;
	}
	
	public void generate(long seed)
	{
		Validate.notNull(mAlgorithm);
		Validate.isTrue(!mIsGenerating);
		
		mIsGenerating = true;
		GenerationThread<T> thread = new GenerationThread<T>(this);
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

	protected abstract void buildNodes();
	
	protected abstract void processMaze(T root); 
	
	protected abstract T findStart();

	protected abstract void placeNode( T node );

	
	public final void save(File file)
	{
		try
		{
			YamlConfiguration output = new YamlConfiguration();

			output.set("world", mWorldId.toString());
			
			// TODO: Save other info too
			
			output.save(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
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