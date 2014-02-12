package au.com.mineauz.dynmazes.types;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.material.Leaves;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import au.com.mineauz.dynmazes.INode;
import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.MazeManager.MazeCommand;
import au.com.mineauz.dynmazes.styles.StoredBlock;

public class GridMaze extends Maze<GridNode>
{
	static BlockFace[] corners = new BlockFace[] {BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_WEST};
	static BlockFace[] directions = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
	
	int mWidth;
	int mLength;
	
	int mPathWidth;
	int mWallWidth;
	private int mHeight;
	
	private GridNode mEntrance;
	private GridNode mExit;
	
	private StoredBlock mPathMaterial = new StoredBlock(Material.GRAVEL);
	private StoredBlock mFillMaterial = new StoredBlock(Material.GRASS);
	private StoredBlock mWallMaterial = new StoredBlock(Material.LEAVES, new Leaves(TreeSpecies.DARK_OAK));
	
	public GridMaze(String name, Location loc, BlockFace facing, int width, int length, int pathWidth, int wallWidth, int height)
	{
		super(name, "Grid", loc.getWorld());
		
		int widthSize = wallWidth + (wallWidth + pathWidth) * width;
		int lengthSize = wallWidth + (wallWidth + pathWidth) * length;
		
		switch(facing)
		{
		case NORTH:
			setBounds(new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() - lengthSize + 1), new BlockVector(loc.getBlockX() + widthSize, loc.getBlockY() + height + 1, loc.getBlockZ() + 1));
			mWidth = width;
			mLength = length;
			break;
		case SOUTH:
			setBounds(new BlockVector(loc.getBlockX() - widthSize + 1, loc.getBlockY(), loc.getBlockZ()), new BlockVector(loc.getBlockX() + 1, loc.getBlockY() + height + 1, loc.getBlockZ() + lengthSize));
			mWidth = width;
			mLength = length;
			break;
		case WEST:
			setBounds(new BlockVector(loc.getBlockX() - lengthSize + 1, loc.getBlockY(), loc.getBlockZ() - widthSize + 1), new BlockVector(loc.getBlockX() + 1, loc.getBlockY() + height + 1, loc.getBlockZ() + 1));
			mWidth = length;
			mLength = width;
			break;
		case EAST:
		default:
			setBounds(loc.toVector().toBlockVector(), new BlockVector(loc.getBlockX() + lengthSize, loc.getBlockY() + height + 1, loc.getBlockZ() + widthSize));
			mWidth = length;
			mLength = width;
			break;
		}
		
		mPathWidth = pathWidth;
		mWallWidth = wallWidth;
		mHeight = height;
	}
	
	public GridMaze()
	{
		super("Grid");
	}
	
	@Override
	protected void buildNodes()
	{
		allNodes = new ArrayList<GridNode>(mLength*mWidth);
		
		for(int y = 0; y < mLength; ++y)
		{
			for(int x = 0; x < mWidth; ++x)
			{
				allNodes.add(new GridNode(this, x, y));
			}
		}
	}
	
	GridNode getNodeAt(int x, int y)
	{
		return ((List<GridNode>)allNodes).get(x + y * mWidth);
	}

	@Override
	protected void processMaze( GridNode root )
	{
		GridNode highest = null;
		int score = 0;
		
		for(GridNode node : allNodes)
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
			mExit = new GridNode(this, -1, highest.getY());
		else if(highest.getX() == mWidth - 1)
			mExit = new GridNode(this, mWidth, highest.getY());
		else if(highest.getY() == 0)
			mExit = new GridNode(this, highest.getX(), -1);
		else
			mExit = new GridNode(this, highest.getX(), mLength);
		
		highest.addChild(mExit);
	}

	@Override
	protected GridNode findStart()
	{
		Random rand = getAlgorithm().getRandom();
		
		int side = rand.nextInt(4);
		GridNode node = null;
		switch(side)
		{
		case 0:
			node = getNodeAt(0, rand.nextInt(mLength));
			mEntrance = new GridNode(this, -1, node.getY());
			break;
		case 1:
			node = getNodeAt(mWidth - 1, rand.nextInt(mLength));
			mEntrance = new GridNode(this, mWidth, node.getY());
			break;
		case 2:
			node = getNodeAt(rand.nextInt(mWidth), 0);
			mEntrance = new GridNode(this, node.getX(), -1);
			break;
		case 3:
			node = getNodeAt(rand.nextInt(mWidth), mLength - 1);
			mEntrance = new GridNode(this, node.getX(), mLength);
			break;
		}
		
		return node;
	}

	@Override
	protected void placeNode( GridNode node, List<StoredBlock> blocks )
	{
		BlockVector origin = node.toLocation();
		
		// Path Center
		for(int x = 0; x < mPathWidth; ++x)
		{
			for(int z = 0; z < mPathWidth; ++z)
			{
				StoredBlock block = mPathMaterial.clone();
				BlockVector vec = origin.clone();
				vec.setX(vec.getX() + x);
				vec.setZ(vec.getZ() + z);
				block.setLocation(vec);
				blocks.add(block);
			}
		}
		
		Set<BlockFace> connections = node.getConnections();
		
		for(BlockFace face : connections)
		{
			if((face.getModX() < 0 && node.getX() != 0) ||
				(face.getModZ() < 0 && node.getY() != 0))
				continue;
			
			BlockVector newOrigin = origin.clone();
			
			if(face.getModX() < 0 || face.getModZ() < 0)
			{
				newOrigin.setX(newOrigin.getX() + mWallWidth * face.getModX());
				newOrigin.setZ(newOrigin.getZ() + mWallWidth * face.getModZ());
			}
			else
			{
				newOrigin.setX(newOrigin.getX() + mPathWidth * face.getModX());
				newOrigin.setZ(newOrigin.getZ() + mPathWidth * face.getModZ());
			}
			
			for(int p = 0; p < mPathWidth; ++p)
			{
				for(int w = 0; w < mWallWidth; ++w)
				{
					StoredBlock block = mPathMaterial.clone();
					BlockVector vec = newOrigin.clone();
					
					if(face.getModX() != 0)
					{
						vec.setX(vec.getX() + w);
						vec.setZ(vec.getZ() + p);
					}
					else
					{
						vec.setX(vec.getX() + p);
						vec.setZ(vec.getZ() + w);
					}
					
					block.setLocation(vec);
					blocks.add(block);
				}
			}
		}
		
		// Do the corner walls
		for(BlockFace corner : corners)
		{
			if((corner.getModX() < 0 && node.getX() != 0) ||
				(corner.getModZ() < 0 && node.getY() != 0))
				continue;
			
			BlockVector newOrigin = origin.clone();
			
			if(corner.getModX() < 0)
				newOrigin.setX(newOrigin.getX() - mWallWidth);
			else
				newOrigin.setX(newOrigin.getX() + mPathWidth);
			
			if(corner.getModZ() < 0)
				newOrigin.setZ(newOrigin.getZ() - mWallWidth);
			else
				newOrigin.setZ(newOrigin.getZ() + mPathWidth);
			
			for(int x = 0; x < mWallWidth; ++x)
			{
				for(int z = 0; z < mWallWidth; ++z)
				{
					for(int y = 0; y <= mHeight; ++y)
					{
						StoredBlock block = (y == 0 ? mFillMaterial.clone() : mWallMaterial.clone());
						
						BlockVector vec = newOrigin.clone();
						vec.setX(vec.getX() + x);
						vec.setY(vec.getY() + y);
						vec.setZ(vec.getZ() + z);
						block.setLocation(vec);
						blocks.add(block);
					}
				}
			}
		}
		
		// Do middle walls
		for(BlockFace face : directions)
		{
			if((face.getModX() < 0 && node.getX() != 0) ||
				(face.getModZ() < 0 && node.getY() != 0))
				continue;
			
			if(connections.contains(face))
				continue;
			
			BlockVector newOrigin = origin.clone();
			
			if(face.getModX() < 0)
				newOrigin.setX(newOrigin.getX() - mWallWidth);
			else
				newOrigin.setX(newOrigin.getX() + mPathWidth * face.getModX());
			
			if(face.getModZ() < 0)
				newOrigin.setZ(newOrigin.getZ() - mWallWidth);
			else
				newOrigin.setZ(newOrigin.getZ() + mPathWidth * face.getModZ());
			
			for(int w = 0; w < mWallWidth; ++w)
			{
				for(int p = 0; p < mPathWidth; ++p)
				{
					for(int y = 0; y <= mHeight; ++y)
					{
						StoredBlock block = (y == 0 ? mFillMaterial.clone() : mWallMaterial.clone());
						
						BlockVector vec = newOrigin.clone();
						if(face.getModX() != 0)
						{
							vec.setX(vec.getX() + w);
							vec.setZ(vec.getZ() + p);
						}
						else
						{
							vec.setX(vec.getX() + p);
							vec.setZ(vec.getZ() + w);
						}
						vec.setY(vec.getY() + y);
						
						block.setLocation(vec);
						blocks.add(block);
					}
				}
			}
		}

	}
	
	@MazeCommand(command="new")
	public static GridMaze newMaze(Player player, String name, String[] args) throws IllegalArgumentException, NoSuchFieldException
	{
		if(args.length != 2 && args.length != 5)
			throw new NoSuchFieldException("<width> <length> [<pathSize> <wallSize> <height>]");
		
		int width, length;
		int pathSize = 1;
		int wallSize = 1;
		int height = 3;
		
		try
		{
			width = Integer.parseInt(args[0]);
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
		
		if(args.length == 5)
		{
			try
			{
				pathSize = Integer.parseInt(args[2]);
				if(pathSize < 1)
					throw new IllegalArgumentException("PathSize cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("PathSize must be a whole number larger than 0");
			}
			
			try
			{
				wallSize = Integer.parseInt(args[3]);
				if(wallSize < 1)
					throw new IllegalArgumentException("WallSize cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("WallSize must be a whole number larger than 0");
			}
			
			try
			{
				height = Integer.parseInt(args[4]);
				if(height < 0)
					throw new IllegalArgumentException("Height cannot be less than 0");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("Height must be a positive whole number");
			}
		}
					
		return new GridMaze(name, player.getLocation(), Util.toFacingSimplest(player.getEyeLocation().getYaw()), width, length, pathSize, wallSize, height);
	}
	
	@Override
	protected void save( ConfigurationSection root )
	{
		super.save(root);
		
		root.set("width", mWidth);
		root.set("length", mLength);
		
		root.set("pathSize", mPathWidth);
		root.set("wallSize", mWallWidth);
		root.set("height", mHeight);
	}
	
	@Override
	protected void read( ConfigurationSection section ) throws InvalidConfigurationException
	{
		super.read(section);
		
		mWidth = section.getInt("width");
		mLength = section.getInt("length");
		mPathWidth = section.getInt("pathSize");
		mWallWidth = section.getInt("wallSize");
		mHeight = section.getInt("height");
	}
}

class GridNode implements INode
{
	private int mX;
	private int mY;
	
	private INode mParent;
	private HashSet<INode> mChildren;
	
	private GridMaze mMaze;
	
	public GridNode(GridMaze maze, int x, int y)
	{
		mX = x;
		mY = y;
		
		mMaze = maze;
		
		mChildren = new HashSet<INode>();
	}
	
	@Override
	public BlockVector toLocation()
	{
		return mMaze.getMinCorner().clone().add(new Vector(mMaze.mWallWidth + mX * (mMaze.mPathWidth + mMaze.mWallWidth), 0, mMaze.mWallWidth + mY * (mMaze.mPathWidth + mMaze.mWallWidth))).toBlockVector();
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
	public INode[] getNeighbours()
	{
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
		int xx = ((GridNode)node).mX - mX;
		int yy = ((GridNode)node).mY - mY;
		
		for(BlockFace dir : GridMaze.directions)
		{
			if(dir.getModX() == xx && dir.getModZ() == yy)
				return dir;
		}
		
		return BlockFace.SELF;
		
	}
	
	public Set<BlockFace> getConnections()
	{
		HashSet<BlockFace> others = new HashSet<BlockFace>(mChildren.size() + 1);
		
		for(INode child : mChildren)
			others.add(toNode(child));
		
		if(mParent != null)
			others.add(toNode(mParent));
		
		return others;
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
