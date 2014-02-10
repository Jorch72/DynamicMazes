package au.com.mineauz.dynmazes.algorithm;

import java.util.Collection;
import java.util.Random;

import au.com.mineauz.dynmazes.INode;

public interface Algorithm
{
	public Collection<INode> generate(INode from);
	
	public void setSeed(long seed);
	
	public Random getRandom();
}
