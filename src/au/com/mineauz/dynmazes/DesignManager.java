package au.com.mineauz.dynmazes;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.BlockVector;

import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.styles.Piece;
import au.com.mineauz.dynmazes.styles.PieceType;
import au.com.mineauz.dynmazes.styles.Style;
import au.com.mineauz.dynmazes.styles.StyleManager;

public class DesignManager implements Listener
{
	private static HashMap<Player, DesignManager> mCurrentManagers = new HashMap<Player, DesignManager>();
	private Style mStyle;
	private Player mPlayer;
	
	private Location[] mPieceLocations;
	
	private BlockVector mMin;
	private BlockVector mMax;
	private World mWorld;
	
	public static DesignManager beingDesigning(Player player, Style style) throws IllegalStateException
	{
		if(mCurrentManagers.containsKey(player))
			throw new IllegalStateException("You are already designing a piece set. Use /dynmaze design end to stop designing.");
		
		DesignManager manager = new DesignManager(player);
		manager.start(style);
		
		mCurrentManagers.put(player, manager);
		return manager;
	}
	
	public static DesignManager getManager(Player player)
	{
		return mCurrentManagers.get(player);
	}
	
	private DesignManager(Player player)
	{
		mPlayer = player;
	}
	
	private void fixBB(BlockVector min, BlockVector max)
	{
		int val;
		if(max.getBlockX() < min.getBlockX())
		{
			val = max.getBlockX();
			max.setX(min.getBlockX());
			min.setX(val);
		}
		
		if(max.getBlockY() < min.getBlockY())
		{
			val = max.getBlockY();
			max.setY(min.getBlockY());
			min.setY(val);
		}
		
		if(max.getBlockZ() < min.getBlockZ())
		{
			val = max.getBlockZ();
			max.setZ(min.getBlockZ());
			min.setZ(val);
		}
	}
	
	private void setupBoard()
	{
		int columns = 4;
		// Get the 'front' direction
		BlockFace front, left;
		double yaw = mPlayer.getEyeLocation().getYaw();
		Location loc = mPlayer.getLocation();
		
		if(yaw >= 180)
			yaw -= 360;

		if(yaw >= -45 && yaw <= 45) // south
		{
			front = BlockFace.SOUTH;
			left = BlockFace.EAST;
		}
		else if(yaw > 45 && yaw < 135) // west
		{
			front = BlockFace.WEST;
			left = BlockFace.SOUTH;
		}
		else if(yaw > -135 && yaw < -45) // east
		{
			front = BlockFace.EAST;
			left = BlockFace.NORTH;
		}
		else // north
		{
			front = BlockFace.NORTH;
			left = BlockFace.WEST;
		}
		
		mPieceLocations = new Location[PieceType.values().length];
		
		int rows = (int)Math.ceil(PieceType.values().length / (double)columns);
		int width = columns * (mStyle.getPieceSize() + 2);
		int length = rows * (mStyle.getPieceSize() + 2);
		
		BlockVector minCorner = new BlockVector(loc.getBlockX() - (left.getModX() * (width / 2)) - (front.getModX() * (length / 2)), loc.getBlockY(), loc.getBlockZ() - (left.getModZ() * (width / 2)) - (front.getModZ() * (length / 2)));
		BlockVector maxCorner = new BlockVector(loc.getBlockX() + (left.getModX() * (width / 2)) + (front.getModX() * (length / 2)), loc.getBlockY() + mStyle.getHeight() + 2, loc.getBlockZ() + (left.getModZ() * (width / 2)) + (front.getModZ() * (length / 2)));
	
		fixBB(minCorner, maxCorner);
		
		int xx = 0;
		int zz = 0;
		for(PieceType type : PieceType.values())
		{
			int minX = (minCorner.getBlockX() + 1 + xx * (mStyle.getPieceSize() + 2));
			int minZ = (minCorner.getBlockZ() + 1 + zz * (mStyle.getPieceSize() + 2));
			
			mPieceLocations[type.ordinal()] = new Location(mPlayer.getWorld(), minX, minCorner.getBlockY()+1, minZ);
			
			if(front == BlockFace.NORTH || front == BlockFace.SOUTH)
			{
				++xx;
				if(xx >= columns)
				{
					xx = 0;
					++zz;
				}
			}
			else
			{
				++zz;
				if(zz >= columns)
				{
					zz = 0;
					++xx;
				}
			}
		}
		
		mMin = minCorner;
		mMax = maxCorner;
	}
	
	private void drawBoard()
	{
		mWorld = mPlayer.getWorld();
		for(int x = mMin.getBlockX(); x < mMax.getBlockX(); ++x)
		{
			for(int z = mMin.getBlockZ(); z < mMax.getBlockZ(); ++z)
				mWorld.getBlockAt(x, mMin.getBlockY(), z).setType(Material.BEDROCK);
		}
		
		for(PieceType type : PieceType.values())
		{
			Location loc = mPieceLocations[type.ordinal()];
			
			int minX = loc.getBlockX();
			int minZ = loc.getBlockZ();
			int maxX = minX + mStyle.getPieceSize() - 1;
			int maxZ = minZ + mStyle.getPieceSize() - 1;
			
			// Print the piece if any
			Piece piece = mStyle.getPiece(type);
			if(piece != null)
				piece.place(loc);
			
			// Print the area borders
			for(int i = 0; i < mStyle.getPieceSize(); ++i)
			{
				loc.getWorld().getBlockAt(minX + i, loc.getBlockY()-1, minZ).setType(Material.GOLD_BLOCK);
				loc.getWorld().getBlockAt(minX + i, loc.getBlockY()-1, maxZ).setType(Material.GOLD_BLOCK);
				
				loc.getWorld().getBlockAt(minX, loc.getBlockY()-1, minZ + i).setType(Material.GOLD_BLOCK);
				loc.getWorld().getBlockAt(maxX, loc.getBlockY()-1, minZ + i).setType(Material.GOLD_BLOCK);
			}
			
			for(int i = 0; i < mStyle.getPieceSize(); ++i)
			{
				loc.getWorld().getBlockAt(minX + i, loc.getBlockY() + mStyle.getHeight(), minZ).setType(Material.GOLD_BLOCK);
				loc.getWorld().getBlockAt(minX + i, loc.getBlockY() + mStyle.getHeight(), maxZ).setType(Material.GOLD_BLOCK);
				
				loc.getWorld().getBlockAt(minX, loc.getBlockY() + mStyle.getHeight(), minZ + i).setType(Material.GOLD_BLOCK);
				loc.getWorld().getBlockAt(maxX, loc.getBlockY() + mStyle.getHeight(), minZ + i).setType(Material.GOLD_BLOCK);
			}
			
			int centerX = (minX + maxX) / 2;
			int centerZ = (minZ + maxZ) / 2;
			
			// Print the connections
			for(BlockFace con : type.getConnections())
			{
				int x = centerX;
				int z = centerZ;
				
				do
				{
					loc.getWorld().getBlockAt(x, loc.getBlockY()-1, z).setType(Material.LAPIS_BLOCK);
					
					x += con.getModX();
					z += con.getModZ();
				}
				while(x >= minX - 1 && x <= maxX + 1 && z >= minZ - 1 && z <= maxZ + 1);
			}
		}
	}
	
	private void start(Style existing) throws IllegalStateException
	{
		mStyle = existing;
		
		setupBoard();
		
		for(int x = mMin.getBlockX(); x < mMax.getBlockX(); ++x)
		{
			for(int z = mMin.getBlockZ(); z < mMax.getBlockZ(); ++z)
			{
				for(int y = mMin.getBlockY(); y < mMax.getBlockY(); ++y)
				{
					Block block = mPlayer.getWorld().getBlockAt(x, y, z);
					if(!block.isEmpty())
					{
						mMin = null;
						mMax = null;
						mPieceLocations = null;
						throw new IllegalStateException("Design area is not empty.");
					}
				}
			}
		}
		
		Bukkit.getPluginManager().registerEvents(this, DynamicMazePlugin.getInstance());
		
		drawBoard();
		
		mPlayer.teleport(mPlayer.getLocation().add(0, 1, 0));
	}
	
	public void end()
	{
		mCurrentManagers.remove(mPlayer);

		if(mMin == null)
			return;
		
		new ClearingTask(mMin, mMax, mPlayer.getWorld(), new Callback()
		{
			@Override
			public void onFailure( Throwable exception )
			{
				exception.printStackTrace();
				mMin = mMax = null;
				mPieceLocations = null;
				
				HandlerList.unregisterAll(DesignManager.this);
			}
			
			@Override
			public void onComplete()
			{
				mMin = mMax = null;
				mPieceLocations = null;
				
				HandlerList.unregisterAll(DesignManager.this);
			}
		}).start();
	}
	
	public void save(String name)
	{
		if(name != null)
			mStyle = new Style(name, (byte)mStyle.getPieceSize(), (byte)mStyle.getHeight());
		
		if(mStyle.getName() == null)
			throw new IllegalArgumentException("Cannot save this without a name");
		
		for(PieceType type : PieceType.values())
		{
			Piece piece = new Piece((byte)mStyle.getPieceSize(), (byte)mStyle.getHeight());
			piece.setFrom(mPieceLocations[type.ordinal()]);
			mStyle.setPiece(type, piece);
		}
		
		StyleManager.saveStyle(mStyle);
	}
	
	public Style getStyle()
	{
		return mStyle;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onBlockBreak(BlockBreakEvent event)
	{
		if(mMin == null || event.getBlock().getWorld() != mWorld)
			return;
		
		if(event.getBlock().getLocation().toVector().isInAABB(mMin, mMax))
		{
			if(event.getBlock().getY() == mMin.getBlockY())
				event.setCancelled(true);
			else if(event.getBlock().getY() == mMax.getBlockY()-1)
				event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onBlockPlace(BlockPlaceEvent event)
	{
		if(mMin == null || event.getBlock().getWorld() != mWorld)
			return;
		
		if(event.getBlock().getLocation().toVector().isInAABB(mMin, mMax))
		{
			if(event.getBlock().getY() == mMin.getBlockY())
				event.setCancelled(true);
			else if(event.getBlock().getY() == mMax.getBlockY()-1)
				event.setCancelled(true);
		}
	}
	
	
}
