package au.com.mineauz.dynmazes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import com.google.common.base.Throwables;

import au.com.mineauz.dynmazes.algorithm.Algorithm;

public class MazeManager
{
	private static HashMap<String, MazeDefinition> mMazeTypes = new HashMap<String, MazeDefinition>();
	
	public static void registerType(String name, Class<? extends MazeGenerator<?>> clazz)
	{
		Validate.isTrue(!name.contains(" "), "Cannot use spaces in type names");
		
		mMazeTypes.put(name.toLowerCase(), new MazeDefinition(name, clazz));
	}
	
	public static void registerAlgorithm(String name, Class<? extends Algorithm> clazz)
	{
		
	}
	
	public static MazeGenerator<?> createMaze(Player player, String name, String type, String[] args) throws IllegalArgumentException, NoSuchFieldException
	{
		MazeDefinition def = mMazeTypes.get(type.toLowerCase());
		if(def == null)
			throw new IllegalArgumentException("No maze type " + type);
		
		return def.newMaze(player, name, args);
	}
	
	public static Collection<String> getMazeTypes()
	{
		ArrayList<String> types = new ArrayList<String>(mMazeTypes.size());
		for(MazeDefinition def : mMazeTypes.values())
			types.add(def.getName());
		
		return types;
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface MazeCommand
	{
		public String command();
	}
	
	private static class MazeDefinition
	{
		private String mName;
		public MazeDefinition(String name, Class<? extends MazeGenerator<?>> clazz)
		{
			mName = name;
			for(Method method : clazz.getMethods())
			{
				if(!Modifier.isStatic(method.getModifiers()))
					continue;
				
				MazeCommand command = method.getAnnotation(MazeCommand.class);
				
				if(command != null)
				{
					if(command.command().equals("new"))
					{
						if(!MazeGenerator.class.isAssignableFrom(method.getReturnType()))
							throw new IllegalArgumentException(method.getName() + " must return an instance of MazeGenerator");
						
						if(method.getParameterTypes().length != 3 || 
							!method.getParameterTypes()[0].equals(Player.class) ||
							!method.getParameterTypes()[1].equals(String.class) ||
							!method.getParameterTypes()[2].equals(String[].class))
							throw new IllegalArgumentException(method.getName() + " does not match required signiture: (Player, String, String[])");
						
						mNewMaze = method;
						mNewMaze.setAccessible(true);
					}
				}
			}
			
			if(mNewMaze == null)
				throw new IllegalArgumentException("No new maze method found. Use @MazeCommand(\"new\") to denote this method.");
		}
		
		public String getName()
		{
			return mName;
		}
		
		private Method mNewMaze;
		public MazeGenerator<?> newMaze(Player player, String name, String[] args) throws IllegalArgumentException, NoSuchFieldException
		{
			try
			{
				return (MazeGenerator<?>)mNewMaze.invoke(null, player, name, args);
			}
			catch ( IllegalAccessException e )
			{
				throw new RuntimeException(e);
			}
			catch ( InvocationTargetException e )
			{
				Throwables.propagateIfPossible(e.getTargetException());
				Throwables.propagateIfInstanceOf(e.getTargetException(), NoSuchFieldException.class);
				throw new RuntimeException(e.getTargetException());
			}
		}
	}
}
