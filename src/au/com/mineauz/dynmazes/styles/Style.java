package au.com.mineauz.dynmazes.styles;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

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
		Validate.isTrue(size >= 4 && size <= 10);
		Validate.isTrue(height > 4);
		
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
	
	public String getName()
	{
		return mName;
	}
	
	public int getHeight()
	{
		return mHeight;
	}
	
	public void setHeight(int height)
	{
		Validate.isTrue(height >= 3 && height <= 40);
		mHeight = (byte)height;
		for(Piece piece : mPieces)
			piece.setHeight(height);
	}
	
	
	public int getPieceSize()
	{
		return mPieceSize;
	}
	
	public void save(File file)
	{
		try
		{
			YamlConfiguration out = new YamlConfiguration();
			out.set("name", mName);
			out.set("size", mPieceSize);
			out.set("height", mHeight);
			
			ConfigurationSection pieces = out.createSection("pieces");
			
			for(PieceType type : PieceType.values())
			{
				ConfigurationSection piece = pieces.createSection(type.name());
				mPieces[type.ordinal()].save(piece);
			}
			
			out.save(file);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean read(File file)
	{
		try
		{
			YamlConfiguration input = new YamlConfiguration();
			input.load(file);
			
			mName = input.getString("name");
			mPieceSize = (byte)input.getInt("size");
			mHeight = (byte)input.getInt("height");
			
			ConfigurationSection pieces = input.getConfigurationSection("pieces");
			
			for(PieceType type : PieceType.values())
			{
				mPieces[type.ordinal()] = new Piece(mPieceSize, mHeight);
				if(pieces.isConfigurationSection(type.name()))
					mPieces[type.ordinal()].read(pieces.getConfigurationSection(type.name()));
			}
			
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(InvalidConfigurationException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
}
