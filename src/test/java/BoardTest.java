import core.Board;
import core.Piece;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;


class BoardTest {


    @Test
    void test_GeneratingPieceSequence_HasAllPieces_And_Has7Pieces() {
        ArrayList<Piece.PieceColour> pieceList = Board.generatePieceList();

        boolean areAllPiecesPresent =
                pieceList.contains(Piece.PieceColour.O) && pieceList.contains(Piece.PieceColour.I) &&
                        pieceList.contains(Piece.PieceColour.J) && pieceList.contains(Piece.PieceColour.L) &&
                        pieceList.contains(Piece.PieceColour.S) && pieceList.contains(Piece.PieceColour.T) &&
                        pieceList.contains(Piece.PieceColour.Z);

        boolean correctLength = pieceList.size() == 7;

        assertTrue(areAllPiecesPresent && correctLength);

    }
}