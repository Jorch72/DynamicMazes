package au.com.mineauz.dynmazes.flags;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import au.com.mineauz.dynmazes.misc.BadArgumentException;
import au.com.mineauz.dynmazes.styles.Style;
import au.com.mineauz.dynmazes.styles.StyleManager;

public class StyleFlag extends Flag<Style> implements RequiresConfirmation<Style>
{
	@Override
	public String getConfirmationPrompt(Style newStyle)
	{
		if(newStyle.getPieceSize() > value.getPieceSize())
			return "The style " + newStyle.getName() + " is has a larger piece size than the existing style (" + value.getName() + "). This will expand the maze. Do you wish to continue?";
		else if(newStyle.getPieceSize() < value.getPieceSize())
			return "The style " + newStyle.getName() + " is has a smaller piece size than the existing style (" + value.getName() + "). This will shrink the maze. Do you wish to continue?";
		
		return null;
	}

	@Override
	public Style parse( Player sender, String[] args ) throws IllegalArgumentException, BadArgumentException
	{
		if(args.length != 1)
			throw new IllegalArgumentException("<style>");
		
		Style style = StyleManager.getStyle(args[0]);
		if(style == null)
			throw new BadArgumentException(0, "Unknown style " + args[0]);
		
		return style;
	}

	@Override
	public List<String> tabComplete( Player sender, String[] args )
	{
		return null;
	}

	@Override
	public void save( ConfigurationSection section )
	{
		section.set("name", value.getName());
	}

	@Override
	public void read( ConfigurationSection section ) throws InvalidConfigurationException
	{
		value = StyleManager.getStyle(section.getString("name"));
		if(value == null)
			throw new InvalidConfigurationException("Unknown style " + section.getString("name"));
	}

	@Override
	public String getValueString()
	{
		return value.getName();
	}

}
