public class PotentialBoardState {

    Piece.PieceColour pieceColour;
    int pieceX;
    int pieceY;
    int rotateCount;
    int boardHeightDiff;
    float score;

    /*
    This class holds information about the board after a piece was placed
     */
    PotentialBoardState(Piece.PieceColour pieceColour, int pieceX, int pieceY, int rotateCount, int boardHeightDiff) {
        this.pieceColour = pieceColour;
        this.pieceX = pieceX;
        this.pieceY = pieceY;
        this.rotateCount = rotateCount;
        this.boardHeightDiff = boardHeightDiff;

        // The closer the absolute value of the score is to 0, the more desirable it is
        score = Math.abs(Window.heightDiff - boardHeightDiff) * Window.norm(pieceY, Board.board.height, 0);
    }

    /**
     * @return The piece that is defined by this BoardState.
     */
    Piece producePiece() {
        Piece newPiece = new Piece(pieceColour, pieceX, pieceY);

        for (int i = 0; i < rotateCount; i++) {
            newPiece.rotatePieceClockwise();
        }

        newPiece.updateMaxMinValues();

        return newPiece;
    }

    public String toString() {
        return pieceColour.name() + " piece | X : " + pieceX + " | Y : " + pieceY + " | RotateCount : " + rotateCount + " | HeightDiff : " + boardHeightDiff + " | Score : " + score;
    }

}
