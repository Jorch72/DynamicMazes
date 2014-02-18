package au.com.mineauz.dynmazes.commands.maze;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.MazeManager;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;
import au.com.mineauz.dynmazes.misc.BadArgumentException;
import au.com.mineauz.dynmazes.misc.Callback;

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
		return "dynamicmazes.command.maze.generate";
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
	public boolean onCommand( final CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length != 1 && args.length != 2)
			return false;
		
		final Maze<?> maze = MazeManager.getMaze(args[0]);
		if(maze == null)
			throw new BadArgumentException(0, "There is no maze " + args[0]);
		
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
		
		sender.sendMessage(ChatColor.GREEN + maze.getName() + " is being generated");
		
		maze.generate(seed, new Callback()
		{
			@Override
			public void onFailure( Throwable exception )
			{
				if(exception instanceof IllegalStateException)
					sender.sendMessage(ChatColor.RED + exception.getMessage());
				else
				{
					sender.sendMessage(ChatColor.RED + "An error occured while generating the maze");
					exception.printStackTrace();
				}
			}
			
			@Override
			public void onComplete()
			{
				sender.sendMessage(ChatColor.GREEN + maze.getName() + " was generated successfully");
			}
		});
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length == 1)
			return Util.matchStrings(args[0], MazeManager.getMazeNames());
		return null;
	}

}
