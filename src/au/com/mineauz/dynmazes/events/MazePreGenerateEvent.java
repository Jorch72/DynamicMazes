package au.com.mineauz.dynmazes.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import au.com.mineauz.dynmazes.Maze;

public class MazePreGenerateEvent extends MazeEvent implements Cancellable
{
	private static HandlerList handlers = new HandlerList();
	
	private boolean mIsCancelled = false;
	
	public MazePreGenerateEvent(Maze<?> maze)
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

	@Override
	public boolean isCancelled()
	{
		return mIsCancelled;
	}

	@Override
	public void setCancelled( boolean cancelled )
	{
		mIsCancelled = cancelled;
	}

}
