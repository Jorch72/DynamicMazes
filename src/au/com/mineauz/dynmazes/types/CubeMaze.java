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
	private BlockTypeFlag mLadderWallMaterial = new BlockTypeFlag();
	
	public CubeMaze(String name, Location loc, BlockFace facing, int width, int length, int height, int pathWidth, int wallWidth, int wallHeight)
	{
		super(name, "Cube", loc.getWorld());
		
		addFlag("wall-type", mWallMaterial);
		addFlag("path-type", mPathMaterial);
		addFlag("roof-type", mRoofMaterial);
		addFlag("fill-type", mFillMaterial);
		addFlag("ladder-wall-type", mLadderWallMaterial);
		
		mWallMaterial.setValue(new StoredBlock(Material.LEAVES));
		mLadderWallMaterial.setValue(new StoredBlock(Material.LOG));
		mPathMaterial.setValue(new StoredBlock(Material.GRAVEL));
		mRoofMaterial.setValue(new StoredBlock(Material.WOOD));
		mFillMaterial.setValue(new StoredBlock(Material.GRASS));
		
		int widthSize = wallWidth + (wallWidth + pathWidth) * width;
		int lengthSize = wallWidth + (wallWidth + pathWidth) * length;
		int heightSize = (wallHeight + 2) * height;
		
		mHeight = height;
		
		switch(facing)
		{
		case NORTH:
			setBounds(new BlockVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() - lengthSize + 1), new BlockVector(loc.getBlockX() + widthSize, loc.getBlockY() + heightSize, loc.getBlockZ() + 1));
			mWidth = width;
			mLength = length;
			break;
		case SOUTH:
			setBounds(new BlockVector(loc.getBlockX() - widthSize + 1, loc.getBlockY(), loc.getBlockZ()), new BlockVector(loc.getBlockX() + 1, loc.getBlockY() + heightSize, loc.getBlockZ() + lengthSize));
			mWidth = width;
			mLength = length;
			break;
		case WEST:
			setBounds(new BlockVector(loc.getBlockX() - lengthSize + 1, loc.getBlockY(), loc.getBlockZ() - widthSize + 1), new BlockVector(loc.getBlockX() + 1, loc.getBlockY() + heightSize, loc.getBlockZ() + 1));
			mWidth = length;
			mLength = width;
			break;
		case EAST:
		default:
			setBounds(loc.toVector().toBlockVector(), new BlockVector(loc.getBlockX() + lengthSize, loc.getBlockY() + heightSize, loc.getBlockZ() + widthSize));
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
		// TODO: Placeing it correctly
		
		Set<BlockFace> connections = node.getConnections();
		
		int holeSize = mPathWidth % 2 == 0 ? 2 : Math.min(3, mPathWidth);
		int holeOffset = (mPathWidth - holeSize) / 2;
		boolean down = connections.contains(BlockFace.DOWN);
		boolean up = connections.contains(BlockFace.UP);
		
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
			if((corner.getModX() < 0 && node.getX() != 0) ||
				(corner.getModZ() < 0 && node.getZ() != 0))
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
			if((face.getModX() < 0 && node.getX() != 0) ||
				(face.getModZ() < 0 && node.getZ() != 0) ||
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
	
	@MazeCommand(command="new")
	public static CubeMaze newMaze(Player player, String name, String[] args) throws IllegalArgumentException, NoSuchFieldException
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
		
		try
		{
			height = Integer.parseInt(args[2]);
			if(height <= 1)
				throw new IllegalArgumentException("Height cannot be less than 2");
		}
		catch(NumberFormatException e)
		{
			throw new IllegalArgumentException("Height must be a whole number larger than 1");
		}
		
		if(args.length == 6)
		{
			try
			{
				pathSize = Integer.parseInt(args[3]);
				if(pathSize < 1)
					throw new IllegalArgumentException("PathSize cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("PathSize must be a whole number larger than 0");
			}
			
			try
			{
				wallSize = Integer.parseInt(args[4]);
				if(wallSize < 1)
					throw new IllegalArgumentException("WallSize cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("WallSize must be a whole number larger than 0");
			}
			
			try
			{
				wallHeight = Integer.parseInt(args[5]);
				if(wallHeight < 0)
					throw new IllegalArgumentException("WallHeight cannot be less than 0");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("WallHeight must be a positive whole number");
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
		mLadderWallMaterial = (BlockTypeFlag)getFlag("ladder-wall-type");
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
		return getMaze().getMinCorner().clone().add(new Vector(getMaze().mWallWidth + x * (getMaze().mPathWidth + getMaze().mWallWidth), y * (getMaze().mWallHeight + 2), getMaze().mWallWidth + z * (getMaze().mPathWidth + getMaze().mWallWidth))).toBlockVector();
	}
}
