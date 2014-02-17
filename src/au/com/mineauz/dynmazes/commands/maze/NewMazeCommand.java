package au.com.mineauz.dynmazes.commands.maze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.mineauz.dynmazes.MazeManager;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;
import au.com.mineauz.dynmazes.misc.BadArgumentException;
import au.com.mineauz.dynmazes.misc.ConfirmationPrompt;

public class NewMazeCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "new";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "dynamicmazes.command.maze.new";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <name> <type> <options>";
	}

	@Override
	public String getDescription()
	{
		return "Creates a new maze at your location";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length < 1)
			return false;
		
		String name = args[0];
		if(!Util.isNameOk(name))
			throw new BadArgumentException(0, "Name has invalid characters in it. Only letters, numbers, and _ may be used.");
		
		if(MazeManager.getMaze(name) != null)
			throw new BadArgumentException(0, "A maze by that name already exists.");
		
		if(args.length == 1 || !MazeManager.isMazeType(args[1]))
		{
			ArrayList<String> lines = new ArrayList<String>();
			lines.add(ChatColor.YELLOW + "Available maze types:");
			StringBuilder builder = new StringBuilder();
			boolean odd = true;
			for(String type : MazeManager.getMazeTypes())
			{
				if(builder.length() != 0)
				{
					builder.append(ChatColor.GRAY);
					builder.append(", ");
				}
				
				if(odd)
					builder.append(ChatColor.WHITE);
				else
					builder.append(ChatColor.GRAY);
				
				builder.append(type);
				odd = !odd;
			}
			
			lines.add(builder.toString());
			
			if(args.length == 1)
				throw new BadArgumentException(1, "Maze type missing.").addInfo(lines);
			else
				throw new BadArgumentException(1, args[1] + " is not a maze type.").addInfo(lines);
		}

		try
		{
			ConfirmationPrompt prompt = MazeManager.createMaze((Player)sender, name, args[1], Arrays.copyOfRange(args, 2, args.length));
			
			prompt.launch();
		}
		catch(BadArgumentException e)
		{
			throw new BadArgumentException(e.getArgument()+2, e.getMessage());
		}
		catch(NoSuchFieldException e)
		{
			sender.sendMessage(ChatColor.RED + "Usage: " + parent + label + " <name> " + args[1] + " " + e.getMessage());
		}

		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length == 2)
			return Util.matchStrings(args[1], MazeManager.getMazeTypes());
		
		return null;
	}

}
