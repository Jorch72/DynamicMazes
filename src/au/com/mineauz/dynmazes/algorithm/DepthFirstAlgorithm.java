package au.com.mineauz.dynmazes.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;

import au.com.mineauz.dynmazes.INode;

public class DepthFirstAlgorithm implements Algorithm
{
	private Random mRand;
	
	public DepthFirstAlgorithm(long seed)
	{
		mRand = (seed == -1 ? new Random() : new Random(seed));
	}
	
	@Override
	public Collection<INode> generate( INode from, INode to )
	{
		HashSet<INode> visited = new HashSet<INode>();
		Stack<INode> next = new Stack<INode>();
		
		next.push(from);
		
		while(!next.isEmpty())
		{
			INode node = next.peek();
			visited.add(node);
			
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
					break;
				}
			}
			
			if(!added)
				next.pop();
		}
		
		return visited;
	}
}
