package au.com.mineauz.dynmazes.flags;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import com.google.common.collect.HashBiMap;

public class FlagIO
{
	private static HashBiMap<String, Class<? extends Flag<?>>> mKnownTypes = HashBiMap.create();
	
	static
	{
		mKnownTypes.put("Algorithm", AlgorithmFlag.class);
		mKnownTypes.put("BlockType", BlockTypeFlag.class);
		mKnownTypes.put("Style", StyleFlag.class);
	}
	
	public static void saveFlags(Map<String, Flag<?>> flags, ConfigurationSection root)
	{
		for(Entry<String, Flag<?>> flag : flags.entrySet())
		{
			ConfigurationSection section = root.createSection(flag.getKey());
			String typeName;
			
			if(mKnownTypes.containsValue(flag.getValue().getClass()))
				typeName = mKnownTypes.inverse().get(flag.getValue().getClass());
			else
				typeName = flag.getValue().getClass().getName();
			
			section.set("~~", typeName);
			flag.getValue().save(section);
		}
	}
	
	@SuppressWarnings( "unchecked" )
	public static Map<String, Flag<?>> loadFlags(ConfigurationSection root) throws InvalidConfigurationException
	{
		HashMap<String, Flag<?>> flags = new HashMap<String, Flag<?>>();
		
		for(String key : root.getKeys(false))
		{
			if(!root.isConfigurationSection(key))
				continue;

			ConfigurationSection section = root.getConfigurationSection(key);
			
			String type = section.getString("~~");
			
			try
			{
				Class<? extends Flag<?>> clazz;
				if(mKnownTypes.containsKey(type))
					clazz = mKnownTypes.get(type);
				else
				{
					clazz = (Class<? extends Flag<?>>)Class.forName(type).asSubclass(Flag.class);
				}
				
				Flag<?> flag = clazz.newInstance();
				flag.read(section);
				flags.put(key, flag);
			}
			catch(ClassNotFoundException e)
			{
				System.err.println("Flag Load: Unknown class name " + type);
			}
			catch ( InstantiationException e )
			{
				e.printStackTrace();
			}
			catch ( IllegalAccessException e )
			{
				e.printStackTrace();
			}
		}
		
		return flags;
	}
}
