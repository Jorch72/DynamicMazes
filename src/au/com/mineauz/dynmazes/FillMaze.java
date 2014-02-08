package au.com.mineauz.dynmazes;

import java.util.Set;

import org.bukkit.Location;

public class FillMaze extends MazeGenerator
{

	@Override
	protected INode findExit()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void clearBetween( INode nodeA, INode nodeB )
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void clearNode( INode node )
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void clearSpace()
	{
		// TODO Auto-generated method stub

	}

	private class Node implements INode
	{
		@Override
		public Location toLocation()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public INode[] getNeighbours()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public INode getParent()
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setParent( INode node )
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addChild( INode node )
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public Set<INode> getChildren()
		{
			// TODO Auto-generated method stub
			return null;
		}
		
	}
}
