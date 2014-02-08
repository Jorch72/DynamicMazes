package au.com.mineauz.dynmazes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;


public abstract class MazeGenerator
{
	protected Random mRand;

	public MazeGenerator()
	{
		super();
	}

	public void generate()
	{
		clearSpace();
		
		INode end = null;
	
		// Find a start point
		findExit();
		end = findExit();
		
		HashSet<INode> visited = new HashSet<INode>();
		Stack<INode> next = new Stack<INode>();
		
		next.push(end);
		
		while(!next.isEmpty())
		{
			INode node = next.peek();
			visited.add(node);
			clearNode(node);
			
			ArrayList<INode> neighbours = new ArrayList<INode>(Arrays.asList(node.getNeighbours()));
			boolean added = false;
			
			while(!neighbours.isEmpty())
			{
				int index = mRand.nextInt(neighbours.size());
				INode neighbour = neighbours.get(index);
				if(visited.contains(neighbour))
					neighbours.remove(index);
				else
				{
					node.addChild(neighbour);
					next.push(neighbour);
					
					added = true;
					
					clearBetween(node, neighbour);
					break;
				}
			}
			
			if(!added)
				next.pop();
		}
	}
	
	protected abstract INode findExit();

	protected abstract void clearBetween( INode nodeA, INode nodeB );

	protected abstract void clearNode( INode node );

	protected abstract void clearSpace();

}