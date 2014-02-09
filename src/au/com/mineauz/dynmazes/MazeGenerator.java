package au.com.mineauz.dynmazes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;


public abstract class MazeGenerator<T extends INode>
{
	protected Random mRand;

	public MazeGenerator()
	{
		super();
	}

	public void generate()
	{
		prepareArea();
		
		T end = null;
	
		// Find a start point
		findExit();
		end = findExit();
		
		HashSet<T> visited = new HashSet<T>();
		Stack<T> next = new Stack<T>();
		
		next.push(end);
		
		while(!next.isEmpty())
		{
			T node = next.peek();
			visited.add(node);
			
			@SuppressWarnings( "unchecked" )
			ArrayList<T> neighbours = new ArrayList<T>(Arrays.asList((T[])node.getNeighbours()));
			boolean added = false;
			
			while(!neighbours.isEmpty())
			{
				int index = mRand.nextInt(neighbours.size());
				T neighbour = neighbours.get(index);
				if(visited.contains(neighbour))
					neighbours.remove(index);
				else
				{
					node.addChild(neighbour);
					next.push(neighbour);
					
					added = true;
					break;
				}
			}
			
			if(!added)
				next.pop();
		}
		
		System.out.println("Generation finished");
		
		for(T node : visited)
			placeNode(node);
	}
	
	protected abstract T findExit();

	protected abstract void clearBetween( T nodeA, T nodeB );

	protected abstract void placeNode( T node );

	protected abstract void prepareArea();

}