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

import au.com.mineauz.dynmazes.AbstractGridNode3D;
import au.com.mineauz.dynmazes.CubeBased;
import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.MazeManager.MazeCommand;
import au.com.mineauz.dynmazes.flags.BlockTypeFlag;
import au.com.mineauz.dynmazes.flags.BooleanFlag;
import au.com.mineauz.dynmazes.misc.BadArgumentException;
import au.com.mineauz.dynmazes.misc.BlockLocation;
import au.com.mineauz.dynmazes.styles.StoredBlock;

public class CubeMaze extends Maze<CubeNode> implements CubeBased<CubeNode>
{
	static BlockFace[] corners = new BlockFace[] {BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST, BlockFace.NORTH_WEST};
	static BlockFace[] directions = {BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH, BlockFace.EAST};
	
	int mWidth;
	int mLength;
	int mHeight;
	
	int mPathWidth;
	int mWallWidth;
	int mWallHeight;
	
	private CubeNode mEntrance;
	private CubeNode mExit;
	
	private BlockTypeFlag mPathMaterial = new BlockTypeFlag();
	private BlockTypeFlag mRoofMaterial = new BlockTypeFlag();
	private BlockTypeFlag mFillMaterial = new BlockTypeFlag();
	private BlockTypeFlag mWallMaterial = new BlockTypeFlag();
	private BooleanFlag mGenStartRoom = new BooleanFlag();
	private BooleanFlag mGenFinishRoom = new BooleanFlag();
	
	public CubeMaze(String name, Location loc, BlockFace facing, int width, int length, int height, int pathWidth, int wallWidth, int wallHeight)
	{
		super(name, "Cube", loc.getWorld());
		
		addFlag("wall-type", mWallMaterial);
		addFlag("path-type", mPathMaterial);
		addFlag("roof-type", mRoofMaterial);
		addFlag("fill-type", mFillMaterial);
		addFlag("gen-start-room", mGenStartRoom);
		addFlag("gen-finish-room", mGenFinishRoom);
		
		mWallMaterial.setValue(new StoredBlock(Material.LEAVES));
		mPathMaterial.setValue(new StoredBlock(Material.GRASS));
		mRoofMaterial.setValue(new StoredBlock(Material.SMOOTH_BRICK));
		mFillMaterial.setValue(new StoredBlock(Material.SMOOTH_BRICK));
		mGenStartRoom.setValue(false);
		mGenFinishRoom.setValue(false);
		
		int widthSize = wallWidth + (wallWidth + pathWidth) * (width + 1);
		int lengthSize = wallWidth + (wallWidth + pathWidth) * (length + 1);
		int heightSize = (wallHeight + 2) * height;
		
		mHeight = height;
		
		switch(facing)
		{
		case NORTH:
			setBounds(new BlockVector(loc.getBlockX() - (wallWidth + (wallWidth + pathWidth)), loc.getBlockY(), loc.getBlockZ() - lengthSize + 1), new BlockVector(loc.getBlockX() + widthSize, loc.getBlockY() + heightSize, loc.getBlockZ() + 1 + (wallWidth + (wallWidth + pathWidth))));
			mWidth = width;
			mLength = length;
			break;
		case SOUTH:
			setBounds(new BlockVector(loc.getBlockX() - widthSize + 1, loc.getBlockY(), loc.getBlockZ() - (wallWidth + (wallWidth + pathWidth))), new BlockVector(loc.getBlockX() + 1 + (wallWidth + (wallWidth + pathWidth)), loc.getBlockY() + heightSize, loc.getBlockZ() + lengthSize));
			mWidth = width;
			mLength = length;
			break;
		case WEST:
			setBounds(new BlockVector(loc.getBlockX() - lengthSize + 1, loc.getBlockY(), loc.getBlockZ() - widthSize + 1), new BlockVector(loc.getBlockX() + 1 + (wallWidth + (wallWidth + pathWidth)), loc.getBlockY() + heightSize, loc.getBlockZ() + 1 + (wallWidth + (wallWidth + pathWidth))));
			mWidth = length;
			mLength = width;
			break;
		case EAST:
		default:
			setBounds(new BlockVector(loc.getBlockX() - (wallWidth + (wallWidth + pathWidth)), loc.getBlockY(), loc.getBlockZ() - (wallWidth + (wallWidth + pathWidth))), new BlockVector(loc.getBlockX() + lengthSize, loc.getBlockY() + heightSize, loc.getBlockZ() + widthSize));
			mWidth = length;
			mLength = width;
			break;
		}
		
		mPathWidth = pathWidth;
		mWallWidth = wallWidth;
		mWallHeight = wallHeight;
	}
	
	public CubeMaze()
	{
		super("Cube");
	}
	
	@Override
	protected void buildNodes()
	{
		allNodes = new ArrayList<CubeNode>(mLength*mWidth*mHeight);
		
		for(int y = 0; y < mHeight; ++y)
		{
			for(int x = 0; x < mWidth; ++x)
			{
				for(int z = 0; z < mLength; ++z)
				{
					allNodes.add(new CubeNode(this, x, y, z));
				}
			}
		}
	}
	
	@Override
	public CubeNode getNodeAt(int x, int y, int z)
	{
		return ((List<CubeNode>)allNodes).get(z + x * mLength + y * (mWidth * mLength));
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
	public int getHeight()
	{
		return mHeight;
	}

	@Override
	protected void processMaze( CubeNode root )
	{
		CubeNode highest = null;
		int score = 0;
		
		for(CubeNode node : allNodes)
		{
			if(node.getX() == 0 || node.getZ() == 0 || node.getX() == mWidth-1 || node.getZ() == mLength-1)
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
			mExit = new CubeNode(this, -1, highest.getY(), highest.getZ());
		else if(highest.getX() == mWidth - 1)
			mExit = new CubeNode(this, mWidth, highest.getY(), highest.getZ());
		else if(highest.getZ() == 0)
			mExit = new CubeNode(this, highest.getX(), highest.getY(), -1);
		else
			mExit = new CubeNode(this, highest.getX(), highest.getY(), mLength);
		
		highest.addChild(mExit);
	}

	@Override
	protected CubeNode findStart()
	{
		Random rand = getAlgorithm().getRandom();
		
		int side = rand.nextInt(4);
		CubeNode node = null;
		switch(side)
		{
		case 0:
			node = getNodeAt(0, rand.nextInt(mHeight), rand.nextInt(mLength));
			mEntrance = new CubeNode(this, -1, node.getY(), node.getZ());
			break;
		case 1:
			node = getNodeAt(mWidth - 1, rand.nextInt(mHeight), rand.nextInt(mLength));
			mEntrance = new CubeNode(this, mWidth, node.getY(), node.getZ());
			break;
		case 2:
			node = getNodeAt(rand.nextInt(mWidth), rand.nextInt(mHeight), 0);
			mEntrance = new CubeNode(this, node.getX(), node.getY(), -1);
			break;
		case 3:
			node = getNodeAt(rand.nextInt(mWidth), rand.nextInt(mHeight), mLength - 1);
			mEntrance = new CubeNode(this, node.getX(), node.getY(), mLength);
			break;
		}
		
		return node;
	}

	@Override
	protected void placeNode( CubeNode node, List<StoredBlock> blocks )
	{
		BlockVector origin = node.toLocation();
	
		Set<BlockFace> connections = node.getConnections();
		
		int holeSize = mPathWidth % 2 == 0 ? 2 : Math.min(3, mPathWidth);
		int holeOffset = (mPathWidth - holeSize) / 2;
		boolean down = connections.contains(BlockFace.DOWN);
		boolean up = connections.contains(BlockFace.UP);
		
		boolean isStandalone = (node == mEntrance || node == mExit);
		
		// Path Center
		for(int x = 0; x < mPathWidth; ++x)
		{
			for(int z = 0; z < mPathWidth; ++z)
			{
				if(down && (x >= holeOffset && x < holeOffset + holeSize && z >= holeOffset && z < holeOffset + holeSize))
					continue;
				
				StoredBlock block = mPathMaterial.getValue().clone();
				BlockVector vec = origin.clone();
				vec.setX(vec.getX() + x);
				vec.setZ(vec.getZ() + z);
				block.setLocation(vec);
				blocks.add(block);
				
				if(isStandalone)
				{
					block = mRoofMaterial.getValue().clone();
					vec = vec.clone();
					vec.setY(vec.getY() - 1);
					block.setLocation(vec);
					blocks.add(block);
				}
			}
		}
		
		if(node.getY() != mHeight - 1)
		{
			for(int x = 0; x < mPathWidth; ++x)
			{
				for(int z = 0; z < mPathWidth; ++z)
				{
					if(up && (x >= holeOffset && x < holeOffset + holeSize && z >= holeOffset && z < holeOffset + holeSize))
						continue;
					
					StoredBlock block = mRoofMaterial.getValue().clone();
					BlockVector vec = origin.clone();
					vec.setX(vec.getX() + x);
					vec.setY(vec.getY() + mWallHeight + 1);
					vec.setZ(vec.getZ() + z);
					block.setLocation(vec);
					blocks.add(block);
				}
			}
		}
		
		if(up)
		{
			for(int y = 1; y <= mWallHeight + 2; ++y)
			{
				for(int x = 0; x < holeSize; ++x)
				{
					for(int z = 0; z < holeSize; ++z)
					{
						StoredBlock block = new StoredBlock(Material.STATIONARY_WATER);
						BlockVector vec = origin.clone();
						vec.setX(vec.getX() + x + holeOffset);
						vec.setY(vec.getY() + y);
						vec.setZ(vec.getZ() + z + holeOffset);
						block.setLocation(vec);
						blocks.add(block);
					}
				}
			}
		}
		
		
		// Path sides
		for(BlockFace face : connections)
		{
			if((face.getModX() < 0 && node.getX() != 0) ||
				(face.getModZ() < 0 && node.getZ() != 0) ||
				face.getModY() != 0)
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
			
			if(node.getY() != mHeight - 1)
			{
				for(int p = 0; p < mPathWidth; ++p)
				{
					for(int w = 0; w < mWallWidth; ++w)
					{
						StoredBlock block = mRoofMaterial.getValue().clone();
						BlockVector vec = newOrigin.clone();
						
						vec.setY(vec.getY() + mWallHeight + 1);
						
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
		}
		
		// Do the corner walls
		for(BlockFace corner : corners)
		{
			if((corner.getModX() < 0 && (node.getX() != 0 && !isStandalone)) ||
				(corner.getModZ() < 0 && (node.getZ() != 0 && !isStandalone)))
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
					for(int y = 0; y <= mWallHeight + 1; ++y)
					{
						StoredBlock block = (y == 0 || (node.getY() != mHeight - 1 && y == mWallHeight + 1) ? mFillMaterial.getValue().clone() : mWallMaterial.getValue().clone());
						
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
			if((face.getModX() < 0 && (node.getX() != 0 && !isStandalone)) ||
				(face.getModZ() < 0 && (node.getZ() != 0 && !isStandalone)) ||
				face.getModY() != 0)
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
					for(int y = 0; y <= mWallHeight + 1; ++y)
					{
						StoredBlock block = (y == 0 || (node.getY() != mHeight - 1 && y == mWallHeight + 1) ? mFillMaterial.getValue().clone() : mWallMaterial.getValue().clone());
						
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
	
	@Override
	protected void placeOther( List<StoredBlock> blocks )
	{
		if(mGenStartRoom.getValue())
			placeNode(mEntrance, blocks);
		if(mGenFinishRoom.getValue())
			placeNode(mExit, blocks);
	}
	
	@MazeCommand(command="new")
	public static CubeMaze newMaze(Player player, String name, String[] args) throws BadArgumentException, NoSuchFieldException
	{
		if(args.length != 3 && args.length != 6)
			throw new NoSuchFieldException("<width> <length> <height> [<pathSize> <wallSize> <wallHeight>]");
		
		int width, length, height;
		int pathSize = 1;
		int wallSize = 1;
		int wallHeight = 3;
		
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
		
		try
		{
			height = Integer.parseInt(args[2]);
			if(height <= 1)
				throw new BadArgumentException(2, "Height cannot be less than 2");
		}
		catch(NumberFormatException e)
		{
			throw new BadArgumentException(2, "Height must be a whole number larger than 1");
		}
		
		if(args.length == 6)
		{
			try
			{
				pathSize = Integer.parseInt(args[3]);
				if(pathSize < 1)
					throw new BadArgumentException(3, "PathSize cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new BadArgumentException(3, "PathSize must be a whole number larger than 0");
			}
			
			try
			{
				wallSize = Integer.parseInt(args[4]);
				if(wallSize < 1)
					throw new BadArgumentException(4, "WallSize cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new BadArgumentException(4, "WallSize must be a whole number larger than 0");
			}
			
			try
			{
				wallHeight = Integer.parseInt(args[5]);
				if(wallHeight < 0)
					throw new BadArgumentException(5, "WallHeight cannot be less than 0");
			}
			catch(NumberFormatException e)
			{
				throw new BadArgumentException(5, "WallHeight must be a positive whole number");
			}
		}
					
		return new CubeMaze(name, player.getLocation(), Util.toFacingSimplest(player.getEyeLocation().getYaw()), width, length, height, pathSize, wallSize, wallHeight);
	}
	
	@Override
	protected void save( ConfigurationSection root )
	{
		super.save(root);
		
		root.set("width", mWidth);
		root.set("length", mLength);
		root.set("height", mHeight);
		
		root.set("pathSize", mPathWidth);
		root.set("wallSize", mWallWidth);
		root.set("wallHeight", mWallHeight);
	}
	
	@Override
	protected void read( ConfigurationSection section ) throws InvalidConfigurationException
	{
		super.read(section);
		
		mWidth = section.getInt("width");
		mLength = section.getInt("length");
		mHeight = section.getInt("height");
		
		mPathWidth = section.getInt("pathSize");
		mWallWidth = section.getInt("wallSize");
		mWallHeight = section.getInt("wallHeight");
		
		
		mPathMaterial = (BlockTypeFlag)getFlag("path-type");
		mFillMaterial = (BlockTypeFlag)getFlag("fill-type");
		mWallMaterial = (BlockTypeFlag)getFlag("wall-type");
		mRoofMaterial = (BlockTypeFlag)getFlag("roof-type");
		mGenStartRoom = (BooleanFlag)getFlag("gen-start-room");
		mGenFinishRoom = (BooleanFlag)getFlag("gen-finish-room");
	}
	
	@Override
	public BlockLocation getStartPoint()
	{
		if(mEntrance == null)
			return null;
		
		BlockVector vec = mEntrance.toLocation();
		vec.setX(vec.getX() + mPathWidth / 2);
		vec.setZ(vec.getZ() + mPathWidth / 2);
		vec.setY(vec.getY() + 1);
		
		return new BlockLocation(vec, mEntrance.toNode((AbstractGridNode3D)mEntrance.getChildren().get(0)));
	}
	
	@Override
	public BlockLocation getEndPoint()
	{
		if(mExit == null)
			return null;
		
		BlockVector vec = mExit.toLocation();
		vec.setX(vec.getX() + mPathWidth / 2);
		vec.setZ(vec.getZ() + mPathWidth / 2);
		vec.setY(vec.getY() + 1);
		
		return new BlockLocation(vec, mExit.toNode((AbstractGridNode3D)mExit.getParents().get(0)));
	}
}

class CubeNode extends AbstractGridNode3D
{
	public CubeNode(CubeMaze maze, int x, int y, int z)
	{
		super(maze, x, y, z);
	}
	
	private CubeMaze getMaze()
	{
		return (CubeMaze)maze;
	}
	
	@Override
	public BlockVector toLocation()
	{
		return getMaze().getMinCorner().clone().add(new Vector(getMaze().mWallWidth + (x+1) * (getMaze().mPathWidth + getMaze().mWallWidth), y * (getMaze().mWallHeight + 2), getMaze().mWallWidth + (z+1) * (getMaze().mPathWidth + getMaze().mWallWidth))).toBlockVector();
	}
}
