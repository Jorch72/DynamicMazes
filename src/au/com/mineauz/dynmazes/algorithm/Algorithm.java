package au.com.mineauz.dynmazes.algorithm;

import java.util.Collection;

import au.com.mineauz.dynmazes.INode;

public interface Algorithm
{
	public Collection<INode> generate(INode from);
}
