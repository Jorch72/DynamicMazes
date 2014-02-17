package au.com.mineauz.dynmazes.misc;

import org.bukkit.block.BlockFace;
import org.bukkit.util.BlockVector;

public class BlockLocation
{
	private int mX;
	private int mY;
	private int mZ;
	private BlockFace mFace;
	
	public BlockLocation(int x, int y, int z, BlockFace face)
	{
		mX = x;
		mY = y;
		mZ = z;
		mFace = face;
	}
	
	public BlockLocation(BlockVector vec, BlockFace face)
	{
		mX = vec.getBlockX();
		mY = vec.getBlockY();
		mZ = vec.getBlockZ();
		
		mFace = face;
	}
	
	public int getX()
	{
		return mX;
	}
	
	public int getY()
	{
		return mY;
	}
	
	public int getZ()
	{
		return mZ;
	}
	
	public BlockFace getFace()
	{
		return mFace;
	}
}
