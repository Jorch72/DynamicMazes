package au.com.mineauz.dynmazes.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Random;

import au.com.mineauz.dynmazes.INode;

public class PrimsAlgorithm implements Algorithm
{
	private Random mRand;
	
	public PrimsAlgorithm(long seed)
	{
		mRand = (seed == -1 ? new Random() : new Random(seed));
	}
	
	@Override
	public Collection<INode> generate( INode from )
	{
		ArrayList<INode> waiting = new ArrayList<INode>();
		HashSet<INode> visited = new HashSet<INode>();
		waiting.add(from);
		
		while(!waiting.isEmpty())
		{
			int id = mRand.nextInt(waiting.size());
			INode next = waiting.get(id);
			visited.add(next);
			
			ArrayList<INode> neighbours = new ArrayList<INode>(Arrays.asList(next.getNeighbours()));
		
			while(!neighbours.isEmpty())
			{
				int index = mRand.nextInt(neighbours.size());
				INode neighbour = neighbours.get(index);
				if(visited.contains(neighbour))
					neighbours.remove(index);
				else
				{
					next.addChild(neighbour);
					waiting.add(neighbour);
					visited.add(neighbour);
					
					break;
				}
			}
			
			if(neighbours.isEmpty())
				waiting.remove(id);
		}
		
		return visited;
	}

	@Override
	public Random getRandom()
	{
		return mRand;
	}
	
	@Override
	public void setSeed( long seed )
	{
		mRand.setSeed(seed);
	}
}
