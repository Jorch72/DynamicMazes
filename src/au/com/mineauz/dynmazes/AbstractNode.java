package au.com.mineauz.dynmazes;

import java.util.AbstractMap;
import java.util.ArrayDeque;
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
}
