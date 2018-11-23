package com.example.brand.legoframework;

import java.util.HashMap;

import org.petero.cuckoo.engine.chess.Game;
import org.petero.cuckoo.engine.chess.Move;
import org.petero.cuckoo.engine.chess.MoveGen;
import org.petero.cuckoo.engine.chess.Piece;
import org.petero.cuckoo.engine.chess.Player;
import org.petero.cuckoo.engine.chess.Position;
import org.petero.cuckoo.engine.chess.TextIO;
import org.petero.cuckoo.engine.chess.UndoInfo;

public class CheeboGame extends Game {
	// The position history stored as the number of occurrences of each state.
	private HashMap<Long, Integer> posOccurrences = new HashMap<Long, Integer>();

	// The position history for the 50 move rule
	private int moveCounter50;

	public CheeboGame(Player whitePlayer, Player blackPlayer) {
		super(whitePlayer, blackPlayer);
	}

	/**
	 * Process fen string if it is a valid next position.
	 *
	 * @param fen
	 *            the fen string to process
	 * @return true, if successful
	 */
	public boolean processFEN(String fen) {
		// Generate Legal Moves
		MoveGen.MoveList moves;
		if (!MoveGen.inCheck(pos))
			moves = new MoveGen().pseudoLegalMoves(pos);
		else
			moves = new MoveGen().checkEvasions(pos);
		MoveGen.removeIllegal(pos, moves);

		// Check if this is a valid FEN position
		String validFEN = null;
		for (int i = 0; i < moves.size; i++) {
			Position validPos = new Position(pos);
			validPos.makeMove(moves.m[i], new UndoInfo());

			if (TextIO.toFEN(validPos).split(" ")[0].equals(fen)) {
				pos = validPos;
				return true;
			}
		}
		return false;
	}

	/**
	 * Convert a generated move string to a bluetooth command string.
	 *
	 * @param moveString
	 *            the move string to parse
	 * @return the generated bluetooth command string
	 */
	public String moveToBluetoothString(String moveString) {
		Move m = TextIO.stringToMove(pos, moveString);
		String bluetoothString = "";

		// Indicate black or white move
		if (pos.whiteMove)
			bluetoothString += "W";
		else
			bluetoothString += "B";

		// Indicate remove for capture
		if (pos.getPiece(m.to) != Piece.EMPTY) {
			bluetoothString += "R" + Position.getX(m.to) + Position.getY(m.to);
			// Indicate moves for castling
		} else if (pos.getPiece(m.from) == Piece.BKING || pos.getPiece(m.from) == Piece.WKING) {
			if (m.from == Position.getSquare(4, 0) && m.to == Position.getSquare(6, 0))
				bluetoothString += "M4060M7050";
			else if (m.from == Position.getSquare(4, 0) && m.to == Position.getSquare(2, 0))
				bluetoothString += "M4020M0030";
			else if (m.from == Position.getSquare(4, 7) && m.to == Position.getSquare(6, 7))
				bluetoothString += "M4767M7757";
			else if (m.from == Position.getSquare(4, 7) && m.to == Position.getSquare(2, 7))
				bluetoothString += "M4727M0737";
			return bluetoothString;
			// Indicate remove for enPassant
		} else if (pos.getEpSquare() == m.to && (pos.getPiece(m.from) == Piece.WPAWN || pos.getPiece(m.from) == Piece.BPAWN)) {
			bluetoothString += "R" + Position.getX(m.to) + Position.getY(m.from);
		}

		// Indicate normal move
		bluetoothString += "M" + Position.getX(m.from) + Position.getY(m.from);
		bluetoothString += Position.getX(m.to) + "" + Position.getY(m.to);

		// Indicate promotions
		if (m.promoteTo != Piece.EMPTY) {
			bluetoothString += "P";
			switch (m.promoteTo) {
			case Piece.WQUEEN:
			case Piece.BQUEEN:
				bluetoothString += "q";
				break;
			case Piece.WROOK:
			case Piece.BROOK:
				bluetoothString += "r";
				break;
			case Piece.WBISHOP:
			case Piece.BBISHOP:
				bluetoothString += "b";
				break;
			case Piece.WKNIGHT:
			case Piece.BKNIGHT:
				bluetoothString += "n";
				break;
			default:
				break;
			}
		}
		return bluetoothString;
	}

	// make a move and calculate draw conditionss
	private void makeMove(Move m) {
		UndoInfo ui = new UndoInfo();
		pos.makeMove(m, ui);
		TextIO.fixupEPSquare(pos);
		while (currentMove < moveList.size()) {
			moveList.remove(currentMove);
			uiInfoList.remove(currentMove);
			drawOfferList.remove(currentMove);
		}
		moveList.add(m);
		uiInfoList.add(ui);
		drawOfferList.add(pendingDrawOffer);
		pendingDrawOffer = false;
		currentMove++;

		// Track draw by 3 fold repition
		long hash = pos.zobristHash();
		if (posOccurrences.containsKey(hash))
			posOccurrences.put(hash, posOccurrences.get(hash) + 1);
		else
			posOccurrences.put(hash, 1);

		// Track draw by 50 move rule
		int movePiece = pos.getPiece(m.from);
		if (movePiece == Piece.WPAWN || movePiece == Piece.BPAWN || isCaptureMove(m))
			moveCounter50 = 0;
		else
			moveCounter50++;
	}

	// Check if the given move captures a piece
	private boolean isCaptureMove(Move m) {
		if (pos.getPiece(m.to) == Piece.EMPTY) {
			int p = pos.getPiece(m.from);
			return (p == (pos.whiteMove ? Piece.WPAWN : Piece.BPAWN) && m.to == pos.getEpSquare());
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.petero.cuckoo.engine.chess.Game#processString(java.lang.String)
	 */
	@Override
	public boolean processString(String str) {
		if (str.equals("new")) {
			moveCounter50 = 0;
			posOccurrences.clear();
			handleCommand(str);
		}

		if (handleCommand(str))
			return true;

		Move m = TextIO.stringToMove(pos, str);
		if (m == null) {
			return false;
		}

		makeMove(m);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.petero.cuckoo.engine.chess.Game#getGameState()
	 */
	@Override
	public GameState getGameState() {
		if (moveCounter50 >= 50) {
			return GameState.DRAW_50;
		} else {
			for (int i : posOccurrences.values()) {
				if (i >= 3)
					return GameState.DRAW_REP;
			}
		}
		return super.getGameState();
	}
}
