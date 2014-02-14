package au.com.mineauz.dynmazes.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import org.bukkit.configuration.ConfigurationSection;

import au.com.mineauz.dynmazes.INode;
import au.com.mineauz.dynmazes.flags.Flag;

public class DepthFirstAlgorithm implements Algorithm
{
	private Random mRand;
	
	public DepthFirstAlgorithm()
	{
		mRand = new Random();
	}
	
	@Override
	public Collection<INode> generate( INode from )
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
	
	@Override
	public String getType()
	{
		return "DepthFirst";
	}
	
	@Override
	public void read( ConfigurationSection section )
	{
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
	}
	
	@Override
	public Map<String, Flag<?>> getFlags()
	{
		return Collections.emptyMap();
	}
}
