package au.com.mineauz.dynmazes.events;

import org.bukkit.event.HandlerList;

import au.com.mineauz.dynmazes.Maze;

public class MazeCreateEvent extends MazeEvent
{
	private static HandlerList handlers = new HandlerList();
	
	public MazeCreateEvent(Maze<?> maze)
	{
		super(maze);
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}

}
