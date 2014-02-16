package au.com.mineauz.dynmazes;

public interface CubeBased<T extends INode>
{
	public int getWidth();
	public int getLength();
	public int getHeight();
	
	public T getNodeAt(int x, int y, int z);
}
