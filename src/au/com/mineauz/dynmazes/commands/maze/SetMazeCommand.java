package au.com.mineauz.dynmazes.commands.maze;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.MazeManager;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;
import au.com.mineauz.dynmazes.flags.Flag;
import au.com.mineauz.dynmazes.flags.RequiresConfirmation;
import au.com.mineauz.dynmazes.misc.BadArgumentException;
import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.misc.ConfirmationPrompt;

public class SetMazeCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "set";
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
		return label + " <maze> <option> <value>";
	}

	@Override
	public String getDescription()
	{
		return "Sets an option for the maze";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean onCommand( final CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length <= 1)
			return false;
		
		final Maze<?> maze = MazeManager.getMaze(args[0]);
		if(maze == null)
		{
			sender.sendMessage(ChatColor.RED + "There is no maze by the name " + args[0]);
			return true;
		}
		
		final Flag<Object> flag = (Flag<Object>)maze.getFlag(args[1]);
		
		if(flag == null)
		{
			sender.sendMessage(ChatColor.RED + "Unknown option " + args[1] + ". Available options:");
			String options = "";
			
			for(String name : maze.getFlags().keySet())
			{
				if(!options.isEmpty())
					options += ", ";
				options += name;
			}
			
			sender.sendMessage(ChatColor.RED + options);
			return true;
		}
		
		Object result = null;
		try
		{
			result = flag.parse((Player)sender, Arrays.copyOfRange(args, 2, args.length));
		}
		catch(IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + String.format("Usage: %s%s %s %s %s", parent, label, args[0], args[1], e.getMessage()));
		}
		catch(BadArgumentException e)
		{
			String cmdString = ChatColor.GRAY + parent + label;
			for(int i = 0; i < args.length; ++i)
			{
				if(i == e.getArgument() + 2)
					cmdString += ChatColor.RED + args[i] + ChatColor.GRAY;
				else
					cmdString += args[i];
			}
			
			sender.sendMessage(ChatColor.RED + "Error in command: " + cmdString);
			sender.sendMessage(ChatColor.RED + " " + e.getMessage());
		}
		
		final Object lastValue = flag.getValue();
		
		if(flag instanceof RequiresConfirmation)
		{
			String prompt = ((RequiresConfirmation<Object>)flag).getConfirmationPrompt(result);
			if(prompt != null)
			{
				final Object fResult = result;
				final String flagName = args[1];
				
				new ConfirmationPrompt()
					.setText(prompt)
					.setPlayer((Player)sender)
					.setCallback(new Callback()
					{
						@Override
						public void onFailure( Throwable exception )
						{
							sender.sendMessage(ChatColor.GOLD + "Change cancelled");
						}
						
						@Override
						public void onComplete()
						{
							flag.setValue(fResult);
							maze.onFlagChanged(flagName, flag, lastValue);
							MazeManager.saveMaze(maze);
							sender.sendMessage(ChatColor.GREEN + flagName + " has been set to " + flag.getValueString());
						}
					})
					.launch();
				return true;
			}
		}
		
		flag.setValue(result);
		maze.onFlagChanged(args[1], flag, lastValue);
		MazeManager.saveMaze(maze);
		sender.sendMessage(ChatColor.GREEN + args[1] + " has been set to " + flag.getValueString());
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length == 1)
			return Util.matchStrings(args[0], MazeManager.getMazeNames());
		else if(args.length > 1)
		{
			Maze<?> maze = MazeManager.getMaze(args[0]);
			if(maze == null)
				return null;
			
			if(args.length == 2)
				return Util.matchStrings(args[1], maze.getFlags().keySet());
			else
			{
				@SuppressWarnings( "unchecked" )
				Flag<Object> flag = (Flag<Object>)maze.getFlag(args[1]);
				if(flag == null)
					return null;
				
				return flag.tabComplete((Player)sender, Arrays.copyOfRange(args, 2, args.length));
			}
		}
		return null;
	}

}
