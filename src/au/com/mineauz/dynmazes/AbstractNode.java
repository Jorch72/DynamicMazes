package au.com.mineauz.dynmazes;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

public abstract class AbstractNode implements INode
{
	private LinkedList<INode> mParents;
	private LinkedList<INode> mChildren;
	
	public AbstractNode()
	{
		mChildren = new LinkedList<INode>();
		mParents = new LinkedList<INode>();
	}
	
	@Override
	public void addChild( INode node )
	{
		node.addParent(this);
		mChildren.add(node);
	}

	@Override
	public List<INode> getParents()
	{
		return mParents;
	}

	@Override
	public void addParent( INode node )
	{
		mParents.add(node);
	}

	@Override
	public List<INode> getChildren()
	{
		return mChildren;
	}
	
	@Override
	public int getDepth()
	{
		if(mParents.isEmpty())
			return 0;
		
		ArrayDeque<Entry<INode, Integer>> nodes = new ArrayDeque<Entry<INode, Integer>>();
		HashSet<INode> visited = new HashSet<INode>();
		nodes.add(new AbstractMap.SimpleEntry<INode, Integer>(this, 0));
		
		while(!nodes.isEmpty())
		{
			Entry<INode, Integer> node = nodes.poll();
			
			if(!visited.add(node.getKey()))
				continue;
			
			if(node.getKey().getParents().isEmpty())
				return node.getValue();
			
			for(INode parent : node.getKey().getParents())
			{
				if(!visited.contains(parent))
					nodes.add(new AbstractMap.SimpleEntry<INode, Integer>(parent, node.getValue() + 1));
			}
		}
		
		return 0;
	}
	
	@Override
	public int getDistance( INode other )
	{
		ArrayDeque<Entry<INode, Integer>> nodes = new ArrayDeque<Entry<INode, Integer>>();
		
		HashMap<INode, Integer> visited = new HashMap<INode, Integer>();
		HashMap<INode, INode> map = new HashMap<INode, INode>();
		
		nodes.add(new AbstractMap.SimpleEntry<INode, Integer>(this, 0));
		visited.put(this, 0);
		map.put(this, this);
		
		nodes.add(new AbstractMap.SimpleEntry<INode, Integer>(other, 0));
		visited.put(other, 0);
		map.put(other, other);
		
		while(!nodes.isEmpty())
		{
			Entry<INode, Integer> node = nodes.poll();
			INode source = map.get(node.getKey());
			
			for(INode parent : node.getKey().getParents())
			{
				if(!visited.containsKey(parent))
				{
					nodes.add(new AbstractMap.SimpleEntry<INode, Integer>(parent, node.getValue() + 1));
					visited.put(parent, node.getValue() + 1);
					map.put(parent, source);
				}
				else
				{
					if(!source.equals(map.get(parent)))
						return visited.get(parent) + node.getValue();
				}
			}
		}
		
		throw new IllegalStateException();
	}
	
	@Override
	public boolean isParentOf( INode node )
	{
		ArrayDeque<INode> nodes = new ArrayDeque<INode>();
		HashSet<INode> visited = new HashSet<INode>();
		nodes.add(node);
		
		while(!nodes.isEmpty())
		{
			INode test = nodes.poll();
			
			if(!visited.add(test))
				continue;
			
			if(test.equals(this))
				return true;
			
			for(INode parentNode : test.getParents())
			{
				if(!visited.contains(parentNode))
					nodes.add(parentNode);
			}
		}
		
		return false;
	}
}
