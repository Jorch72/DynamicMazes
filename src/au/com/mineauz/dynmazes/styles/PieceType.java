package au.com.mineauz.dynmazes.styles;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.bukkit.block.BlockFace;

public enum PieceType
{
	StraightNS(BlockFace.NORTH, BlockFace.SOUTH),
	StraightWE(BlockFace.WEST, BlockFace.EAST),
	CornerNW(BlockFace.WEST, BlockFace.NORTH),
	CornerNE(BlockFace.NORTH, BlockFace.EAST),
	CornerSW(BlockFace.SOUTH, BlockFace.WEST),
	CornerSE(BlockFace.SOUTH, BlockFace.EAST),
	TeeN(BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST),
	TeeE(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH),
	TeeS(BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST),
	TeeW(BlockFace.SOUTH, BlockFace.WEST, BlockFace.NORTH),
	Cross(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST),
	EndN(BlockFace.NORTH),
	EndE(BlockFace.EAST),
	EndS(BlockFace.SOUTH),
	EndW(BlockFace.WEST),
	StartN("Starting Location", BlockFace.NORTH),
	StartE("Starting Location", BlockFace.EAST),
	StartS("Starting Location", BlockFace.SOUTH),
	StartW("Starting Location", BlockFace.WEST),
	FinishN("Finish Location", BlockFace.NORTH),
	FinishE("Finish Location", BlockFace.EAST),
	FinishS("Finish Location", BlockFace.SOUTH),
	FinishW("Finish Location", BlockFace.WEST);
	
	public static PieceType[] StartPieces = new PieceType[] {StartN, StartE, StartS, StartW};
	public static PieceType[] FinishPieces = new PieceType[] {FinishN, FinishE, FinishS, FinishW};
	public static PieceType[] NormalPieces = new PieceType[] {StraightNS, StraightWE, CornerNW, CornerNE, CornerSW, CornerSE, TeeN, TeeE, TeeS, TeeW, Cross, EndN, EndE, EndS, EndW};
	
	private Collection<BlockFace> mConnections;
	private String mText;
	
	private PieceType(String text, BlockFace... connections)
	{
		mText = text;
		mConnections = Arrays.asList(connections);
	}
	
	private PieceType(BlockFace... connections)
	{
		mConnections = Arrays.asList(connections);
	}
	
	public Collection<BlockFace> getConnections()
	{
		return mConnections;
	}
	
	public Collection<BlockFace> getConnection(BlockFace from)
	{
		if(mConnections.size() <= 1)
			return Collections.emptyList();
		
		LinkedList<BlockFace> others = new LinkedList<BlockFace>();
		for(BlockFace face : mConnections)
		{
			if(!face.equals(from))
				others.add(face);
		}
		
		return others;
	}
	
	public boolean hasText()
	{
		return mText != null;
	}
	
	public String getText()
	{
		return mText;
	}
}
