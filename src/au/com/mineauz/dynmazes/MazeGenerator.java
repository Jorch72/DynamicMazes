package au.com.mineauz.dynmazes;

import java.util.Collection;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.BlockVector;

import au.com.mineauz.dynmazes.algorithm.Algorithm;
import au.com.mineauz.dynmazes.algorithm.DepthFirstAlgorithm;


public abstract class MazeGenerator<T extends INode>
{
	private Algorithm mAlgorithm;
	private String mName;
	
	private World mWorld;
	private UUID mWorldId;
	
	private BlockVector mMin;
	private BlockVector mMax;
	
	private Collection<T> mAllNodes;
	
	private boolean mIsGenerating = false;
	private boolean mIsDrawing = false;
	
	public MazeGenerator(String name, Location min, Location max)
	{
		mAlgorithm = new DepthFirstAlgorithm(-1);
		
		mName = name;
		
		mWorld = min.getWorld();
		mWorldId = mWorld.getUID();
		
		mMin = min.toVector().toBlockVector();
		mMax = max.toVector().toBlockVector();
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
		if(mAllNodes == null)
			return;
		
		// push to drawing task
	}
	
	public void generate(long seed)
	{
		Validate.notNull(mAlgorithm);
		
		// Launch the thread here
		// In the thread:
		//  generate the node mesh
		//  find start location
		//  run algorithm
		//  run post generation stuff
		//  push to drawing task
	}


	@SuppressWarnings( "unchecked" )
	public void generate()
	{
		prepareArea();
		
		T root = findStart();
		Collection<INode> allNodes = mAlgorithm.generate(root);
		
		System.out.println("Generation finished");

		onGenerateComplete(root, (Collection<T>)allNodes);
		
		for(INode node : allNodes)
			placeNode((T)node);
		
		System.out.println("Placement finished");
	}
	
	protected abstract void prepareArea();
	
	protected abstract T findStart();

	protected void onGenerateComplete(T root, Collection<T> nodes) {};
	
	protected abstract void placeNode( T node );

	
	
	
	protected int getDepth(INode node)
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