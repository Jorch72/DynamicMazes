package au.com.mineauz.dynmazes;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;

public class WorldLockingHandler implements Listener
{
	@EventHandler(ignoreCancelled=true)
	private void onFluidFlow(BlockFromToEvent event)
	{
		Maze<?> maze = MazeManager.getMazeAt(event.getBlock().getLocation());
		Maze<?> maze2 = MazeManager.getMazeAt(event.getToBlock().getLocation());
		
		if(maze != null || maze2 != null)
			event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true)
	private void onBlockForm(BlockFormEvent event)
	{
		Maze<?> maze = MazeManager.getMazeAt(event.getBlock().getLocation());
		
		if(maze != null)
			event.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled=true)
	private void onBlockFade(BlockFadeEvent event)
	{
		Maze<?> maze = MazeManager.getMazeAt(event.getBlock().getLocation());
		
		if(maze != null)
			event.setCancelled(true);
	}
}
