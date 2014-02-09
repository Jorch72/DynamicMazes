package au.com.mineauz.dynmazes.styles;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

public class Piece
{
	private byte mSize;
	private byte mHeight;
	
	private StoredBlock[] mBlocks;
	
	public Piece(byte size, byte height)
	{
		mSize = size;
		mHeight = height;
		
		mBlocks = new StoredBlock[mSize * mSize * mHeight];
	}
	
	public void setFrom(Location minCorner)
	{
		for(int y = 0; y < mHeight; ++y)
		{
			for(int x = 0; x < mSize; ++x)
			{
				for(int z = 0; z < mSize; ++z)
					mBlocks[x + z * (mSize) + y * (mSize * mSize)] = new StoredBlock(minCorner.getWorld().getBlockAt(minCorner.getBlockX() + x, minCorner.getBlockY() + y, minCorner.getBlockZ() + z).getState());
			}
		}
	}
	
	public void place(Location minCorner)
	{
		for(int y = 0; y < mHeight; ++y)
		{
			for(int x = 0; x < mSize; ++x)
			{
				for(int z = 0; z < mSize; ++z)
					mBlocks[x + z * (mSize) + y * (mSize * mSize)].apply(minCorner.getWorld().getBlockAt(minCorner.getBlockX() + x, minCorner.getBlockY() + y, minCorner.getBlockZ() + z));
			}
		}
	}
	
	public void save(ConfigurationSection parent)
	{
		for(int i = 0; i < mBlocks.length; ++i)
		{
			if(mBlocks[i].isAir())
				continue;
			
			ConfigurationSection block = parent.createSection(String.valueOf(i));
			mBlocks[i].save(block);
		}
	}
	
	public void read(ConfigurationSection parent)
	{
		Arrays.fill(mBlocks, new StoredBlock());
		for(String key : parent.getKeys(false))
		{
			int id = Integer.parseInt(key);
			
			mBlocks[id].read(parent.getConfigurationSection(key));
		}
	}
}
