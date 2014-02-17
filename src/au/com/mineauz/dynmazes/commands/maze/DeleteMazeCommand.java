package au.com.mineauz.dynmazes.commands.maze;

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
import au.com.mineauz.dynmazes.misc.BadArgumentException;
import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.misc.ConfirmationPrompt;

public class DeleteMazeCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "delete";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"remove"};
	}

	@Override
	public String getPermission()
	{
		return "dynamicmazes.command.maze.delete";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <name>";
	}

	@Override
	public String getDescription()
	{
		return "Deletes a maze";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( final CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length != 1)
			return false;
		
		final Maze<?> maze = MazeManager.getMaze(args[0]);
		
		if(maze == null)
			throw new BadArgumentException(0, "There is no maze " + args[0]);
		
		ConfirmationPrompt prompt = new ConfirmationPrompt()
			.setPlayer((Player)sender)
			.setText("Delete " + maze.getName() + "?")
			.setCallback(new Callback()
			{
				@Override
				public void onFailure( Throwable exception )
				{
					sender.sendMessage(ChatColor.GOLD + "Maze delete cancelled");
				}
				
				@Override
				public void onComplete()
				{
					MazeManager.deleteMaze(maze, new Callback()
					{
						@Override
						public void onFailure( Throwable exception )
						{
							sender.sendMessage(ChatColor.RED + "An internal error occured while deleting that maze.");
							exception.printStackTrace();
						}
						
						@Override
						public void onComplete()
						{
							sender.sendMessage(ChatColor.GREEN + maze.getName() + " was deleted successfully");
						}
					});
					
					sender.sendMessage(ChatColor.GOLD + "The maze " + maze.getName() + " is now being deleted.");
				}
			});
		
		prompt.launch();

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
