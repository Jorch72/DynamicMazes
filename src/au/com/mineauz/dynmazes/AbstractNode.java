package au.com.mineauz.dynmazes;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractNode implements INode
{
	private INode mParent;
	private HashSet<INode> mChildren;
	
	public AbstractNode()
	{
		mChildren = new HashSet<INode>();
	}
	
	@Override
	public void addChild( INode node )
	{
		node.setParent(this);
		mChildren.add(node);
	}

	@Override
	public INode getParent()
	{
		return mParent;
	}

	@Override
	public void setParent( INode node )
	{
		mParent = node;
	}

	@Override
	public Set<INode> getChildren()
	{
		return mChildren;
	}
	
	@Override
	public int getDepth()
	{
		INode node = this;
		int count = -1;
		
		while(node != null)
		{
			node = node.getParent();
			++count;
		}
		
		return count;
	}
}
