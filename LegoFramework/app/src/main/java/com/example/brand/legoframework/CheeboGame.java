package com.example.brand.legoframework;

import org.petero.cuckoo.engine.chess.Game;
import org.petero.cuckoo.engine.chess.Player;

public class CheeboGame extends Game {

	public CheeboGame(Player whitePlayer, Player blackPlayer) {
		super(whitePlayer, blackPlayer);
	}
	
	public void newGame ()
	{
		handleCommand("new");
	}
	
	

}
