package au.com.mineauz.dynmazes.commands.design;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.mineauz.dynmazes.DesignManager;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;

public class SaveDesignCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "save";
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
		return label + " [<name>]";
	}

	@Override
	public String getDescription()
	{
		return "Saves the current edited style. If name is specified it saves it as that style name, otherwise it overrides the existing one.";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length > 1)
			return false;
		
		String name = null;
		if(args.length == 1)
			name = args[0];
		
		DesignManager manager = DesignManager.getManager((Player)sender);
		if(manager == null)
		{
			sender.sendMessage(ChatColor.RED + "You are not designing anything currently.");
			return true;
		}
		
		try
		{
			manager.save(name);
			sender.sendMessage(ChatColor.GREEN + "Style saved as " + manager.getStyle().getName());
		}
		catch(IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label,
			String[] args )
	{
		return null;
	}

}
