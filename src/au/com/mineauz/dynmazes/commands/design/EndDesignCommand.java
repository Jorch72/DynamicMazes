package au.com.mineauz.dynmazes.commands.design;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.mineauz.dynmazes.DesignManager;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;

public class EndDesignCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "end";
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
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Ends the current design session without saving";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 0)
			return false;
		
		DesignManager manager = DesignManager.getManager((Player)sender);
		
		if(manager == null)
			sender.sendMessage(ChatColor.RED + "You are not designing anything currently.");
		else
		{
			manager.end();
			sender.sendMessage(ChatColor.GOLD + "You have exited design mode.");
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}
