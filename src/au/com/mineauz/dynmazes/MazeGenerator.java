package au.com.mineauz.dynmazes;

import java.util.Collection;
import au.com.mineauz.dynmazes.algorithm.Algorithm;
import au.com.mineauz.dynmazes.algorithm.DepthFirstAlgorithm;
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
		
		T end = findExit();
		T start = findExit();
		
		Collection<INode> allNodes = mAlgorithm.generate(start, end);
		
		System.out.println("Generation finished");
		
		for(INode node : allNodes)
			placeNode((T)node);
		
		System.out.println("Placement finished");
	}
	
	protected abstract T findExit();

	protected abstract void clearBetween( T nodeA, T nodeB );

	protected abstract void placeNode( T node );

	protected abstract void prepareArea();

}