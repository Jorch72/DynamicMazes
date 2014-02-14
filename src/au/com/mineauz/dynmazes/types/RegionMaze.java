package au.com.mineauz.dynmazes.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.Region;

import au.com.mineauz.dynmazes.AbstractGridNode;
import au.com.mineauz.dynmazes.GridBased;
import au.com.mineauz.dynmazes.INode;
import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.MazeManager.MazeCommand;
import au.com.mineauz.dynmazes.flags.BlockTypeFlag;
import au.com.mineauz.dynmazes.misc.WorldEditUtil;
import au.com.mineauz.dynmazes.styles.StoredBlock;

public class RegionMaze extends Maze<RegionNode> implements GridBased<RegionNode>
{
	private Region mRegion;
	
	int mWidth;
	int mLength;
	
	int mPathWidth;
	int mWallWidth;
	private int mHeight;
	
	private RegionNode mEntrance;
	private RegionNode mExit;
	
	private BlockTypeFlag mPathMaterial = new BlockTypeFlag();
	private BlockTypeFlag mFillMaterial = new BlockTypeFlag();
	private BlockTypeFlag mExtFillMaterial = new BlockTypeFlag();
	private BlockTypeFlag mWallMaterial = new BlockTypeFlag();
	
	public RegionMaze(String name, World world, Region region, int pathWidth, int wallWidth, int height)
	{
		super(name, "Region", world);
		mRegion = region;
		
		addFlag("wall-type", mWallMaterial);
		addFlag("path-type", mPathMaterial);
		addFlag("fill-type", mFillMaterial);
		addFlag("out-fill-type", mExtFillMaterial);
		
		mWallMaterial.setValue(new StoredBlock(Material.LEAVES));
		mPathMaterial.setValue(new StoredBlock(Material.GRAVEL));
		mFillMaterial.setValue(new StoredBlock(Material.GRASS));
		mExtFillMaterial.setValue(new StoredBlock(Material.GRASS));
		
		mPathWidth = pathWidth;
		mWallWidth = wallWidth;
		mHeight = height;
		
		if(region instanceof EllipsoidRegion)
			setBounds(new BlockVector(mRegion.getMinimumPoint().getBlockX(), mRegion.getCenter().getBlockY() + 1, mRegion.getMinimumPoint().getBlockZ()), new BlockVector(mRegion.getMaximumPoint().getBlockX() + 1, mRegion.getCenter().getBlockY() + 2 + height, mRegion.getMaximumPoint().getBlockZ() + 1));
		else
			setBounds(new BlockVector(mRegion.getMinimumPoint().getBlockX(), mRegion.getMinimumPoint().getBlockY() + 1, mRegion.getMinimumPoint().getBlockZ()), new BlockVector(mRegion.getMaximumPoint().getBlockX() + 1, mRegion.getMinimumPoint().getBlockY() + 2 + height, mRegion.getMaximumPoint().getBlockZ() + 1));
		
		mWidth = (mRegion.getWidth() - wallWidth) / (pathWidth + wallWidth);
		mLength = (mRegion.getLength() - wallWidth) / (pathWidth + wallWidth);
	}
	
	protected RegionMaze()
	{
		super("Region");
	}

	@Override
	protected void buildNodes()
	{
		allNodes = new ArrayList<RegionNode>(mWidth * mLength);
		int middle = mRegion.getCenter().getBlockY();
		
		for(int y = 0; y < mLength; ++y)
		{
			for(int x = 0; x < mWidth; ++x)
			{
				com.sk89q.worldedit.Vector vec = new com.sk89q.worldedit.Vector(getMinCorner().getBlockX() + mWallWidth + x * (mWallWidth + mPathWidth), middle, getMinCorner().getBlockZ() + mWallWidth + y * (mWallWidth + mPathWidth));
				if(mRegion.contains(vec))
					allNodes.add(new RegionNode(this, x, y));
				else
					allNodes.add(null);
			}
		}
		
	}

	@Override
	protected void processMaze( RegionNode root )
	{
		RegionNode highest = null;
		int score = 0;
		
		for(RegionNode node : allNodes)
		{
			if(node == null)
				continue;
			
			if(node.isOnEdge())
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
		
		BlockFace[] available = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		
		int count = available.length;
		for(int i = 0; i < count; ++i)
		{
			BlockFace face = available[i];
			
			int x = highest.getX() + face.getModX();
			int y = highest.getY() + face.getModZ();
			
			if(x >= 0 && x < mWidth && y >= 0 && y < mLength)
			{
				if(getNodeAt(x, y) != null)
				{
					// Shift the remaining ones to cover this one
					for(int j = i; j < count - 1; ++j)
						available[j] = available[j + 1];
					
					--i;
					--count;
					continue;
				}
			}
		}
		
		BlockFace selected = available[getAlgorithm().getRandom().nextInt(count)];
		mExit = new RegionNode(this, highest.getX() + selected.getModX(), highest.getY() + selected.getModZ());
		
		highest.addChild(mExit);
	}

	@Override
	protected RegionNode findStart()
	{
		ArrayList<RegionNode> edgeNodes = new ArrayList<RegionNode>();
		
		for(int i = 0; i < allNodes.size(); ++i)
		{
			RegionNode node = ((List<RegionNode>)allNodes).get(i);
			if(node != null && node.isOnEdge())
				edgeNodes.add(node);
		}
		
		RegionNode start = edgeNodes.get(getAlgorithm().getRandom().nextInt(edgeNodes.size()));

		BlockFace[] available = new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
		
		int count = available.length;
		for(int i = 0; i < count; ++i)
		{
			BlockFace face = available[i];
			
			int x = start.getX() + face.getModX();
			int y = start.getY() + face.getModZ();
			
			if(x >= 0 && x < mWidth && y >= 0 && y < mLength)
			{
				if(getNodeAt(x, y) != null)
				{
					// Shift the remaining ones to cover this one
					for(int j = i; j < count - 1; ++j)
						available[j] = available[j + 1];
					
					--i;
					--count;
					continue;
				}
			}
		}
		
		BlockFace selected = available[getAlgorithm().getRandom().nextInt(count)];
		mEntrance = new RegionNode(this, start.getX() + selected.getModX(), start.getY() + selected.getModZ());
	
		return start;
	}

	@Override
	protected void placeNode( RegionNode node, List<StoredBlock> blocks )
	{
		if(node == null)
			return;
		
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
			if((face.getModX() < 0 && (node.getX() != 0 && !node.isOnEdge())) ||
				(face.getModZ() < 0 && (node.getY() != 0 && !node.isOnEdge())))
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
		for(BlockFace corner : GridMaze.corners)
		{
			if((corner.getModX() < 0 && (node.getX() != 0 && !node.isOnEdge())) ||
				(corner.getModZ() < 0 && (node.getY() != 0 && !node.isOnEdge())))
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
		for(BlockFace face : GridMaze.directions)
		{
			if((face.getModX() < 0 && (node.getX() != 0 && !node.isOnEdge())) ||
				(face.getModZ() < 0 && (node.getY() != 0 && !node.isOnEdge())))
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
	
	@Override
	public RegionNode getNodeAt(int x, int y)
	{
		return ((List<RegionNode>)allNodes).get(x + y * mWidth);
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
	protected void save( ConfigurationSection root )
	{
		super.save(root);
		
		root.set("width", mWidth);
		root.set("length", mLength);
		
		root.set("pathSize", mPathWidth);
		root.set("wallSize", mWallWidth);
		root.set("height", mHeight);
		
		WorldEditUtil.saveRegion(mRegion, root.createSection("region"));
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
		mExtFillMaterial = (BlockTypeFlag)getFlag("out-fill-type");
		mWallMaterial = (BlockTypeFlag)getFlag("wall-type");
		
		mRegion = WorldEditUtil.loadRegion(section.getConfigurationSection("region"));
	}
	
	@MazeCommand(command="new")
	public static RegionMaze newMaze(Player player, String name, String[] args) throws IllegalArgumentException, NoSuchFieldException
	{
		if(args.length != 0 && args.length != 3)
			throw new NoSuchFieldException("[<pathSize> <wallSize> <height>]");
		
		Region region;
		try
		{
			region = WorldEdit.getInstance().getSession(player.getName()).getSelection(BukkitUtil.getLocalWorld(player.getWorld()));
			if(region == null)
				throw new IllegalArgumentException("You have nothing selected");
		}
		catch ( IncompleteRegionException e )
		{
			throw new IllegalArgumentException("Your selection is imcomplete");
		}
		
		int pathSize = 1;
		int wallSize = 1;
		int height = 3;
		
		if(args.length == 3)
		{
			try
			{
				pathSize = Integer.parseInt(args[0]);
				if(pathSize < 1)
					throw new IllegalArgumentException("PathSize cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("PathSize must be a whole number larger than 0");
			}
			
			try
			{
				wallSize = Integer.parseInt(args[1]);
				if(wallSize < 1)
					throw new IllegalArgumentException("WallSize cannot be less than 1");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("WallSize must be a whole number larger than 0");
			}
			
			try
			{
				height = Integer.parseInt(args[2]);
				if(height < 0)
					throw new IllegalArgumentException("Height cannot be less than 0");
			}
			catch(NumberFormatException e)
			{
				throw new IllegalArgumentException("Height must be a positive whole number");
			}
		}
		
		return new RegionMaze(name, player.getWorld(), region, pathSize, wallSize, height);
	}
}

class RegionNode extends AbstractGridNode
{
	public RegionNode(RegionMaze maze, int x, int y)
	{
		super(maze, x, y);
	}
	
	private RegionMaze getMaze()
	{
		return (RegionMaze)maze;
	}
	
	@Override
	public BlockVector toLocation()
	{
		return getMaze().getMinCorner().clone().add(new Vector(getMaze().mWallWidth + x * (getMaze().mPathWidth + getMaze().mWallWidth), 0, getMaze().mWallWidth + y * (getMaze().mPathWidth + getMaze().mWallWidth))).toBlockVector();
	}
	
	private INode[] removeNulls(INode... nodes)
	{
		int count = nodes.length;
		for(int i = 0; i < count; ++i)
		{
			if(nodes[i] == null)
			{
				for(int j = i; j < count - 1; ++j)
					nodes[j] = nodes[j+1];
				
				--i;
				--count;
			}
		}
		
		if(count == nodes.length)
			return nodes;
		else
			return Arrays.copyOf(nodes, count);
	}
	
	@Override
	public INode[] getNeighbours()
	{
		return removeNulls(super.getNeighbours());
	}
	
	public boolean isOnEdge()
	{
		if(x == 0)
			return true;
		else if(x == maze.getWidth() - 1)
			return true;
		else if(y == 0)
			return true;
		else if(y == maze.getLength() - 1)
			return true;
		else if(maze.getNodeAt(x + 1, y) == null)
			return true;
		else if(maze.getNodeAt(x - 1, y) == null)
			return true;
		else if(maze.getNodeAt(x, y + 1) == null)
			return true;
		else if(maze.getNodeAt(x, y - 1) == null)
			return true;
		else
			return false;
	}
}
