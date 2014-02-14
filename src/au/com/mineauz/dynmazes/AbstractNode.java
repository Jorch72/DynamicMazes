package au.com.mineauz.dynmazes;

import java.util.LinkedList;
import java.util.List;

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
