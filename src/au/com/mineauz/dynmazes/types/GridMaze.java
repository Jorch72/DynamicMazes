package au.com.mineauz.dynmazes.types;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import au.com.mineauz.dynmazes.INode;
import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.algorithm.PrimsAlgorithm;

// TODO: This whole class is to be redone
public class GridMaze extends Maze
{
	private Location mMinCorner;
	private int mWidth;
	private int mLength;
	
	private Random mRand;
	
	public GridMaze(String name, Location loc, int width, int length, int wallWidth, int wallHeight, int pathSize)
	{
		super(name, loc, loc.clone().add(width * (pathSize + wallWidth), 4, length * (pathSize + wallWidth)));
		mMinCorner = loc;
		mWidth = width;
		mLength = length;
		
		mRand = new Random();
	}
	
//	@Override
//	protected void prepareArea()
//	{
//		for(int x = mMinCorner.getBlockX(); x <= mMinCorner.getBlockX() + mWidth * 2; ++x)
//		{
//			for(int z = mMinCorner.getBlockZ(); z <= mMinCorner.getBlockZ() + mLength * 2; ++z)
//			{
//				for(int y = mMinCorner.getBlockY(); y < mMinCorner.getBlockY() + 4; ++y)
//				{
//					Block block = mMinCorner.getWorld().getBlockAt(x, y, z);
//					if(y == mMinCorner.getBlockY())
//						block.setType(Material.STONE);
//					else if(y == mMinCorner.getBlockY() + 1)
//						block.setType(Material.GRASS);
//					else
//						block.setTypeIdAndData(Material.LEAVES.getId(), (byte)4, false);
//				}
//			}
//				
//		}
//	}
	
	@Override
	protected void placeNode(INode node)
	{
		Location loc = node.toLocation();
		loc.getBlock().getRelative(BlockFace.UP, 1).setType(Material.GRAVEL);
		for(int i = 0; i < 3; ++i)
			loc.getBlock().getRelative(BlockFace.UP, 2 + i).setType(Material.AIR);
	}
	
	protected void clearBetween(INode nodeA, INode nodeB)
	{
		Location a = nodeA.toLocation();
		Location b = nodeB.toLocation();
		
		Location loc = a.clone().add(b).multiply(0.5);
		loc.getBlock().getRelative(BlockFace.UP, 1).setType(Material.GRAVEL);
		for(int i = 0; i < 3; ++i)
			loc.getBlock().getRelative(BlockFace.UP, 2 + i).setType(Material.AIR);
	}
	
	@Override
	protected INode findStart()
	{
		int side = mRand.nextInt(4);
		INode node = null;
		Block b = null;
		switch(side)
		{
		case 0:
			node = new GridNode(0, mRand.nextInt(mLength));
			b = node.toLocation().getBlock().getRelative(BlockFace.WEST);
			break;
		case 1:
			node = new GridNode(mWidth - 1, mRand.nextInt(mLength));
			b = node.toLocation().getBlock().getRelative(BlockFace.EAST);
			break;
		case 2:
			node = new GridNode(mRand.nextInt(mWidth), 0);
			b = node.toLocation().getBlock().getRelative(BlockFace.NORTH);
			break;
		case 3:
			node = new GridNode(mRand.nextInt(mWidth), mLength - 1);
			b = node.toLocation().getBlock().getRelative(BlockFace.SOUTH);
			break;
		}
		
		b.getRelative(BlockFace.UP, 1).setType(Material.GRAVEL);
		for(int i = 0; i < 3; ++i)
			b.getRelative(BlockFace.UP, 2 + i).setType(Material.AIR);
		
		return node;
	}
	
	
	
	private class GridNode implements INode
	{
		private int mX;
		private int mY;
		
		private INode mParent;
		private HashSet<INode> mChildren;
		
		public GridNode(int x, int y)
		{
			mX = x;
			mY = y;
			
			mChildren = new HashSet<INode>();
		}
		
		@Override
		public INode[] getNeighbours()
		{
			if(mX == 0)
			{
				if(mY == 0)
					return new INode[] {new GridNode(mX + 1, mY), new GridNode(mX, mY + 1)};
				if(mY == mLength - 1)
					return new INode[] {new GridNode(mX + 1, mY), new GridNode(mX, mY - 1)};
				
				return new INode[] {new GridNode(mX + 1, mY), new GridNode(mX, mY - 1), new GridNode(mX, mY + 1)};
			}
			else if(mX == mWidth - 1)
			{
				if(mY == 0)
					return new INode[] {new GridNode(mX - 1, mY), new GridNode(mX, mY + 1)};
				if(mY == mLength - 1)
					return new INode[] {new GridNode(mX - 1, mY), new GridNode(mX, mY - 1)};
				
				return new INode[] {new GridNode(mX - 1, mY), new GridNode(mX, mY - 1), new GridNode(mX, mY + 1)};
			}
			else
			{
				if(mY == 0)
					return new INode[] {new GridNode(mX + 1, mY), new GridNode(mX - 1, mY), new GridNode(mX, mY + 1)};
				if(mY == mLength - 1)
					return new INode[] {new GridNode(mX + 1, mY), new GridNode(mX - 1, mY), new GridNode(mX, mY - 1)};
				
				return new INode[] {new GridNode(mX + 1, mY), new GridNode(mX - 1, mY), new GridNode(mX, mY - 1), new GridNode(mX, mY + 1)};
			}
		}
		
		@Override
		public Location toLocation()
		{
			return mMinCorner.clone().add(1 + mX * 2, 0, 1 + mY * 2);
		}
		
		@Override
		public int hashCode()
		{
			return mX | mY << 16;
		}
		
		@Override
		public boolean equals( Object obj )
		{
			if(!(obj instanceof GridNode))
				return false;
			
			GridNode other = (GridNode)obj;
			
			return mX == other.mX && mY == other.mY;
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
		
		
	}



	@Override
	protected void buildNodes()
	{
	}

	@Override
	protected void processMaze( INode root )
	{
	}
}
