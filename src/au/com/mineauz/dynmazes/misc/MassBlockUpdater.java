package au.com.mineauz.dynmazes.misc;

import java.util.Collection;

import me.desht.dhutils.nms.NMSHelper;
import me.desht.dhutils.nms.fallback.NMSHandler;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.material.MaterialData;
import org.bukkit.util.BlockVector;

import au.com.mineauz.dynmazes.DynamicMazePlugin;
import au.com.mineauz.dynmazes.misc.nms.FallbackMassBlockUpdater;
import au.com.mineauz.dynmazes.misc.nms.SpigotMassBlockUpdater;
import au.com.mineauz.dynmazes.styles.StoredBlock;

/**
 * The goal of the mass block updater is to do the most amount of updates possible, with the smallest amount of lag possible
 */
public abstract class MassBlockUpdater
{
	public static void applyAll(World world, Collection<StoredBlock> blocks)
	{
		MassBlockUpdater updater = create();
		for(StoredBlock block : blocks)
		{
			if (block.getLocation() != null)
				updater.setBlock(world, block.getLocation(), block);
		}
	}
	
	private static Boolean mUseNMS;
	public static MassBlockUpdater create()
	{
		if (mUseNMS == null)
		{
			try
			{
				mUseNMS = !(NMSHelper.getNMS() instanceof NMSHandler);
			}
			catch(Throwable e)
			{
				mUseNMS = false;
			}
			
			if (mUseNMS)
				DynamicMazePlugin.getInstance().getLogger().info("Using NMS based mass block updater");
			else
				DynamicMazePlugin.getInstance().getLogger().info("Using fallback mass block updater");
		}
		
		if (mUseNMS)
			return new SpigotMassBlockUpdater();
		else
			return new FallbackMassBlockUpdater();
	}
	
	private Callback mCallback;
	
	public void setCompletionCallback(Callback callback)
	{
		mCallback = callback;
	}
	
	public void setBlock(World world, int x, int y, int z, MaterialData material)
	{
		setBlock(world, x, y, z, new StoredBlock(material.getItemType(), material));
	}
	
	public abstract void setBlock(World world, int x, int y, int z, StoredBlock material);
	
	public void setBlock(World world, BlockVector pos, MaterialData material)
	{
		setBlock(world, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), material);
	}
	
	public void setBlock(World world, BlockVector pos, StoredBlock material)
	{
		setBlock(world, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), material);
	}
	
	public void setBlock(Location location, MaterialData material)
	{
		setBlock(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), material);
	}
	
	public void setBlock(Location location, StoredBlock material)
	{
		setBlock(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), material);
	}
	
	public abstract void execute();
	
	protected void setFailed(Throwable e)
	{
		if (mCallback != null)
			mCallback.onFailure(e);
		else
			e.printStackTrace();
	}
	
	protected void setCompleted()
	{
		if (mCallback != null)
			mCallback.onComplete();
	}
}
