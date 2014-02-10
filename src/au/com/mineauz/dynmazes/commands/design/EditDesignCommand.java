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
import au.com.mineauz.dynmazes.styles.StyleManager;

public class EditDesignCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "edit";
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
		return label + " <name>";
	}

	@Override
	public String getDescription()
	{
		return "Edits an existing style";
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
		
		if(!StyleManager.styleExists(args[0]))
		{
			sender.sendMessage(ChatColor.RED + args[0] + " is not a style");
			return true;
		}
		
		Style style = StyleManager.getStyle(args[0]);
		
		try
		{
			DesignManager.beingDesigning((Player)sender, style);
			sender.sendMessage(ChatColor.GREEN + "You are now in design mode!"); 
			sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.YELLOW + parent + "save [<name>]" + ChatColor.WHITE + " to save changes.");
			sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.RED + parent + "end" + ChatColor.WHITE + " to end designing.");
		}
		catch(IllegalStateException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		return null;
	}

}
