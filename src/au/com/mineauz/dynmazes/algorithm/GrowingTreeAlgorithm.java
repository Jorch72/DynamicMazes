package au.com.mineauz.dynmazes.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;

import au.com.mineauz.dynmazes.INode;

public class GrowingTreeAlgorithm implements Algorithm
{
	private Random mRand;
	
	private double mRandChance = 0.5;
	
	public GrowingTreeAlgorithm()
	{
		mRand = new Random();
	}
	
	private int select(List<INode> nodes)
	{
		if(mRand.nextDouble() < mRandChance)
			return mRand.nextInt(nodes.size());
		else
			return nodes.size()-1;
	}
	
	@Override
	public Collection<INode> generate( INode from )
	{
		ArrayList<INode> waiting = new ArrayList<INode>();
		HashSet<INode> visited = new HashSet<INode>();
		waiting.add(from);
		
		while(!waiting.isEmpty())
		{
			int id = select(waiting);
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
	
	public double getRandomChance()
	{
		return mRandChance;
	}
	
	public void setRandomChance(double chance)
	{
		mRandChance = chance;
	}
	
	@Override
	public String getType()
	{
		return "GrowingTree";
	}
	
	@Override
	public void read( ConfigurationSection section )
	{
		mRandChance = section.getDouble("chance");
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		section.set("chance", mRandChance);
	}
}
