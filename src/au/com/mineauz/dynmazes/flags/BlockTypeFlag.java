package au.com.mineauz.dynmazes.flags;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.misc.BadArgumentException;
import au.com.mineauz.dynmazes.styles.StoredBlock;

public class BlockTypeFlag extends Flag<StoredBlock>
{
	private static HashMap<String, Material> mMaterials = new HashMap<String, Material>();
	
	static
	{
		for(Material mat : Material.values())
		{
			if(!mat.isBlock())
				continue;
			
			String name = mat.name().toLowerCase().replace("_", "");
			mMaterials.put(name, mat);
			mMaterials.put(mat.name().toLowerCase(), mat);
		}
	}
	
	@SuppressWarnings( "deprecation" )
	@Override
	public StoredBlock parse( Player sender, String[] args ) throws IllegalArgumentException, BadArgumentException
	{
		if(args.length != 1 && args.length != 2)
			throw new IllegalArgumentException("<material> [data]");
		
		Material mat = null;
		try
		{
			int id = Integer.parseInt(args[0]);
			mat = Material.getMaterial(id);
		}
		catch(NumberFormatException e)
		{
			mat = mMaterials.get(args[0].toLowerCase());
		}
		
		if(mat == null || !mat.isBlock())
			throw new BadArgumentException(0, "Unknown material " + args[0]);
		
		int data = 0;
		if(args.length == 2)
		{
			try
			{
				data = Integer.parseInt(args[1]);
				if(data < 0 || data > 15)
					throw new BadArgumentException(1, "Data is out of range 0-15");
			}
			catch(NumberFormatException e)
			{
				throw new BadArgumentException(1, "Data must be a value from 0 to 15");
			}
		}

		return new StoredBlock(mat, new MaterialData(mat, (byte)data));
	}

	@Override
	public List<String> tabComplete( Player sender, String[] args )
	{
		if(args.length == 1)
			return Util.matchStrings(args[0], mMaterials.keySet());
		
		return null;
	}

	@Override
	public void save( ConfigurationSection section )
	{
		value.save(section);
	}

	@Override
	public void read( ConfigurationSection section ) throws InvalidConfigurationException
	{
		value = new StoredBlock();
		value.read(section);
	}
	
	@Override
	public String getValueString()
	{
		return value.toString();
	}

}
