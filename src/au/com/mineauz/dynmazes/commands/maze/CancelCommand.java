package au.com.mineauz.dynmazes.commands.maze;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;
import au.com.mineauz.dynmazes.misc.ConfirmationPrompt;

public class CancelCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "cancel";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"reject", "decline", "no"};
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
		return "Cancels a prompt";
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
		
		ConfirmationPrompt.reject((Player)sender);
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
