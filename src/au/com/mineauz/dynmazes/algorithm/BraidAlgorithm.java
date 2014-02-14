package au.com.mineauz.dynmazes.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;

import au.com.mineauz.dynmazes.INode;

public class BraidAlgorithm implements Algorithm
{
	private Random mRand;
	
	private double mRandChance = 0.5;
	private double mDeadEndChance = 0.0;
	
	public BraidAlgorithm()
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
	
	private boolean isDeadEnd(INode node)
	{
		return (node.getChildren().size() + node.getParents().size()) == 1;
	}
	
	private boolean isParentOf(INode parent, INode node)
	{
		if(node.getParents().contains(parent))
			return true;
		
		for(INode parentNode : node.getParents())
		{
			if(isParentOf(parent, parentNode))
				return true;
		}
		
		return false;
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
		
		// Rules for braiding:
		// If not a dead end, do nothing
		// If adjacent to a dead end, punch through to it
		
		// Process the nodes removing dead ends
		for(INode node : visited)
		{
			if(node.equals(from))
				continue;
			
			if(!isDeadEnd(node))
				continue;
			
			if(mRand.nextDouble() < mDeadEndChance)
				continue;
			
			INode parent = node.getParents().get(0);
			INode destination = null;
			INode[] neighbours = node.getNeighbours();
			for(INode neighbour : neighbours)
			{
				if(parent.equals(neighbour))
					continue;
				
				if(isDeadEnd(neighbour))
				{
					destination = neighbour;
					break;
				}
			}
			
			if(destination == null)
			{
				if(neighbours.length > 1)
				{
					while(destination == null || destination.equals(parent))
						destination = neighbours[mRand.nextInt(neighbours.length)];
				}
				else
					destination = neighbours[0];
			}
			
			if(isParentOf(destination, node))
				destination.addChild(node);
			else
				node.addChild(destination);
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
		return "Braid";
	}
	
	@Override
	public void read( ConfigurationSection section )
	{
		mRandChance = section.getDouble("chance");
		mDeadEndChance = section.getDouble("deadEndChance");
	}
	
	@Override
	public void save( ConfigurationSection section )
	{
		section.set("chance", mRandChance);
		section.set("deadEndChance", mDeadEndChance);
	}
}
