package au.com.mineauz.dynmazes.misc;

import java.util.Comparator;

import org.bukkit.util.BlockVector;

import au.com.mineauz.dynmazes.styles.StoredBlock;

public class ChunkGroupingComparator implements Comparator<StoredBlock>
{
	@Override
	public int compare( StoredBlock o1, StoredBlock o2 )
	{
		BlockVector loc1 = o1.getLocation();
		BlockVector loc2 = o2.getLocation();
		
		int val = Integer.valueOf(loc1.getBlockX() >> 4).compareTo(loc2.getBlockX() >> 4);
		if(val != 0)
			return val;
		
		return Integer.valueOf(loc1.getBlockZ() >> 4).compareTo(loc2.getBlockZ() >> 4);
	}
}
