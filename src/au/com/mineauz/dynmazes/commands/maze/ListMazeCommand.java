package au.com.mineauz.dynmazes.commands.maze;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.MazeManager;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;

public class ListMazeCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "list";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Lists all the mazes";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length != 0)
			return false;
		
		sender.sendMessage(ChatColor.GOLD + "All Mazes: ");
		for(String name : MazeManager.getMazeNames())
		{
			Maze<?> maze = MazeManager.getMaze(name);
			String text = String.format("&e(%d, %d, %d) &f-> &e(%d, %d, %d)", maze.getMinCorner().getBlockX(), maze.getMinCorner().getBlockY(), maze.getMinCorner().getBlockZ(), maze.getMaxCorner().getBlockX() - 1, maze.getMaxCorner().getBlockY() - 1, maze.getMaxCorner().getBlockZ() - 1);
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7 \u25B7 &c%s &7[%s] %s", name, maze.getType(), text))); 
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
