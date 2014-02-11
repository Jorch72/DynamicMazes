package au.com.mineauz.dynmazes.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

import com.google.common.collect.HashMultimap;

import au.com.mineauz.dynmazes.styles.StoredBlock;

public class DependencySortThread extends Thread
{
	private final List<StoredBlock> mList;
	private final SortFuture mFuture;
	
	public DependencySortThread(List<StoredBlock> list)
	{
		mList = list;
		mFuture = new SortFuture();
	}
	
	public Future<List<StoredBlock>> getFuture()
	{
		return mFuture;
	}
	
	@Override
	public void run()
	{
		try
		{
			Collections.sort(mList, new ChunkGroupingComparator());
			HashMultimap<BlockVector, StoredBlock> dependencies = HashMultimap.create();
			ArrayList<StoredBlock> blocksToAdd = new ArrayList<StoredBlock>(mList.size());
			
			// Place non dependent blocks and list dependent ones
			for(int i = 0; i < mList.size(); ++i)
			{
				StoredBlock block = mList.get(i);
				
				BlockFace face = block.getDependantFace();
						
				if(face == BlockFace.SELF || (face == BlockFace.DOWN && block.getLocation().getBlockY() == 0))
					blocksToAdd.add(block);
				else
					dependencies.put(block.getLocationRelative(face), block);
			}

			// Insert dependent blocks after their dependencies
			for(int i = 0; i < blocksToAdd.size(); ++i)
			{
				Set<StoredBlock> dependents = dependencies.get(blocksToAdd.get(i).getLocation());
				if(dependents != null)
				{
					for(StoredBlock dependent : dependents)
						blocksToAdd.add(i+1, dependent);
					
					dependencies.removeAll(blocksToAdd.get(i));
				}
			}
			
			// Add any that are dependent on something that doesnt exist (or depedency loops)
			blocksToAdd.addAll(dependencies.values());
			
			mFuture.setCompleted(blocksToAdd);
		}
		catch(Throwable e)
		{
			mFuture.setCompleted(e);
		}
	}
	
	public class SortFuture implements Future<List<StoredBlock>>
	{
		private Object mWaitObj = new Object();
		private List<StoredBlock> mList = null;
		private Throwable mException;
		private boolean mIsComplete = false;
		
		@Override
		public boolean cancel( boolean mayInterruptIfRunning )
		{
			return false;
		}

		@Override
		public boolean isCancelled()
		{
			return false;
		}

		@Override
		public boolean isDone()
		{
			return mIsComplete;
		}

		@Override
		public List<StoredBlock> get() throws InterruptedException, ExecutionException
		{
			while(!mIsComplete)
			{
				synchronized(mWaitObj)
				{
					mWaitObj.wait();
				}
			}
			
			if(mException != null)
				throw new ExecutionException(mException);
			
			return mList;
		}

		@Override
		public List<StoredBlock> get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
		{
			if(!mIsComplete)
			{
				synchronized(mWaitObj)
				{
					mWaitObj.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
				}
			}
			
			if(mIsComplete)
			{
				if(mException != null)
					throw new ExecutionException(mException);
				
				return mList;
			}
			
			return null;
		}
		
		void setCompleted(List<StoredBlock> data)
		{
			mList = data;
			mIsComplete = true;
			synchronized(mWaitObj)
			{
				mWaitObj.notifyAll();
			}
		}
		
		void setCompleted(Throwable exception)
		{
			mException = exception;
			mIsComplete = true;
			synchronized(mWaitObj)
			{
				mWaitObj.notifyAll();
			}
		}
	}
}
