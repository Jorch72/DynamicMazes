package au.com.mineauz.dynmazes.algorithm;

import java.util.Collection;
import java.util.Random;

import org.bukkit.configuration.ConfigurationSection;

import au.com.mineauz.dynmazes.INode;

public interface Algorithm
{
	public Collection<INode> generate(INode from);
	
	public void setSeed(long seed);
	
	public Random getRandom();
	
	public String getType();
	
	public void save(ConfigurationSection section);
	public void read(ConfigurationSection section);
}
