package au.com.mineauz.dynmazes.styles;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.bukkit.Location;

public class Piece
{
	private PieceType mType;
	private byte mSize;
	private byte mHeight;
	
	private StoredBlock[] mBlocks;
	
	public Piece()
	{
		
	}
	
	public Piece(byte size, byte height, PieceType type)
	{
		mType = type;
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
					mBlocks[x + z * (mSize) + y * (mSize * mSize)] = new StoredBlock(minCorner.getWorld().getBlockAt(minCorner.getBlockX() + x, minCorner.getBlockX() + y, minCorner.getBlockX() + z).getState());
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
					mBlocks[x + z * (mSize) + y * (mSize * mSize)].apply(minCorner.getWorld().getBlockAt(minCorner.getBlockX() + x, minCorner.getBlockX() + y, minCorner.getBlockX() + z));
			}
		}
	}
	
	public void write(DataOutput output) throws IOException
	{
		output.writeByte(0); // Reserved for if 3D sets are made
		output.writeByte(mType.ordinal());
		output.writeByte(mSize);
		output.writeByte(mHeight);
		
		for(int i = 0; i < mBlocks.length; ++i)
			mBlocks[i].write(output);
	}
	
	public void read(DataInput input) throws IOException
	{
		input.readByte(); // Reserved
		mType = PieceType.values()[input.readByte()];
		mSize = input.readByte();
		mHeight = input.readByte();
		
		int blocks = mSize * mSize * mHeight;
		mBlocks = new StoredBlock[blocks];
		for(int i = 0; i < blocks; ++i)
		{
			mBlocks[i] = new StoredBlock();
			mBlocks[i].read(input);
		}
	}
}
