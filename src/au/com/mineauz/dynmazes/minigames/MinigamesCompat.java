package au.com.mineauz.dynmazes.minigames;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.pauldavdesign.mineauz.minigames.events.EndMinigameEvent;
import com.pauldavdesign.mineauz.minigames.events.QuitMinigameEvent;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.mineauz.dynmazes.DynamicMazePlugin;
import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.MazeManager;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.flags.Flag;
import au.com.mineauz.dynmazes.flags.FlagIO;
import au.com.mineauz.dynmazes.misc.BlockLocation;
import au.com.mineauz.dynmazes.misc.Callback;

public class MinigamesCompat implements Listener
{
	private static MinigamesCompat instance;
	
	private MinigamesCompat()
	{
		
	}
	
	public static void initialize(DynamicMazePlugin plugin)
	{
		instance = new MinigamesCompat();
		Bukkit.getPluginManager().registerEvents(instance, plugin);
		
		FlagIO.addKnownType("Minigame", MinigameFlag.class);
		plugin.getMazeCommand().registerCommand(new ToMinigameMazeCommand());
	}
	
	public static Maze<?> getMazeFor(Minigame minigame)
	{
		for(String name : MazeManager.getMazeNames())
		{
			Maze<?> maze = MazeManager.getMaze(name);
			MinigameFlag flag = (MinigameFlag)maze.getFlag("minigame");
			if(flag == null)
				continue;
			
			if(flag.getValue() == minigame)
				return maze;
		}
		
		return null;
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	private void onMinigameEnd(EndMinigameEvent event)
	{
		if(event.isCancelled()) // Minigames 1.6.2 and earlier dont implement Cancellable
			return;
		
		if(event.getMinigame().getPlayers().size() == event.getWinners().size() && event.getMinigame().getPlayers().containsAll(event.getWinners()))
			minigameFinished(event.getMinigame());
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	private void onMinigameEnd(QuitMinigameEvent event)
	{
		if(event.isCancelled()) // Minigames 1.6.2 and earlier dont implement Cancellable
			return;
		
		if(event.getMinigame().getPlayers().size() == 1 && event.getMinigame().getPlayers().contains(event.getMinigamePlayer()))
			minigameFinished(event.getMinigame());
	}
	
	@SuppressWarnings( "unchecked" )
	private void minigameFinished(final Minigame minigame)
	{
		final Maze<?> maze = getMazeFor(minigame);
		if(maze != null && ((Flag<Boolean>)maze.getFlag("regen-on-end")).getValue())
		{
			minigame.setRegenerating(true);
			maze.generate(-1, new Callback()
			{
				@Override
				public void onFailure( Throwable exception )
				{
					exception.printStackTrace();
					minigame.setRegenerating(false);
				}
				
				@Override
				public void onComplete()
				{
					minigame.setRegenerating(false);
					BlockLocation loc = maze.getStartPoint();
					minigame.setStartLocation(new Location(maze.getWorld(), loc.getX(), loc.getY(), loc.getZ(), Util.toYaw(loc.getFace()), 0));
					addFinishSign(maze);
					addQuitSign(maze);
				}
			});
		}
	}
	
	public static void addFinishSign(Maze<?> maze)
	{
		BlockLocation loc = maze.getEndPoint();
		if(loc == null)
			return;
		
		Block block = maze.getWorld().getBlockAt(loc.getX(), loc.getY(), loc.getZ());
		block.setType(Material.SIGN_POST);
		
		BlockState state = block.getState();
		if(!(state instanceof Sign))
		{
			DynamicMazePlugin.getInstance().getLogger().warning("Could not place finish sign for " + maze.getName());
			return;
		}
		
		Sign sign = (Sign)state;
		sign.setLine(0, ChatColor.DARK_BLUE + "[Minigame]");
		sign.setLine(1, ChatColor.GREEN + "Finish");
		
		((org.bukkit.material.Sign)sign.getData()).setFacingDirection(loc.getFace());
		
		sign.update(true);
	}
	
	public static void addQuitSign(Maze<?> maze)
	{
		BlockLocation loc = maze.getStartPoint();
		if(loc == null)
			return;
		
		Block block = maze.getWorld().getBlockAt(loc.getX(), loc.getY(), loc.getZ());
		block.setType(Material.SIGN_POST);
		
		BlockState state = block.getState();
		if(!(state instanceof Sign))
		{
			DynamicMazePlugin.getInstance().getLogger().warning("Could not place finish sign for " + maze.getName());
			return;
		}
		
		Sign sign = (Sign)state;
		sign.setLine(0, ChatColor.DARK_BLUE + "[Minigame]");
		sign.setLine(1, ChatColor.GREEN + "Quit");
		
		((org.bukkit.material.Sign)sign.getData()).setFacingDirection(loc.getFace());
		
		sign.update(true);
	}
}
