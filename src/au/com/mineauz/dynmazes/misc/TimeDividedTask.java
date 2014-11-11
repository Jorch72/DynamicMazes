package au.com.mineauz.dynmazes.misc;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public abstract class TimeDividedTask<T> extends Notifiable implements Runnable
{
	private Iterator<T> mIterator;
	private long mMaxIterationTime;
	private BukkitTask mTask;
	
	public TimeDividedTask(Iterable<T> collection, long maxTime, TimeUnit unit, Callback callback)
	{
		this(collection.iterator(), maxTime, unit, callback);
	}
	
	public TimeDividedTask(Iterator<T> iterator, long maxTime, TimeUnit unit, Callback callback)
	{
		super(callback);
		mIterator = iterator;
		mMaxIterationTime = unit.toNanos(maxTime);
	}
	
	public void start(Plugin plugin)
	{
		mTask = Bukkit.getScheduler().runTaskTimer(plugin, this, 1, 1);
	}
	
	protected abstract void process(T item);
	
	protected void done() {};
	
	@Override
	public final void run()
	{
		try
		{
			long time = System.nanoTime();
			while(mIterator.hasNext())
			{
				if(System.nanoTime() - time >= mMaxIterationTime)
					return;

				process(mIterator.next());
			}
			
			mTask.cancel();
			done();
			setCompleted();
		}
		catch(Throwable e)
		{
			mTask.cancel();
			setFailed(e);
		}
	}
}
