import core.Block;
import core.Board;
import core.Piece;
import core.Window;
import processing.core.PApplet;

public class DebugWindow extends Window {

    static {
        widthInBlocks = 10;
        heightInBlocks = 40;
        cellDiameter = 20;
    }

    public void draw() {
        background(255);
        drawBackboard();
        Piece IPiece = new Piece(Piece.PieceColour.I, 2, 3);
        IPiece.placePieceOnBoard();
        Board.board.set(IPiece.boardX, IPiece.boardY, new Block(51, 51, 51));

        Piece OPiece = new Piece(Piece.PieceColour.O, 2, 7);
        OPiece.placePieceOnBoard();
        Board.board.getBlock(OPiece.boardX, OPiece.boardY).setColour(51, 51, 51);

        Piece JPiece = new Piece(Piece.PieceColour.J, 2, 13);
        JPiece.placePieceOnBoard();
        Board.board.getBlock(JPiece.boardX, JPiece.boardY).setColour(51, 51, 51);

        Piece LPiece = new Piece(Piece.PieceColour.L, 2, 18);
        LPiece.placePieceOnBoard();
        Board.board.getBlock(LPiece.boardX, LPiece.boardY).setColour(51, 51, 51);

        Piece SPiece = new Piece(Piece.PieceColour.S, 2, 23);
        SPiece.placePieceOnBoard();
        Board.board.getBlock(SPiece.boardX, SPiece.boardY).setColour(51, 51, 51);

        Piece TPiece = new Piece(Piece.PieceColour.T, 2, 28);
        TPiece.placePieceOnBoard();
        Board.board.getBlock(TPiece.boardX, TPiece.boardY).setColour(51, 51, 51);

        Piece ZPiece = new Piece(Piece.PieceColour.Z, 2, 33);
        ZPiece.placePieceOnBoard();
        Board.board.getBlock(ZPiece.boardX, ZPiece.boardY).setColour(51, 51, 51);

        drawPlacedBlocks();
        noLoop();
    }

    public static void main(String[] args) {
        PApplet.main("DebugWindow");
    }
}
