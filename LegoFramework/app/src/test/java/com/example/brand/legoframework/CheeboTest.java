package com.example.brand.legoframework;

import java.util.Scanner;

import org.petero.cuckoo.engine.chess.ChessParseError;
import org.petero.cuckoo.engine.chess.ComputerPlayer;
import org.petero.cuckoo.engine.chess.Move;
import org.petero.cuckoo.engine.chess.Piece;
import org.petero.cuckoo.engine.chess.Position;
import org.petero.cuckoo.engine.chess.TextIO;

public class CheeboTest {

	public static void main(String[] args) {
		ComputerPlayer c1 = new ComputerPlayer();
		ComputerPlayer c2 = new ComputerPlayer();

		CheeboGame cg = new CheeboGame(c1, c2);

		// System.out.println(cg.processFEN("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR"));

		try {
			cg.pos = TextIO.readFEN("r3k2r/pppppppp/1nbq1bn1/8/5PP1/1NBQ1BN1/PPPPP2P/R3K2R w KQkq -");
		} catch (ChessParseError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(cg.pos);

		Scanner s = new Scanner(System.in);
		while (true) {
			String moveStr = s.next();
			Move m = TextIO.uciStringToMove(moveStr);

			if (m == null)
				continue;

			moveStr = TextIO.moveToString(cg.pos, m, false);

			if (moveStr == null)
				continue;

			System.out.println(cg.moveToBluetoothString(moveStr));
			cg.processString(TextIO.moveToString(cg.pos, m, false));
			System.out.println(cg.pos);

		}
	}
}