package au.com.mineauz.dynmazes.commands.design;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import au.com.mineauz.dynmazes.DesignManager;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;
import au.com.mineauz.dynmazes.styles.Style;

public class NewDesignCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "new";
	}

	@Override
	public String[] getAliases()
	{
		String[] test = new String[1];
		test.clone();
		
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
		return label + " [name] <size> <height>";
	}

	@Override
	public String getDescription()
	{
		return "Starts designing a new piece set";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 2 && args.length != 3)
			return false;
		
		String name = null;
		if(args.length == 3)
			name = args[0];
		
		int size;
		int height;
		
		try
		{
			size = Integer.parseInt(args[args.length - 2]);
			if(size < 4 || size > 10)
			{
				sender.sendMessage(ChatColor.RED + "Piece size is out of range. Allowed range is 4 - 10 inclusive.");
				return true;
			}
		}
		catch(NumberFormatException e)
		{
			sender.sendMessage(ChatColor.RED + "Piece size should be value from 4 - 10 inclusive.");
			return true;
		}
		
		try
		{
			height = Integer.parseInt(args[args.length - 1]);
			if(height < 4 || height > 100)
			{
				sender.sendMessage(ChatColor.RED + "Piece height is out of range. Allowed range is 4 - 100 inclusive.");
				return true;
			}
		}
		catch(NumberFormatException e)
		{
			sender.sendMessage(ChatColor.RED + "Piece height should be value from 4 - 100 inclusive.");
			return true;
		}
		
		try
		{
			DesignManager.beingDesigning((Player)sender, new Style(name, (byte)size, (byte)height));
			sender.sendMessage(ChatColor.GREEN + "You are now in design mode!"); 
			sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.YELLOW + "/dynmaze design save [<name>]" + ChatColor.WHITE + " to save changes.");
			sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.RED + "/dynmaze design end" + ChatColor.WHITE + " to end designing.");
		}
		catch(IllegalStateException e)
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
