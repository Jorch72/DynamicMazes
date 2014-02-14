package au.com.mineauz.dynmazes;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractNode implements INode
{
	private HashSet<INode> mParents;
	private HashSet<INode> mChildren;
	
	public AbstractNode()
	{
		mChildren = new HashSet<INode>();
		mParents = new HashSet<INode>();
	}
	
	@Override
	public void addChild( INode node )
	{
		node.addParent(this);
		mChildren.add(node);
	}

	@Override
	public Set<INode> getParents()
	{
		return mParents;
	}

	@Override
	public void addParent( INode node )
	{
		mParents.add(node);
	}

	@Override
	public Set<INode> getChildren()
	{
		return mChildren;
	}
	
	@Override
	public int getDepth()
	{
		if(mParents.isEmpty())
			return 0;
		
		int count = Integer.MAX_VALUE;
		for(INode parent : mParents)
		{
			int dist = parent.getDepth();
			if(dist < count)
				count = dist;
		}
		
		return count + 1;
	}
}
