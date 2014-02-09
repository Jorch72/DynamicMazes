package au.com.mineauz.dynmazes.commands.design;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;

import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;

public class SaveDesignCommand implements ICommand
{

	@Override
	public String getName()
	{
		return null;
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
