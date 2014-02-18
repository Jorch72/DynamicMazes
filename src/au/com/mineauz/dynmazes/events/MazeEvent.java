package au.com.mineauz.dynmazes.events;

import org.bukkit.event.Event;
import au.com.mineauz.dynmazes.Maze;

public abstract class MazeEvent extends Event
{
	private Maze<?> mMaze;
	
	public MazeEvent(Maze<?> maze)
	{
		mMaze = maze;
	}
	
	public MazeEvent(Maze<?> maze, boolean async)
	{
		super(async);
		mMaze = maze;
	}
	
	public Maze<?> getMaze()
	{
		return mMaze;
	}

}
