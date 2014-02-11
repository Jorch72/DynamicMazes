package au.com.mineauz.dynmazes;

import java.util.Set;

import org.bukkit.util.BlockVector;

public interface INode
{
	public BlockVector toLocation();
	public INode[] getNeighbours();
	
	public INode getParent();
	public void setParent(INode node);
	
	public void addChild(INode node);
	public Set<INode> getChildren();
}
