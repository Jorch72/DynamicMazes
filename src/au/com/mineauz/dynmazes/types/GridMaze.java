package au.com.mineauz.dynmazes.types;

import java.util.ArrayList;
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

import au.com.mineauz.dynmazes.AbstractGridNode;
import au.com.mineauz.dynmazes.GridBased;
import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.MazeManager.MazeCommand;
import au.com.mineauz.dynmazes.flags.BlockTypeFlag;
import au.com.mineauz.dynmazes.misc.BadArgumentException;
import au.com.mineauz.dynmazes.misc.BlockLocation;
import au.com.mineauz.dynmazes.styles.StoredBlock;

public class GridMaze extends Maze<GridNode> implements GridBased<GridNode>
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
	
	private BlockTypeFlag mPathMaterial = new BlockTypeFlag();
	private BlockTypeFlag mFillMaterial = new BlockTypeFlag();
	private BlockTypeFlag mWallMaterial = new BlockTypeFlag();
	
	public GridMaze(String name, Location loc, BlockFace facing, int width, int length, int pathWidth, int wallWidth, int height)
	{
		super(name, "Grid", loc.getWorld());
		
		addFlag("wall-type", mWallMaterial);
		addFlag("path-type", mPathMaterial);
		addFlag("fill-type", mFillMaterial);
		
		mWallMaterial.setValue(new StoredBlock(Material.LEAVES));
		mPathMaterial.setValue(new StoredBlock(Material.GRAVEL));
		mFillMaterial.setValue(new StoredBlock(Material.GRASS));
		
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
	
	@Override
	public GridNode getNodeAt(int x, int y)
	{
		return ((List<GridNode>)allNodes).get(x + y * mWidth);
	}
	
	@Override
	public int getLength()
	{
		return mLength;
	}
	
	@Override
	public int getWidth()
	{
		return mWidth;
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
				int depth = node.getDepth();
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
				StoredBlock block = mPathMaterial.getValue().clone();
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
					StoredBlock block = mPathMaterial.getValue().clone();
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
						StoredBlock block = (y == 0 ? mFillMaterial.getValue().clone() : mWallMaterial.getValue().clone());
						
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
						StoredBlock block = (y == 0 ? mFillMaterial.getValue().clone() : mWallMaterial.getValue().clone());
						
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
	public static GridMaze newMaze(Player player, String name, String[] args) throws BadArgumentException, NoSuchFieldException
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
				throw new BadArgumentException(0, "Width cannot be less than 2");
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(0, "Width must be a whole number larger than 1");
		}
		
		try
		{
			length = Integer.parseInt(args[1]);
			if(length <= 1)
				throw new BadArgumentException(1, "Length cannot be less than 2");
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(1, "Length must be a whole number larger than 1");
		}
		
		if(args.length == 5)
		{
			try
			{
				pathSize = Integer.parseInt(args[2]);
				if(pathSize < 1)
					throw new BadArgumentException(2, "PathSize cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new BadArgumentException(2, "PathSize must be a whole number larger than 0");
			}
			
			try
			{
				wallSize = Integer.parseInt(args[3]);
				if(wallSize < 1)
					throw new BadArgumentException(3, "WallSize cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new BadArgumentException(3, "WallSize must be a whole number larger than 0");
			}
			
			try
			{
				height = Integer.parseInt(args[4]);
				if(height < 0)
					throw new BadArgumentException(4, "Height cannot be less than 0");
			}
			catch(NumberFormatException e)
			{
				throw new BadArgumentException(4, "Height must be a positive whole number");
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
		
		mPathMaterial = (BlockTypeFlag)getFlag("path-type");
		mFillMaterial = (BlockTypeFlag)getFlag("fill-type");
		mWallMaterial = (BlockTypeFlag)getFlag("wall-type");
	}
	
	@Override
	public BlockLocation getStartPoint()
	{
		if(mEntrance == null)
			return null;
		
		BlockVector vec = mEntrance.toLocation();
		vec.setX(vec.getX() + mPathWidth / 2);
		vec.setZ(vec.getZ() + mPathWidth / 2);
		
		return new BlockLocation(vec, mEntrance.toNode((AbstractGridNode)mEntrance.getChildren().get(0)));
	}
	
	@Override
	public BlockLocation getEndPoint()
	{
		return new BlockLocation(mExit.toLocation(), mExit.toNode((AbstractGridNode)mExit.getParents().get(0)));
	}
}

class GridNode extends AbstractGridNode
{
	public GridNode(GridMaze maze, int x, int y)
	{
		super(maze, x, y);
	}
	
	private GridMaze getMaze()
	{
		return (GridMaze)maze;
	}
	
	@Override
	public BlockVector toLocation()
	{
		return getMaze().getMinCorner().clone().add(new Vector(getMaze().mWallWidth + x * (getMaze().mPathWidth + getMaze().mWallWidth), 0, getMaze().mWallWidth + y * (getMaze().mPathWidth + getMaze().mWallWidth))).toBlockVector();
	}
}
