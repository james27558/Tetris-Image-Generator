package core;

public class PotentialBoardState {

    Piece.PieceColour pieceColour;
    int pieceX;
    int pieceY;
    int rotateCount;
    int boardHeightDiff;
    float score;

    float bumpyness;

    /*
    This class holds information about the board after a piece was placed
     */
    PotentialBoardState(Piece.PieceColour pieceColour, int pieceX, int pieceY, int rotateCount, int boardHeightDiff) {
        this.pieceColour = pieceColour;
        this.pieceX = pieceX;
        this.pieceY = pieceY;
        this.rotateCount = rotateCount;
        this.boardHeightDiff = boardHeightDiff;

        this.bumpyness = 1;
        for (int i = 0; i < Board.board.width - 1; i++) {
            bumpyness += Math.abs(getColumnHeight(i) - getColumnHeight(i + 1));
        }

        // The closer the absolute value of the score is to 0, the more desirable it is
        score = (float) (Math.pow(Window.heightDiff - boardHeightDiff, 2) * Window.norm(pieceY, 0,
                Board.board.height)) * (bumpyness * 0.1f);
        if (pieceColour == Piece.PieceColour.I && (rotateCount == 1 || rotateCount == 3)) score *= 0.5f;
    }

    /**
     * @return The piece that is defined by this BoardState.
     */
    Piece getPlacedPiece() {
        Piece newPiece = new Piece(pieceColour, pieceX, pieceY);

        for (int i = 0; i < rotateCount; i++) {
            newPiece.rotatePieceClockwise();
        }

        return newPiece;
    }

    int getColumnHeight(int x) {
        for (int i = Board.board.visibleHeight - 1; i >= 0; i--) {
            if (Board.board.getBlock(x, i) != null) return i;
        }

        return 0;
    }

    public String toString() {
        return pieceColour.name() + " piece | X : " + pieceX + " | Y : " + pieceY + " | RC : " + rotateCount + " | " +
                "HD : " + boardHeightDiff + " | B : " + bumpyness + " | Score : " + score;
    }

}
