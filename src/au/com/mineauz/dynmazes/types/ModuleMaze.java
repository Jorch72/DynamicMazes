package au.com.mineauz.dynmazes.types;

import java.util.Collection;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import au.com.mineauz.dynmazes.INode;
import au.com.mineauz.dynmazes.MazeGenerator;
import au.com.mineauz.dynmazes.MazeManager.MazeCommand;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.styles.PieceType;
import au.com.mineauz.dynmazes.styles.Style;
import au.com.mineauz.dynmazes.styles.StyleManager;

public class ModuleMaze extends MazeGenerator<ModuleNode>
{
	Style mStyle;
	Location mMinCorner;
	
	int mWidth;
	int mLength;
	
	private ModuleNode mEntrance;
	private ModuleNode mExit;
	
	public ModuleMaze(String name, Style style, Location loc, int width, int length, BlockFace facing)
	{
		super(name, loc, loc.clone().add(width * style.getPieceSize(), style.getHeight(), length * style.getPieceSize()));
		mMinCorner = loc;
		mWidth = width;
		mLength = length;
		mStyle = style;
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
	protected void onGenerateComplete( ModuleNode root, Collection<ModuleNode> nodes )
	{
		ModuleNode highest = null;
		int score = 0;
		
		for(ModuleNode node : nodes)
		{
			if(node.getX() == 0 || node.getY() == 0 || node.getX() == mWidth-1 || node.getY() == mLength-1)
			{
				int depth = getDepth(node);
				if(depth > score)
				{
					highest = node;
					score = depth;
				}
			}
		}
		
		mEntrance.addChild(root);
		
		if(highest.getX() == 0)
			mExit = new ModuleNode(this, -1, highest.getY(), true);
		else if(highest.getX() == mWidth - 1)
			mExit = new ModuleNode(this, mWidth, highest.getY(), true);
		else if(highest.getY() == 0)
			mExit = new ModuleNode(this, highest.getX(), -1, true);
		else
			mExit = new ModuleNode(this, highest.getX(), mLength, true);
		
		highest.addChild(mExit);
	}
	
	@Override
	protected void placeNode(ModuleNode node)
	{
		if(!node.isTerminus())
			mStyle.getPiece(node.getType()).place(node.toLocation());
	}
	
	@Override
	protected ModuleNode findStart()
	{
		Random rand = getAlgorithm().getRandom();
		
		int side = rand.nextInt(4);
		ModuleNode node = null;
		switch(side)
		{
		case 0:
			node = new ModuleNode(this, 0, rand.nextInt(mLength));
			mEntrance = new ModuleNode(this, -1, node.getY(), true);
			break;
		case 1:
			node = new ModuleNode(this, mWidth - 1, rand.nextInt(mLength));
			mEntrance = new ModuleNode(this, mWidth, node.getY(), true);
			break;
		case 2:
			node = new ModuleNode(this, rand.nextInt(mWidth), 0);
			mEntrance = new ModuleNode(this, node.getX(), -1, true);
			break;
		case 3:
			node = new ModuleNode(this, rand.nextInt(mWidth), mLength - 1);
			mEntrance = new ModuleNode(this, node.getX(), mLength, true);
			break;
		}
		
		return node;
	}
	
	@MazeCommand( command="new" )
	public static ModuleMaze newMaze(Player player, String name, String[] args) throws IllegalArgumentException, NoSuchFieldException
	{
		if(args.length != 3)
			throw new NoSuchFieldException("<style> <width> <length>");
		
		Style style = StyleManager.getStyle(args[0]);
		if(style == null)
			throw new IllegalArgumentException("Cannot find style " + args[0]);
		
		int width, length;
		
		try
		{
			width = Integer.parseInt(args[1]);
			if(width <= 1)
				throw new IllegalArgumentException("Width cannot be less than 2");
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Width must be a whole number larger than 1");
		}
		
		try
		{
			length = Integer.parseInt(args[1]);
			if(length <= 1)
				throw new IllegalArgumentException("Length cannot be less than 2");
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Length must be a whole number larger than 1");
		}
		
		return new ModuleMaze(name, style, player.getLocation(), width, length, Util.toFacingSimplest(player.getEyeLocation().getYaw()));
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
	
	private boolean mTerminus = false;
	
	public ModuleNode(ModuleMaze maze, int x, int y)
	{
		mMaze = maze;
		mX = x;
		mY = y;
		
		mChildren = new HashSet<INode>();
	}
	
	public ModuleNode(ModuleMaze maze, int x, int y, boolean terminus)
	{
		this(maze, x, y);
		mTerminus = terminus;
	}
	
	@Override
	public INode[] getNeighbours()
	{
		if(mTerminus)
			return new INode[0];
		
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
	
	public int getX()
	{
		return mX;
	}
	
	public int getY()
	{
		return mY;
	}
	
	public boolean isTerminus()
	{
		return mTerminus;
	}
}


