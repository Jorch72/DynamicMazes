package au.com.mineauz.dynmazes.commands.design;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.mineauz.dynmazes.DesignManager;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;

public class ChangeHeightDesignCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "height";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"setheight"};
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <height>";
	}

	@Override
	public String getDescription()
	{
		return "Changes the height of the current piece set";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length != 1)
			return false;
		
		DesignManager manager = DesignManager.getManager((Player)sender);
		
		int height = 0;
		if(manager == null)
		{
			sender.sendMessage(ChatColor.RED + "You are not designing anything currently.");
			return true;
		}
				
		try
		{
			height = Integer.parseInt(args[0]);
			if(height < 3 || height > 40)
			{
				sender.sendMessage(ChatColor.RED + "Height is out of range. Valid range is 3 - 40 inclusive.");
				return true;
			}
		}
		catch(NumberFormatException e)
		{
			sender.sendMessage(ChatColor.RED + "Height must be a number between 3 and 40 inclusive");
			return true;
		}
		
		manager.setHeight(height);
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
