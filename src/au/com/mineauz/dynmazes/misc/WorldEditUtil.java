package au.com.mineauz.dynmazes.misc;

import java.util.Arrays;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;

public class WorldEditUtil
{
	public static void saveRegion(Region region, ConfigurationSection section)
	{
		section.set("~~", region.getClass().getSimpleName());
		
		if(region instanceof CuboidRegion)
		{
			CuboidRegion cRegion = (CuboidRegion)region;
			saveVector(cRegion.getPos1(), "pos1", section);
			saveVector(cRegion.getPos2(), "pos2", section);
		}
		else if(region instanceof CylinderRegion)
		{
			CylinderRegion cRegion = (CylinderRegion)region;
			saveVector(cRegion.getCenter(), "center", section);
			saveVector2D(cRegion.getRadius(), "radius", section);
			
			if(cRegion.getMinimumY() != cRegion.getMaximumY())
			{
				section.set("minY", cRegion.getMinimumY());
				section.set("maxY", cRegion.getMaximumY());
			}
		}
		else if(region instanceof EllipsoidRegion)
		{
			EllipsoidRegion eRegion = (EllipsoidRegion)region;
			saveVector(eRegion.getCenter(), "center", section);
			saveVector(eRegion.getRadius(), "radius", section);
		}
		else if(region instanceof Polygonal2DRegion)
		{
			Polygonal2DRegion pRegion = (Polygonal2DRegion)region;
			if(pRegion.getMinimumY() != pRegion.getMaximumY())
			{
				section.set("minY", pRegion.getMinimumY());
				section.set("maxY", pRegion.getMaximumY());
			}
			
			section.set("points", pRegion.getPoints().size());
			int index = 0;
			for(BlockVector2D point : pRegion.getPoints())
			{
				saveVector2D(point, String.valueOf(index), section);
				++index;
			}
		}
	}
	
	public static void saveVector(Vector vector, String name, ConfigurationSection root)
	{
		ConfigurationSection section = root.createSection(name);
		section.set("x", vector.getX());
		section.set("y", vector.getY());
		section.set("z", vector.getZ());
	}
	
	public static Vector readVector(String name, ConfigurationSection root)
	{
		ConfigurationSection section = root.getConfigurationSection(name);
		return new Vector(section.getDouble("x"), section.getDouble("y"), section.getDouble("z"));
	}
	
	public static void saveVector2D(Vector2D vector, String name, ConfigurationSection root)
	{
		ConfigurationSection section = root.createSection(name);
		section.set("~~", vector.getClass().getSimpleName());
		section.set("x", vector.getX());
		section.set("z", vector.getZ());
	}
	
	public static Vector2D readVector2D(String name, ConfigurationSection root)
	{
		ConfigurationSection section = root.getConfigurationSection(name);
		String type = section.getString("~~");
		if(type.equals("BlockVector2D"))
			return new BlockVector2D(section.getDouble("x"), section.getDouble("z"));
		else
			return new Vector2D(section.getDouble("x"), section.getDouble("z"));
	}
	
	public static Region loadRegion(ConfigurationSection section) throws InvalidConfigurationException
	{
		String type = section.getString("~~");
		Region region = null;
		
		if(type.equals("CuboidRegion"))
			region = new CuboidRegion(readVector("pos1", section), readVector("pos2", section));
		else if(type.equals("CylinderRegion"))
		{
			int minY, maxY;
			Vector center = readVector("center", section);
			Vector2D radius = readVector2D("radius", section);
			
			if(section.isInt("minY"))
			{
				minY = section.getInt("minY");
				maxY = section.getInt("maxY");
				region = new CylinderRegion(null, center, radius, minY, maxY);
			}
			else
			{
				region = new CylinderRegion();
				((CylinderRegion)region).setCenter(center.toVector2D());
				((CylinderRegion)region).setRadius(radius);
			}
		}
		else if(type.equals("EllipsoidRegion"))
			return new EllipsoidRegion(null, readVector("center", section), readVector("radius", section));
		else if(type.equals("Polygonal2DRegion"))
		{
			int count = section.getInt("points");
			BlockVector2D[] points = new BlockVector2D[count];
			
			for(int i = 0; i < count; ++i)
				points[i] = (BlockVector2D)readVector2D(String.valueOf(i), section);
			
			if(section.isInt("minY"))
			{
				int minY, maxY;
				minY = section.getInt("minY");
				maxY = section.getInt("maxY");
				
				region = new Polygonal2DRegion(null, Arrays.asList(points), minY, maxY);
			}
			else
			{
				region = new Polygonal2DRegion();
				for(BlockVector2D point : points)
					((Polygonal2DRegion)region).addPoint(point);
			}
		}
		else
			throw new InvalidConfigurationException("Unknown region type " + type);
		
		return region;
	}
}
