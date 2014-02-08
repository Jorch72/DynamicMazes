package au.com.mineauz.dynmazes.styles;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.Validate;

public class Style
{
	private String mName;
	private Piece[] mPieces;
	private byte mPieceSize;
	private byte mHeight;
	
	public Style()
	{
		mPieces = new Piece[PieceType.values().length];
	}
	
	public Style(String name, byte size, byte height)
	{
		Validate.isTrue(size == 4 || size == 6 || size == 8 || size == 10);
		Validate.isTrue(height > 1);
		
		mPieceSize = size;
		mName = name;
		mHeight = height;
		
		mPieces = new Piece[PieceType.values().length];
	}
	
	public void setPiece(PieceType type, Piece piece)
	{
		Validate.notNull(piece);
		mPieces[type.ordinal()] = piece;
	}
	
	public Piece getPiece(PieceType type)
	{
		return mPieces[type.ordinal()];
	}
	
	public void save(File file)
	{
		Validate.isTrue(file.isFile());
		
		try
		{
			DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
			
			out.writeUTF(mName);
			out.writeByte(mPieceSize);
			out.writeByte(mHeight);
			
			for(Piece piece : mPieces)
				piece.write(out);

			out.flush();
			out.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void read(File file)
	{
		Validate.isTrue(file.isFile());
		Validate.isTrue(file.exists());
		
		try
		{
			DataInputStream input = new DataInputStream(new FileInputStream(file));
			
			mName = input.readUTF();
			mPieceSize = input.readByte();
			mHeight = input.readByte();
			
			for(PieceType type : PieceType.values())
			{
				mPieces[type.ordinal()] = new Piece();
				mPieces[type.ordinal()].read(input);
			}
			
			input.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
