package au.com.mineauz.dynmazes;

import java.util.Collection;
import org.bukkit.Bukkit;
import au.com.mineauz.dynmazes.misc.Callback;
import au.com.mineauz.dynmazes.misc.MassBlockUpdater;
import au.com.mineauz.dynmazes.misc.Notifiable;

public class DrawingTask<T extends INode> extends Notifiable
{
	private Maze<T> mMaze;
	private Collection<T> mNodes;
	
	private MassBlockUpdater mUpdater;
	
	public DrawingTask(Maze<T> maze, Collection<T> allNodes, int floorLevel, Callback callback)
	{
		super(callback);
		
		mMaze = maze;
		mNodes = allNodes;
		
		mUpdater = MassBlockUpdater.create();
	}
	
	public void start()
	{
		Bukkit.getScheduler().runTaskAsynchronously(DynamicMazePlugin.getInstance(), new Runnable()
		{
			@Override
			public void run()
			{
				for(T node : mNodes)
					mMaze.placeNode(node, mUpdater);
				
				mMaze.placeOther(mUpdater);
				
				doPlace();
			}
		});
	}
	
	private void doPlace()
	{
		Bukkit.getScheduler().runTask(DynamicMazePlugin.getInstance(), new Runnable()
		{
			@Override
			public void run()
			{
				mUpdater.setCompletionCallback(new Callback()
				{
					@Override
					public void onFailure( Throwable exception )
					{
						setFailed(exception);
					}
					
					@Override
					public void onComplete()
					{
						setCompleted();
					}
				});
				
				mUpdater.execute();
			}
		});
	}
}
