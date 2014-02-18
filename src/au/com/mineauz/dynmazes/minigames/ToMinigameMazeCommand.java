package au.com.mineauz.dynmazes.minigames;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import com.pauldavdesign.mineauz.minigames.Minigames;
import com.pauldavdesign.mineauz.minigames.gametypes.MinigameType;
import com.pauldavdesign.mineauz.minigames.minigame.Minigame;

import au.com.mineauz.dynmazes.Maze;
import au.com.mineauz.dynmazes.MazeManager;
import au.com.mineauz.dynmazes.Util;
import au.com.mineauz.dynmazes.commands.CommandSenderType;
import au.com.mineauz.dynmazes.commands.ICommand;
import au.com.mineauz.dynmazes.flags.BooleanFlag;
import au.com.mineauz.dynmazes.misc.BadArgumentException;
import au.com.mineauz.dynmazes.misc.BlockLocation;

public class ToMinigameMazeCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "toMinigame";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"makeMinigame"};
	}

	@Override
	public String getPermission()
	{
		return "minigame.create";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <maze> <minigameName>";
	}

	@Override
	public String getDescription()
	{
		return "Converts a maze into a minigame";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player);
	}

	@Override
	public boolean onCommand( CommandSender sender, String parent, String label, String[] args ) throws BadArgumentException
	{
		if(args.length != 2)
			return false;
		
		Maze<?> maze = MazeManager.getMaze(args[0]);
		if(maze == null)
			throw new BadArgumentException(0, args[0] + " is not a maze");
		
		if(Minigames.plugin.mdata.getMinigame(args[1]) != null)
			throw new BadArgumentException(1, args[1] + " already exists");
		
		if(maze.getFlag("minigame") != null)
			throw new BadArgumentException(0, maze.getName() + " is already a minigame");
		
		Minigame game = new Minigame(args[1]);
		game.setScoreType("Custom");
		game.setType(MinigameType.SINGLEPLAYER);
				
		Minigames.plugin.mdata.addMinigame(game);
		game.saveMinigame();
		
		maze.addFlag("minigame", new MinigameFlag());
		maze.addFlag("regen-on-end", new BooleanFlag());
		
		((MinigameFlag)maze.getFlag("minigame")).setValue(game);
		((BooleanFlag)maze.getFlag("regen-on-end")).setValue(true);
		
		if(maze.hasFlag("gen-start-room"))
			((BooleanFlag)maze.getFlag("gen-start-room")).setValue(true);
		if(maze.hasFlag("gen-finish-room"))
			((BooleanFlag)maze.getFlag("gen-finish-room")).setValue(true);
		
		BlockLocation loc = maze.getStartPoint();
		if(loc != null)
			game.addStartLocation(new Location(maze.getWorld(), loc.getX(), loc.getY(), loc.getZ(), Util.toYaw(loc.getFace()), 0));
		else
			game.addStartLocation(new Location(maze.getWorld(), maze.getMinCorner().getX(), maze.getMinCorner().getY(), maze.getMinCorner().getZ()));
		
		MinigamesCompat.addFinishSign(maze);
		MinigamesCompat.addQuitSign(maze);
		
		// TODO: Place stuff in maze as needed
		
		MazeManager.saveMaze(maze);
		sender.sendMessage(ChatColor.GREEN + "This maze is now linked to " + game.getName());
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String parent, String label, String[] args )
	{
		if(args.length == 1)
			return Util.matchStrings(args[0], MazeManager.getMazeNames());
		else if(args.length == 2)
			return Util.matchStrings(args[1], Minigames.plugin.mdata.getAllMinigames().keySet());
		
		return null;
	}

}
