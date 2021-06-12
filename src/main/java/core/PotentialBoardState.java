package core;

public class PotentialBoardState {

    private static float heightDiffMultiplier;
    private static float bumpinessMultiplier;
    private static float goalDistScoreMultiplier;

    private Piece.PieceColour pieceColour;
    int pieceX;
    int pieceY;
    private int rotateCount;
    private int boardHeightDiff;
    float score;

    private float bumpiness;

    /*
    This class holds information about the board after a piece was placed
     */
    PotentialBoardState(Piece.PieceColour pieceColour, int pieceX, int pieceY, int rotateCount, int boardHeightDiff) {
        this.pieceColour = pieceColour;
        this.pieceX = pieceX;
        this.pieceY = pieceY;
        this.rotateCount = rotateCount;
        this.boardHeightDiff = boardHeightDiff;

        this.bumpiness = 0;
        // Calculate bumpiness
        for (int i = 0; i < Board.board.width - 1; i++) {
            bumpiness += Math.abs(Board.getColumnHeight(i) - Board.getColumnHeight(i + 1));
        }


        // Difference between the target height difference and the height difference of the board
        float heightDiffDifference = Math.abs(Window.heightDiff - boardHeightDiff);

        // Distance between the current goal X and this piece's X position
        // TODO: Perhaps change the goalDist to be the euclidean distance between the tower goal and this piece, see
        //  how it affects the shape of towers
        float goalDistScore = 0;
        if (goalDistScoreMultiplier != 0) {
            float goalDist = Math.abs((Board.currentTowerGoal.getBoardX() - pieceX));
            goalDistScore = (float) Math.exp(goalDist / 10f);

        }


        // The closer the score is to 0, the more desirable it is
        score =
                (heightDiffMultiplier * heightDiffDifference) + (bumpinessMultiplier * bumpiness * 2) + (goalDistScoreMultiplier * goalDistScore);

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

    // TODO: Move multiplier setting in Window to this class with presets for flat stacking and tower stacking

    static void setHeightDiffMultiplier(float heightDiffMultiplier) {
        PotentialBoardState.heightDiffMultiplier = heightDiffMultiplier;
    }

    static void setBumpinessMultiplier(float bumpinessMultiplier) {
        PotentialBoardState.bumpinessMultiplier = bumpinessMultiplier;
    }

    static void setGoalDistScoreMultiplier(float goalDistScoreMultiplier) {
        PotentialBoardState.goalDistScoreMultiplier = goalDistScoreMultiplier;
    }

    public String toString() {
        return pieceColour.name() + " piece | X : " + pieceX + " | Y : " + pieceY + " | RC : " + rotateCount + " | " +
                "HD : " + boardHeightDiff + " | B : " + bumpiness + " | Score : " + score;
    }

}
