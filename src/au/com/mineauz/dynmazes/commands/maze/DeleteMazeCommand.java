package au.com.mineauz.dynmazes.commands.maze;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.MazeManager;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;
import au.com.mineauz.dynmazes.misc.Callback;

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
		return null;
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
		{
			sender.sendMessage(ChatColor.RED + "No maze by the name " + args[0] + " exists");
			return true;
		}

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
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
