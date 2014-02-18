package au.com.mineauz.dynmazes.events;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.event.HandlerList;

import au.com.mineauz.dynmazes.INode;
import au.com.mineauz.dynmazes.Maze;

public class AsyncMazeGenerateEvent extends MazeEvent
{
	private static HandlerList handlers = new HandlerList();
	
	private Collection<INode> mNodes;
	private INode mRoot;
	
	public AsyncMazeGenerateEvent(Maze<?> maze, Collection<INode> nodes, INode root)
	{
		super(maze, true);
		mNodes = Collections.unmodifiableCollection(nodes);
		mRoot = root;
	}
	
	public Collection<INode> getNodes()
	{
		return mNodes;
	}
	
	public INode getRoot()
	{
		return mRoot;
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
