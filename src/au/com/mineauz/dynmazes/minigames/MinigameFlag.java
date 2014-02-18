package au.com.mineauz.dynmazes.minigames;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.flags.Flag;
import au.com.mineauz.dynmazes.misc.BadArgumentException;

public class MinigameFlag extends Flag<Minigame>
{
	@Override
	public Minigame parse( Player sender, String[] args ) throws IllegalArgumentException, BadArgumentException
	{
		if(args.length != 1)
			throw new IllegalArgumentException("<minigame>");
		
		Minigame minigame = Minigames.plugin.mdata.getMinigame(args[0]);
		if(minigame == null)
			throw new BadArgumentException(0, args[0] + " is not a minigame");
		
		return minigame;
	}

	@Override
	public List<String> tabComplete( Player sender, String[] args )
	{
		if(args.length == 1)
			return Util.matchStrings(args[0], Minigames.plugin.mdata.getAllMinigames().keySet());
		return null;
	}

	@Override
	public void save( ConfigurationSection section )
	{
		section.set("value", value.getName());
	}

	@Override
	public void read( ConfigurationSection section ) throws InvalidConfigurationException
	{
		value = Minigames.plugin.mdata.getMinigame(section.getString("value"));
	}

	@Override
	public String getValueString()
	{
		if(value == null)
			return "*Invalid Value*";
		
		return value.getName();
	}
}
