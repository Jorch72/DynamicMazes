package au.com.mineauz.dynmazes.events;

import org.bukkit.event.HandlerList;

import au.com.mineauz.dynmazes.Maze;

public class MazeDeleteEvent extends MazeEvent
{
	private static HandlerList handlers = new HandlerList();
	
	public MazeDeleteEvent(Maze<?> maze)
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
