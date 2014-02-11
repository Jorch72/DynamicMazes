package au.com.mineauz.dynmazes;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
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
	private static HashMap<String, Maze<?>> mMazes = new HashMap<String, Maze<?>>();
	
	private static HashMap<String, Constructor<? extends Algorithm>> mAlgorithms = new HashMap<String, Constructor<? extends Algorithm>>();
	
	private static File mFolder;
	
	public static void initialize(File folder)
	{
		mFolder = folder;
		mFolder.mkdirs();
	}
	
	public static void registerType(String name, Class<? extends Maze<?>> clazz)
	{
		Validate.isTrue(!name.contains(" "), "Cannot use spaces in type names");
		
		mMazeTypes.put(name.toLowerCase(), new MazeDefinition(name, clazz));
	}
	
	public static void registerAlgorithm(String name, Class<? extends Algorithm> clazz)
	{
		try
		{
			Constructor<? extends Algorithm> constructor = clazz.getConstructor();
			constructor.setAccessible(true);
			mAlgorithms.put(name.toLowerCase(), constructor);
		}
		catch(NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static Algorithm getAlgorithm(String name)
	{
		try
		{
			Constructor<? extends Algorithm> constructor = mAlgorithms.get(name.toLowerCase());
			return constructor.newInstance();
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static Maze<?> createMaze(Player player, String name, String type, String[] args) throws IllegalArgumentException, NoSuchFieldException
	{
		MazeDefinition def = mMazeTypes.get(type.toLowerCase());
		if(def == null)
			throw new IllegalArgumentException("No maze type " + type);
		
		Maze<?> maze = def.newMaze(player, name, args);
		mMazes.put(name.toLowerCase(), maze);
		
		saveMaze(maze);
		
		return maze;
	}
	
	public static Maze<?> createEmptyMaze(String type)
	{
		MazeDefinition def = mMazeTypes.get(type.toLowerCase());
		if(def == null)
			throw new IllegalArgumentException("No maze type " + type);
		
		Maze<?> maze = def.newBlankMaze();
		
		return maze;
	}
	
	public static Maze<?> getMaze(String name)
	{
		return mMazes.get(name.toLowerCase());
	}
	
	public static void deleteMaze(Maze<?> maze)
	{
		mMazes.remove(maze.getName().toLowerCase());
		new File(mFolder, maze.getName().toLowerCase() + ".yml").delete();
		maze.clear();
	}
	
	public static void saveMaze(Maze<?> maze)
	{
		maze.save(new File(mFolder, maze.getName().toLowerCase() + ".yml"));
	}
	
	public static Collection<String> getMazeTypes()
	{
		ArrayList<String> types = new ArrayList<String>(mMazeTypes.size());
		for(MazeDefinition def : mMazeTypes.values())
			types.add(def.getName());
		
		return types;
	}
	
	public static void loadMazes()
	{
		mMazes.clear();
		for(File file : mFolder.listFiles())
		{
			if(file.getName().endsWith(".yml"))
			{
				Maze<?> maze = Maze.read(file);
				if(maze != null)
					mMazes.put(maze.getName().toLowerCase(), maze);
			}
		}
	}
	
	public static void saveMazes()
	{
		for(Maze<?> maze : mMazes.values())
			saveMaze(maze);
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
		
		public MazeDefinition(String name, Class<? extends Maze<?>> clazz)
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
						if(!Maze.class.isAssignableFrom(method.getReturnType()))
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
			
			try
			{
				mBlankMaze = clazz.getDeclaredConstructor();
				mBlankMaze.setAccessible(true);
			}
			catch(NoSuchMethodException e)
			{
				throw new IllegalArgumentException("No read (default) constructor found.");
			}
			
			if(mNewMaze == null)
				throw new IllegalArgumentException("No new maze method found. Use @MazeCommand(\"new\") to denote this method.");
		}
		
		public String getName()
		{
			return mName;
		}
		
		private Method mNewMaze;
		public Maze<?> newMaze(Player player, String name, String[] args) throws IllegalArgumentException, NoSuchFieldException
		{
			try
			{
				return (Maze<?>)mNewMaze.invoke(null, player, name, args);
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
		
		private Constructor<? extends Maze<?>> mBlankMaze;
		public Maze<?> newBlankMaze()
		{
			try
			{
				return mBlankMaze.newInstance();
			}
			catch(Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}