package au.com.mineauz.dynmazes;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import au.com.mineauz.dynmazes.styles.Piece;
import au.com.mineauz.dynmazes.styles.PieceType;
import au.com.mineauz.dynmazes.styles.Style;

public class DesignManager implements Listener
{
	private static HashMap<Player, DesignManager> mCurrentManagers = new HashMap<Player, DesignManager>();
	private Style mStyle;
	private Player mPlayer;
	private Location mSource;
	
	private int mWidth;
	private int mLength;
	
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
	
	private void drawBoard(Location loc)
	{
		for(int x = loc.getBlockX(); x < loc.getBlockX() + mLength; ++x)
		{
			for(int z = loc.getBlockZ(); z < loc.getBlockZ() + mWidth; ++z)
				mPlayer.getWorld().getBlockAt(x, loc.getBlockY(), z).setType(Material.BEDROCK);
		}
		
		int xx = 0;
		int zz = 0;
		for(PieceType type : PieceType.values())
		{
			int minX = (loc.getBlockX() + 1 + xx * (mStyle.getPieceSize() + 2));
			int minZ = (loc.getBlockZ() + 1 + zz * (mStyle.getPieceSize() + 2));
			int maxX = minX + mStyle.getPieceSize() - 1;
			int maxZ = minZ + mStyle.getPieceSize() - 1;
			
			// Print the piece if any
			Piece piece = mStyle.getPiece(type);
			if(piece != null)
				piece.place(new Location(loc.getWorld(), minX, loc.getBlockY() + 1, minZ));
			
			// Print the area borders
			for(int i = 0; i < mStyle.getPieceSize(); ++i)
			{
				loc.getWorld().getBlockAt(minX + i, loc.getBlockY(), minZ).setType(Material.GOLD_BLOCK);
				loc.getWorld().getBlockAt(minX + i, loc.getBlockY(), maxZ).setType(Material.GOLD_BLOCK);
				
				loc.getWorld().getBlockAt(minX, loc.getBlockY(), minZ + i).setType(Material.GOLD_BLOCK);
				loc.getWorld().getBlockAt(maxX, loc.getBlockY(), minZ + i).setType(Material.GOLD_BLOCK);
			}
			
			for(int i = 0; i < mStyle.getPieceSize(); ++i)
			{
				loc.getWorld().getBlockAt(minX + i, loc.getBlockY() + mStyle.getHeight() + 1, minZ).setType(Material.GOLD_BLOCK);
				loc.getWorld().getBlockAt(minX + i, loc.getBlockY() + mStyle.getHeight() + 1, maxZ).setType(Material.GOLD_BLOCK);
				
				loc.getWorld().getBlockAt(minX, loc.getBlockY() + mStyle.getHeight() + 1, minZ + i).setType(Material.GOLD_BLOCK);
				loc.getWorld().getBlockAt(maxX, loc.getBlockY() + mStyle.getHeight() + 1, minZ + i).setType(Material.GOLD_BLOCK);
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
					loc.getWorld().getBlockAt(x, loc.getBlockY(), z).setType(Material.LAPIS_BLOCK);
					
					x += con.getModX();
					z += con.getModZ();
				}
				while(x >= minX - 1 && x <= maxX + 1 && z >= minZ - 1 && z <= maxZ + 1);
			}
			
			++xx;
			if(xx >= 8)
			{
				xx = 0;
				++zz;
			}
		}
	}
	
	private void start(Style existing) throws IllegalStateException
	{
		mStyle = existing;
		
		mLength = (mStyle.getPieceSize() + 2) * 8;
		mWidth = (mStyle.getPieceSize() + 2) * 2;
		
		Location loc = mPlayer.getLocation();
		
		for(int x = loc.getBlockX(); x < loc.getBlockX() + mLength; ++x)
		{
			for(int z = loc.getBlockZ(); z < loc.getBlockZ() + mWidth; ++z)
			{
				for(int y = loc.getBlockY(); y < loc.getBlockY() + mStyle.getHeight() + 2; ++y)
				{
					Block block = mPlayer.getWorld().getBlockAt(x, y, z);
					if(!block.isEmpty())
						throw new IllegalStateException("Design area is not empty.");
				}
			}
		}
		
		Bukkit.getPluginManager().registerEvents(this, DynamicMazePlugin.getInstance());
		
		drawBoard(loc);
		
		mSource = loc.clone();
		mPlayer.teleport(loc.add(0, 1, 0));
	}
	
	public void end()
	{
		mCurrentManagers.remove(mPlayer);

		if(mSource == null)
			return;
		
		// TODO: Save
		
		for(int y = mSource.getBlockY() + mStyle.getHeight() + 1; y >= mSource.getBlockY(); --y)
		{
			for(int x = mSource.getBlockX(); x < mSource.getBlockX() + mLength; ++x)
			{
				for(int z = mSource.getBlockZ(); z < mSource.getBlockZ() + mWidth; ++z)
					mPlayer.getWorld().getBlockAt(x, y, z).setType(Material.AIR);
			}
		}
		
		mSource = null;
		
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onBlockBreak(BlockBreakEvent event)
	{
		if(mSource == null || event.getBlock().getWorld() != mSource.getWorld())
			return;
		
		if(event.getBlock().getX() >= mSource.getBlockX() && event.getBlock().getX() < mSource.getBlockX() + mLength && 
			event.getBlock().getZ() >= mSource.getBlockZ() && event.getBlock().getZ() < mSource.getBlockZ() + mWidth &&
			event.getBlock().getY() >= mSource.getBlockY() && event.getBlock().getY() < mSource.getBlockY() + mStyle.getHeight() + 2)
		{
			if(event.getBlock().getY() == mSource.getBlockY())
				event.setCancelled(true);
			else if(event.getBlock().getY() == mSource.getBlockY() + mStyle.getHeight() + 1)
				event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onBlockPlace(BlockPlaceEvent event)
	{
		if(mSource == null || event.getBlock().getWorld() != mSource.getWorld())
			return;
		
		if(event.getBlock().getX() >= mSource.getBlockX() && event.getBlock().getX() < mSource.getBlockX() + mLength && 
			event.getBlock().getZ() >= mSource.getBlockZ() && event.getBlock().getZ() < mSource.getBlockZ() + mWidth &&
			event.getBlock().getY() >= mSource.getBlockY() && event.getBlock().getY() < mSource.getBlockY() + mStyle.getHeight() + 2)
		{
			if(event.getBlock().getY() == mSource.getBlockY())
				event.setCancelled(true);
			else if(event.getBlock().getY() == mSource.getBlockY() + mStyle.getHeight() + 1)
				event.setCancelled(true);
		}
	}
	
	
}
