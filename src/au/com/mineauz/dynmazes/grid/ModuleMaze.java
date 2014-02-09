package au.com.mineauz.dynmazes.grid;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import au.com.mineauz.dynmazes.INode;
import au.com.mineauz.dynmazes.MazeGenerator;
import au.com.mineauz.dynmazes.styles.PieceType;
import au.com.mineauz.dynmazes.styles.Style;

public class ModuleMaze extends MazeGenerator<ModuleNode>
{
	Style mStyle;
	Location mMinCorner;
	
	int mWidth;
	int mLength;
	
	private Random mRand;
	
	public ModuleMaze(Style style, Location loc, int width, int length, BlockFace facing)
	{
		mMinCorner = loc;
		mWidth = width;
		mLength = length;
		mStyle = style;
		
		mRand = new Random();
	}
	
	@Override
	protected void prepareArea()
	{
		for(int x = mMinCorner.getBlockX(); x < mMinCorner.getBlockX() + mWidth * mStyle.getPieceSize(); ++x)
		{
			for(int z = mMinCorner.getBlockZ(); z < mMinCorner.getBlockZ() + mLength * mStyle.getPieceSize(); ++z)
			{
				for(int y = mMinCorner.getBlockY() - 1; y < mMinCorner.getBlockY() + mStyle.getHeight(); ++y)
				{
					Block block = mMinCorner.getWorld().getBlockAt(x, y, z);
					if(y == mMinCorner.getBlockY() - 1)
						block.setType(Material.BEDROCK);
					else
						block.setType(Material.AIR);
				}
			}
		}
	}
	
	@Override
	protected void placeNode(ModuleNode node)
	{
		mStyle.getPiece(node.getType()).place(node.toLocation());
	}
	
	@Override
	protected void clearBetween(ModuleNode nodeA, ModuleNode nodeB)
	{
	}
	
	@Override
	protected ModuleNode findExit()
	{
		int side = mRand.nextInt(4);
		ModuleNode node = null;
		Block b = null;
		switch(side)
		{
		case 0:
			node = new ModuleNode(this, 0, mRand.nextInt(mLength));
			b = node.toLocation().getBlock().getRelative(BlockFace.WEST);
			break;
		case 1:
			node = new ModuleNode(this, mWidth - 1, mRand.nextInt(mLength));
			b = node.toLocation().getBlock().getRelative(BlockFace.EAST);
			break;
		case 2:
			node = new ModuleNode(this, mRand.nextInt(mWidth), 0);
			b = node.toLocation().getBlock().getRelative(BlockFace.NORTH);
			break;
		case 3:
			node = new ModuleNode(this, mRand.nextInt(mWidth), mLength - 1);
			b = node.toLocation().getBlock().getRelative(BlockFace.SOUTH);
			break;
		}
		
		b.getRelative(BlockFace.UP, 1).setType(Material.GRAVEL);
		for(int i = 0; i < 3; ++i)
			b.getRelative(BlockFace.UP, 2 + i).setType(Material.AIR);
		
		return node;
	}
}

class ModuleNode implements INode
{
	private static BlockFace[] directions = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
	
	private int mX;
	private int mY;
	
	private INode mParent;
	private HashSet<INode> mChildren;
	
	private ModuleMaze mMaze;
	
	public ModuleNode(ModuleMaze maze, int x, int y)
	{
		mMaze = maze;
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
				return new INode[] {new ModuleNode(mMaze, mX + 1, mY), new ModuleNode(mMaze, mX, mY + 1)};
			if(mY == mMaze.mLength - 1)
				return new INode[] {new ModuleNode(mMaze, mX + 1, mY), new ModuleNode(mMaze, mX, mY - 1)};
			
			return new INode[] {new ModuleNode(mMaze, mX + 1, mY), new ModuleNode(mMaze, mX, mY - 1), new ModuleNode(mMaze, mX, mY + 1)};
		}
		else if(mX == mMaze.mWidth - 1)
		{
			if(mY == 0)
				return new INode[] {new ModuleNode(mMaze, mX - 1, mY), new ModuleNode(mMaze, mX, mY + 1)};
			if(mY == mMaze.mLength - 1)
				return new INode[] {new ModuleNode(mMaze, mX - 1, mY), new ModuleNode(mMaze, mX, mY - 1)};
			
			return new INode[] {new ModuleNode(mMaze, mX - 1, mY), new ModuleNode(mMaze, mX, mY - 1), new ModuleNode(mMaze, mX, mY + 1)};
		}
		else
		{
			if(mY == 0)
				return new INode[] {new ModuleNode(mMaze, mX + 1, mY), new ModuleNode(mMaze, mX - 1, mY), new ModuleNode(mMaze, mX, mY + 1)};
			if(mY == mMaze.mLength - 1)
				return new INode[] {new ModuleNode(mMaze, mX + 1, mY), new ModuleNode(mMaze, mX - 1, mY), new ModuleNode(mMaze, mX, mY - 1)};
			
			return new INode[] {new ModuleNode(mMaze, mX + 1, mY), new ModuleNode(mMaze, mX - 1, mY), new ModuleNode(mMaze, mX, mY - 1), new ModuleNode(mMaze, mX, mY + 1)};
		}
	}
	
	@Override
	public Location toLocation()
	{
		return mMaze.mMinCorner.clone().add(mX * mMaze.mStyle.getPieceSize(), 0, mY * mMaze.mStyle.getPieceSize());
	}
	
	@Override
	public int hashCode()
	{
		return mX | mY << 16;
	}
	
	@Override
	public boolean equals( Object obj )
	{
		if(!(obj instanceof ModuleNode))
			return false;
		
		ModuleNode other = (ModuleNode)obj;
		
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
	
	private BlockFace toNode(INode node)
	{
		int xx = ((ModuleNode)node).mX - mX;
		int yy = ((ModuleNode)node).mY - mY;
		
		for(BlockFace dir : directions)
		{
			if(dir.getModX() == xx && dir.getModZ() == yy)
				return dir;
		}
		
		return BlockFace.SELF;
		
	}
	
	public PieceType getType()
	{
		HashSet<BlockFace> others = new HashSet<BlockFace>(mChildren.size() + 1);
		
		for(INode child : mChildren)
			others.add(toNode(child));
		
		if(mParent != null)
			others.add(toNode(mParent));
		
		for(PieceType type : PieceType.values())
		{
			Set<BlockFace> types = new HashSet<BlockFace>(type.getConnections());
			if(types.size() == others.size() && types.containsAll(others))
				return type;
		}
		
		return PieceType.Cross;
	}
	
}


