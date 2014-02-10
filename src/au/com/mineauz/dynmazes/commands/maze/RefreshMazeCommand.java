package au.com.mineauz.dynmazes.commands.maze;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;

import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;

public class RefreshMazeCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "refresh";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"redraw"};
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return null;
	}

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return null;
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		return false;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label,
			String[] args )
	{
		return null;
	}

}
