package au.com.mineauz.dynmazes.flags;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import au.com.mineauz.dynmazes.MazeManager;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.algorithm.Algorithm;
import au.com.mineauz.dynmazes.misc.BadArgumentException;

public class AlgorithmFlag extends Flag<Algorithm>
{
	@Override
	public Algorithm parse( Player sender, String[] args ) throws IllegalArgumentException
	{
		if(args.length != 1)
			throw new IllegalArgumentException("<algorithm>");

		Algorithm algorithm = MazeManager.getAlgorithm(args[0]);
		if(algorithm == null)
			throw new BadArgumentException(0, "Unknown algorithm " + args[0]);
		
		return algorithm;
	}

	@Override
	public List<String> tabComplete( Player sender, String[] args )
	{
		if(args.length == 1)
			return Util.matchStrings(args[0], MazeManager.getAlgorithmTypes());
		
		return null;
	}

	@Override
	public void save( ConfigurationSection section )
	{
		section.set("type", value.getType());
		value.save(section);
	}
	
	@Override
	public void read( ConfigurationSection section ) throws InvalidConfigurationException
	{
		value = MazeManager.getAlgorithm(section.getString("type"));
		if(value == null)
			throw new InvalidConfigurationException("Unknown algorithm type " + section.getString("type"));
		else
			value.read(section);
	}
	
	@Override
	public String getValueString()
	{
		return value.getType();
	}
}
