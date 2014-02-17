package au.com.mineauz.dynmazes.commands.maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.MazeManager;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;
import au.com.mineauz.dynmazes.flags.Flag;
import au.com.mineauz.dynmazes.misc.BadArgumentException;

public class InfoMazeCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "info";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "dynamicmazes.command.maze.info";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <maze>";
	}

	@Override
	public String getDescription()
	{
		return "Gets information on the specified maze";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length != 1)
			return false;
		
		Maze<?> maze = MazeManager.getMaze(args[0]);
		
		if(maze == null)
			throw new BadArgumentException(0, "There is no maze " + args[0]);
		
		ArrayList<String> flags = new ArrayList<String>();
		for(Entry<String, Flag<?>> flag : maze.getFlags().entrySet())
			flags.add(String.format("%s: %s", flag.getKey(), flag.getValue().getValueString()));
		
		Collections.sort(flags);
		
		sender.sendMessage(ChatColor.GRAY + "=============== Maze Info =============== ");
		sender.sendMessage(ChatColor.GOLD + "Name: " + ChatColor.YELLOW + maze.getName() + " " + ChatColor.GRAY + "[" + maze.getType() + "]");
		sender.sendMessage(ChatColor.GOLD + "Bounds: " + ChatColor.GRAY + String.format("(%d,%d,%d) -> (%d,%d,%d) [%s]", maze.getMinCorner().getBlockX(), maze.getMinCorner().getBlockY(), maze.getMinCorner().getBlockZ(), maze.getMaxCorner().getBlockX(), maze.getMaxCorner().getBlockY(), maze.getMaxCorner().getBlockZ(), maze.getWorld().getName()));
		for(String flag : flags)
		{
			int sep = flag.indexOf(':')+1;
			sender.sendMessage(ChatColor.YELLOW + flag.substring(0,sep) + ChatColor.WHITE + flag.substring(sep));
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent,	String label, String[] args )
	{
		if(args.length == 1)
			return Util.matchStrings(args[0], MazeManager.getMazeNames());
		return null;
	}

}
