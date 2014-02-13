package au.com.mineauz.dynmazes.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import au.com.mineauz.dynmazes.INode;
import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.MazeManager.MazeCommand;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.flags.Flag;
import au.com.mineauz.dynmazes.flags.StyleFlag;
import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.styles.PieceType;
import au.com.mineauz.dynmazes.styles.StoredBlock;
import au.com.mineauz.dynmazes.styles.Style;
import au.com.mineauz.dynmazes.styles.StyleManager;

public class ModuleMaze extends Maze<ModuleNode>
{
	int mWidth;
	int mLength;
	
	private ModuleNode mEntrance;
	private ModuleNode mExit;
	
	StyleFlag mStyle = new StyleFlag();
	
	public ModuleMaze(String name, Style style, Location loc, int width, int length, BlockFace facing)
	{
		super(name, "Module", loc.getWorld());
		
		int widthSize = (width + 2) * style.getPieceSize();
		int lengthSize = (length + 2) * style.getPieceSize();
		
		switch(facing)
		{
		case NORTH:
			setBounds(new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() - lengthSize + 1), new BlockVector(loc.getBlockX() + widthSize, loc.getBlockY() + style.getHeight(), loc.getBlockZ() + 1));
			mWidth = width;
			mLength = length;
			break;
		case SOUTH:
			setBounds(new BlockVector(loc.getBlockX() - widthSize + 1, loc.getBlockY(), loc.getBlockZ()), new BlockVector(loc.getBlockX() + 1, loc.getBlockY() + style.getHeight(), loc.getBlockZ() + lengthSize));
			mWidth = width;
			mLength = length;
			break;
		case WEST:
			setBounds(new BlockVector(loc.getBlockX() - lengthSize + 1, loc.getBlockY(), loc.getBlockZ() - widthSize + 1), new BlockVector(loc.getBlockX() + 1, loc.getBlockY() + style.getHeight(), loc.getBlockZ() + 1));
			mWidth = length;
			mLength = width;
			break;
		case EAST:
		default:
			setBounds(loc.toVector().toBlockVector(), new BlockVector(loc.getBlockX() + lengthSize, loc.getBlockY() + style.getHeight(), loc.getBlockZ() + widthSize));
			mWidth = length;
			mLength = width;
			break;
		}
		
		mStyle.setValue(style);
		addFlag("style", mStyle);
	}
	
	protected ModuleMaze()
	{
		super("Module");
	}
	
	@Override
	protected void processMaze( ModuleNode root )
	{
		ModuleNode highest = null;
		int score = 0;
		
		for(ModuleNode node : allNodes)
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
			mExit = new ModuleNode(this, -1, highest.getY(), 2);
		else if(highest.getX() == mWidth - 1)
			mExit = new ModuleNode(this, mWidth, highest.getY(), 2);
		else if(highest.getY() == 0)
			mExit = new ModuleNode(this, highest.getX(), -1, 2);
		else
			mExit = new ModuleNode(this, highest.getX(), mLength, 2);
		
		highest.addChild(mExit);
		
		allNodes.add(mEntrance);
		allNodes.add(mExit);
	}
	
	@Override
	protected void placeNode(ModuleNode node, List<StoredBlock> blocks)
	{
		blocks.addAll(mStyle.getValue().getPiece(node.getType()).getBlocks(node.toLocation()));
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
			node = getNodeAt(0, rand.nextInt(mLength));
			mEntrance = new ModuleNode(this, -1, node.getY(), 1);
			break;
		case 1:
			node = getNodeAt(mWidth - 1, rand.nextInt(mLength));
			mEntrance = new ModuleNode(this, mWidth, node.getY(), 1);
			break;
		case 2:
			node = getNodeAt(rand.nextInt(mWidth), 0);
			mEntrance = new ModuleNode(this, node.getX(), -1, 1);
			break;
		case 3:
			node = getNodeAt(rand.nextInt(mWidth), mLength - 1);
			mEntrance = new ModuleNode(this, node.getX(), mLength, 1);
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
			length = Integer.parseInt(args[2]);
			if(length <= 1)
				throw new IllegalArgumentException("Length cannot be less than 2");
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Length must be a whole number larger than 1");
		}
		
		return new ModuleMaze(name, style, player.getLocation(), width, length, Util.toFacingSimplest(player.getEyeLocation().getYaw()));
	}

	@Override
	protected void buildNodes()
	{
		allNodes = new ArrayList<ModuleNode>(mLength*mWidth);
		
		for(int y = 0; y < mLength; ++y)
		{
			for(int x = 0; x < mWidth; ++x)
			{
				allNodes.add(new ModuleNode(this, x, y));
			}
		}
	}
	
	ModuleNode getNodeAt(int x, int y)
	{
		return ((List<ModuleNode>)allNodes).get(x + y * mWidth);
	}
	
	@Override
	protected void save( ConfigurationSection root )
	{
		super.save(root);
		
		root.set("width", mWidth);
		root.set("length", mLength);
	}
	
	@Override
	protected void read( ConfigurationSection section ) throws InvalidConfigurationException
	{
		super.read(section);
		
		mWidth = section.getInt("width");
		mLength = section.getInt("length");
		
		mStyle = (StyleFlag)getFlag("style");
	}
	
	@Override
	public <Type> void onFlagChanged( String name, final Flag<Type> flag, Type value )
	{
		if(name.equals("style"))
		{
			Style oldStyle = (Style)value;
			final Style newStyle = ((StyleFlag)flag).getValue();
			
			if(oldStyle.getHeight() != newStyle.getHeight() || oldStyle.getPieceSize() != newStyle.getPieceSize())
			{
				clear(true, new Callback()
				{
					@Override
					public void onFailure( Throwable exception )
					{
					}
					
					@Override
					public void onComplete()
					{
						int widthSize = (mWidth + 2) * newStyle.getPieceSize();
						int lengthSize = (mLength + 2) * newStyle.getPieceSize();
						
						setBounds(getMinCorner(), new BlockVector(getMinCorner().getBlockX() + lengthSize, getMinCorner().getBlockY() + newStyle.getHeight(), getMinCorner().getBlockZ() + widthSize));
						prepareArea(new StoredBlock(Material.BEDROCK), null);
					}
				});
			}
		}
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
	
	private int mType = 0;
	
	public ModuleNode(ModuleMaze maze, int x, int y)
	{
		mMaze = maze;
		mX = x;
		mY = y;
		
		mChildren = new HashSet<INode>();
	}
	
	public ModuleNode(ModuleMaze maze, int x, int y, int type)
	{
		this(maze, x, y);
		mType = type;
	}
	
	@Override
	public INode[] getNeighbours()
	{
		if(mType != 0)
			return new INode[0];
		
		if(mX == 0)
		{
			if(mY == 0)
				return new INode[] {mMaze.getNodeAt(mX + 1, mY), mMaze.getNodeAt(mX, mY + 1)};
			if(mY == mMaze.mLength - 1)
				return new INode[] {mMaze.getNodeAt(mX + 1, mY), mMaze.getNodeAt(mX, mY - 1)};
			
			return new INode[] {mMaze.getNodeAt(mX + 1, mY), mMaze.getNodeAt(mX, mY - 1), mMaze.getNodeAt(mX, mY + 1)};
		}
		else if(mX == mMaze.mWidth - 1)
		{
			if(mY == 0)
				return new INode[] {mMaze.getNodeAt(mX - 1, mY), mMaze.getNodeAt(mX, mY + 1)};
			if(mY == mMaze.mLength - 1)
				return new INode[] {mMaze.getNodeAt(mX - 1, mY), mMaze.getNodeAt(mX, mY - 1)};
			
			return new INode[] {mMaze.getNodeAt(mX - 1, mY), mMaze.getNodeAt(mX, mY - 1), mMaze.getNodeAt(mX, mY + 1)};
		}
		else
		{
			if(mY == 0)
				return new INode[] {mMaze.getNodeAt(mX + 1, mY), mMaze.getNodeAt(mX - 1, mY), mMaze.getNodeAt(mX, mY + 1)};
			if(mY == mMaze.mLength - 1)
				return new INode[] {mMaze.getNodeAt(mX + 1, mY), mMaze.getNodeAt(mX - 1, mY), mMaze.getNodeAt(mX, mY - 1)};
			
			return new INode[] {mMaze.getNodeAt(mX + 1, mY), mMaze.getNodeAt(mX - 1, mY), mMaze.getNodeAt(mX, mY - 1), mMaze.getNodeAt(mX, mY + 1)};
		}
	}
	
	@Override
	public BlockVector toLocation()
	{
		return mMaze.getMinCorner().clone().add(new Vector(mX * mMaze.mStyle.getValue().getPieceSize() + mMaze.mStyle.getValue().getPieceSize(), 0, mY * mMaze.mStyle.getValue().getPieceSize() + mMaze.mStyle.getValue().getPieceSize())).toBlockVector();
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
		
		PieceType[] set = PieceType.NormalPieces;
		if(mType == 1)
			set = PieceType.StartPieces;
		else if(mType == 2)
			set = PieceType.FinishPieces;
		
		for(PieceType type : set)
		{
			Set<BlockFace> types = new HashSet<BlockFace>(type.getConnections());
			if(types.size() == others.size() && types.containsAll(others))
				return type;
		}
		
		System.out.println(String.format("Unknown configuration: %d childs %s parent", mChildren.size(), mParent));
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
}


