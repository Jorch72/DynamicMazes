package au.com.mineauz.dynmazes.commands.maze;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.MazeManager;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;

public class GenerateMazeCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "generate";
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
		return label + " <name> [seed]";
	}

	@Override
	public String getDescription()
	{
		return "(Re)Generates the specified maze using the specified seed if any";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 1 && args.length != 2)
			return false;
		
		Maze<?> maze = MazeManager.getMaze(args[0]);
		if(maze == null)
		{
			sender.sendMessage(ChatColor.RED + "There is no maze by the name " + args[0]);
			return true;
		}
		
		if(maze.isGenerating() || maze.isDrawing())
		{
			sender.sendMessage(ChatColor.GOLD + "The maze is already being generated. Please wait until complete to regenerate it.");
			return true;
		}
		
		long seed = -1;
		if(args.length == 2)
		{
			try
			{
				seed = Long.parseLong(args[1]);
			}
			catch(NumberFormatException e)
			{
				seed = args[1].hashCode();
			}
		}
		
		maze.generate(seed);
		sender.sendMessage(ChatColor.GREEN + maze.getName() + " is being (re)generated");
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
