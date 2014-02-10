package au.com.mineauz.dynmazes;

import java.util.Collection;
import au.com.mineauz.dynmazes.algorithm.Algorithm;
import au.com.mineauz.dynmazes.algorithm.PrimsAlgorithm;


public abstract class MazeGenerator<T extends INode>
{
	private Algorithm mAlgorithm;
	
	public MazeGenerator()
	{
		super();
		mAlgorithm = new PrimsAlgorithm(-1);
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