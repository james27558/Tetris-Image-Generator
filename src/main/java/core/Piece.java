package core;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Represent a piece on the board. core.Piece position is defined from a center and block offsets.
 */
public class Piece {

    static final RealMatrix counterClockwiseRotationMatrix =
            MatrixUtils.createRealMatrix(new double[][]{{0, 1}, {-1, 0}});

    static final RealMatrix clockwiseRotationMatrix = MatrixUtils.createRealMatrix(new double[][]{{0, -1}, {1, 0}});

    // Bound Offsets
    int maxXOffset;
    int minXOffset;
    int maxYOffset;
    int minYOffset;

    PieceColour pieceColour;
    RealMatrix blockOffsets;

    // Position
    public int boardX;
    public int boardY;

    // Control
    boolean stopped;

    /**
     * Initialises the core.Piece with a preset set of block offsets using the default set of pieces at a given position
     * on the board. The core.Piece will be constructed with the deafult rotation for the given core.Piece
     *
     * @param pieceColour core.Piece type to inherit default block positions from
     * @param boardX
     * @param boardY
     */
    public Piece(PieceColour pieceColour, int boardX, int boardY) {
        this.pieceColour = pieceColour;
        this.boardX = boardX;
        this.boardY = boardY;
        this.stopped = false;

        blockOffsets = DefaultPieceRotations.values()[pieceColour.ordinal()].blockOffsets;

        updateMaxMinValues();
    }

    /**
     * Check if a given index is out of bounds of the core.Board
     *
     * @param x
     * @param y
     * @return
     */
    private static boolean isIndexOutOfBoardBounds(int x, int y) {
        return (x < 0 || x > Board.board.width - 1 || y < 0 || y > Board.board.height - 1);
    }

    /**
     * Rotates the core.Piece clockwise. Doesn't do any error checking or check if the rotation would make the core
     * .Piece
     * overlap with existing Pieces on the board. Updates the bound offsets afterwards
     */
    void rotatePieceClockwise() {
        // The O core.Piece doesn't rotate
        if (pieceColour == PieceColour.O) return;

        // Rotate the piece
        // The I core.Piece must be handled seperately, otherwise rotate using matrix multiplication
        if (pieceColour == PieceColour.I) {
            IRotations currentRotation = IRotations.identifyRotationFromMatrix(blockOffsets);
            blockOffsets = currentRotation.getClockwiseRotation().blockOffsets;
        } else {
            blockOffsets = clockwiseRotationMatrix.multiply(blockOffsets);
        }

        // As the piece has been rotated, the min and max values must be updated
        updateMaxMinValues();
    }

    /**
     * Rotates the core.Piece counterclockwise. Doesn't do any error checking or check if the rotation would make the core.Piece
     * overlap with existing Pieces on the board. Updates the bound offsets afterwards
     */
    void rotatePieceCounterClockwise() {
        if (pieceColour == PieceColour.O) return;

        // Rotate the piece
        // The I core.Piece must be handled seperately, otherwise rotate using matrix multiplication
        if (pieceColour == PieceColour.I) {
            IRotations currentRotation = IRotations.identifyRotationFromMatrix(blockOffsets);
            blockOffsets = currentRotation.getCounterClockwiseRotation().blockOffsets;
        } else {
            blockOffsets = counterClockwiseRotationMatrix.multiply(blockOffsets);
        }

        // As the piece has been rotated, the min and max values must be updated
        updateMaxMinValues();
    }


    /**
     * Hard drops the current core.Piece onto the board
     *
     * @return true if the piece can't lower and has stopped, false if the piece has lowered
     */
    void hardDropPiece() {
        boolean shouldStop = false;

        while (!shouldStop) {
            if (wouldNewCenterBeValid(boardX, boardY - 1)) {
                boardY--;
            } else {
                shouldStop = true;
            }
        }
    }

    /**
     * Try to kick the piece left, if it isn't succesful, try to kick it right
     */
    private void tryToWallKickPiece() {
        boolean wasPieceAbleToMoveLeft = movePieceLeft();

        if (!wasPieceAbleToMoveLeft) {
            movePieceRight();
        }
    }

    /**
     * Try to floor kick the piece
     */
    private void tryToFloorKickPiece() {
        if (!wouldNewCenterBeValid(boardX, boardY - 1)) boardY--;
    }

    /**
     * Checks if the new piece center is valid. If the new center would cause the blocks of this piece to intersect
     * with other existing blocks on the board, or the blocks would be out of bounds of the board then it is invalid
     *
     * @param newCenterX Column of the new center X position on the board
     * @param newCenterY Row of the new center Y position on the board
     * @return Whether the new center would be a valid position for the piece
     */
    boolean wouldNewCenterBeValid(int newCenterX, int newCenterY) {
        for (int i = 0; i < 4; i++) {
            int blockX = (int) blockOffsets.getEntry(0, i) + newCenterX;
            int blockY = (int) blockOffsets.getEntry(1, i) + newCenterY;

            // If the new space is occupied by an already present block, then the new center is not valid
            if (isIndexOutOfBoardBounds(blockX, blockY) || Board.board.getBlock(blockX, blockY) != null) return false;
        }

        // All new positions are empty so the new center is valid
        return true;
    }

    /**
     * Moves the core.Piece left by one block. The move wont occur if the core.Piece isn't able to move i.e. it is
     * blocked by a
     * block or the edge of the core.Board
     * @return true if the core.Piece has successfully moved, false if the core.Piece was blocked
     */
    boolean movePieceLeft() {
        // Check if the new blocks position overlaps with existing blocks, if not, move the core.Piece
        if (wouldNewCenterBeValid(boardX - 1, boardY)) {
            boardX--;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Moves the core.Piece right by one block. The move wont occur if the core.Piece isn't able to move i.e. it is
     * blocked by a
     * block or the edge of the core.Board
     * @return true if the core.Piece has successfully moved, false if the core.Piece was blocked
     */
    boolean movePieceRight() {
        // Check if the new blocks position overlaps with existing blocks, if not, move the core.Piece
        if (wouldNewCenterBeValid(boardX + 1, boardY)) {
            boardX++;
            return true;
        } else {
            return false;
        }
    }

    public void placePieceOnBoard() {
        for (int i = 0; i < 4; i++) {
            int blockX = (int) blockOffsets.getEntry(0, i) + boardX;
            int blockY = (int) blockOffsets.getEntry(1, i) + boardY;
            Board.board.set(blockX, blockY, new Block(pieceColour.r, pieceColour.g, pieceColour.b));
        }
    }

    void removePieceOnBoard() {
        for (int i = 0; i < 4; i++) {

            int blockX = (int) blockOffsets.getEntry(0, i) + boardX;
            int blockY = (int) blockOffsets.getEntry(1, i) + boardY;
            Board.board.set(blockX, blockY, null);
        }
    }

    /**
     * @return An array of ints that contain the indexes of the columns the piece is in
     */
    int[] whatColumsIsThisPieceIn() {

        if (pieceColour == PieceColour.I) {
            switch (IRotations.identifyRotationFromMatrix(blockOffsets)) {
                case CLOCKWISE_ONE:
                    return new int[]{boardX + 1};
                case CLOCKWISE_THREE:
                    return new int[]{boardX};
            }
        }

        // If it isn't an I core.Piece in those specific rotations, calculate the columns the default way
        int index = 0;
        int[] columns = new int[maxXOffset + Math.abs(minXOffset) + 1];
        for (int i = boardX + minXOffset; i <= boardX + maxXOffset; i++) {
            columns[index] = i;
            index++;
        }

        return columns;
    }

    /**
     * @return Returns an array of ints that contain the indexes of the columns the piece is in, including a column to
     * the left of the piece and one to the right of the piece. If the column were to be outside the board then it will
     * be excluded
     */
    int[] whatColumsIsThisPieceInWithPaddingColumns() {
        // TODO: Make this function more efficient. Possibly use bound offsets and center position

        // Calculate the minimum offset. The piece can't be outside the board but with padding it could be outside
        // the board
        // If the padding would be outside the board then restrict it to the board
        int minPos;
        if (boardX + minXOffset - 1 >= 0) {
            minPos = boardX + minXOffset - 1;
        } else {
            minPos = boardX + minXOffset;
        }

        // Same as above
        int maxPos;
        if (boardX + maxXOffset + 1 < Board.board.width) {
            maxPos = boardX + maxXOffset + 1;
        } else {
            maxPos = boardX + maxXOffset;
        }

        int index = 0;
        int[] columns = new int[maxPos - minPos + 1];
        for (int i = minPos; i <= maxPos; i++) {
            columns[index] = i;
            index++;
        }

        return columns;
    }

    /**
     * Updates the attributes {@link Piece#minXOffset}, {@link Piece#maxXOffset}, {@link Piece#minYOffset},
     * {@link Piece#maxYOffset} as they will be inaccurate after the core.Piece is rotated
     */
    void updateMaxMinValues() {
        // Find the max and min X offset
        this.maxXOffset = -99;
        this.minXOffset = 99;
        for (double d : blockOffsets.getData()[0]) {
            this.maxXOffset = Window.max(this.maxXOffset, (int) d);
            this.minXOffset = Window.min(this.minXOffset, (int) d);
        }

        // Find the max and min X offset
        this.maxYOffset = -99;
        this.minYOffset = 99;
        for (double d : blockOffsets.getData()[1]) {
            this.maxYOffset = Window.max(this.maxYOffset, (int) d);
            this.minYOffset = Window.min(this.minYOffset, (int) d);
        }
    }

    /**
     * @return Width of core.Piece in blocks
     */
    int calculatePieceWidth() {
        // The expression in the bottom return statement doesn't work for the I core.Piece so we account for that here
        if (pieceColour == PieceColour.I) {
            switch (IRotations.identifyRotationFromMatrix(blockOffsets)) {
                case CLOCKWISE_THREE:
                case CLOCKWISE_ONE:
                    return 1;

                case Initial:
                case CLOCKWISE_TWO:
                    return 4;
            }
        }

        // Otherwise, calculate the width as normal
        return Math.abs(minXOffset) + 1 + maxXOffset;
    }

    /**
     * @return The Column Index of the rightmost block
     */
    int getRightMostBlockColumn() {
        return boardX + maxXOffset;
    }

    /**
     * @return true if the piece is at the edge of the board and it can't move further right, false otherwise
     */
    boolean isPieceAtRightEdgeOfBoard() {
        return boardX + maxXOffset == Board.board.width - 1;
    }

    /**
     * Holds information about the colour of each standard core.Piece
     */
    public enum PieceColour {
        J(242, 157, 2),
        T(161, 2, 233),
        I(1, 241, 241),
        S(2, 239, 0),
        Z(237, 2, 0),
        O(238, 241, 1),
        L(1, 1, 240);

        int r;
        int g;
        int b;

        PieceColour(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        static String identifyColourName(int r, int g, int b) {
            for (PieceColour pieceColour : PieceColour.values()) {
                if (pieceColour.r == r && pieceColour.g == g && pieceColour.b == b) return pieceColour.name();
            }

            return null;
        }

        static String identifyColourName(Block block) {
            return identifyColourName(block.r, block.g, block.b);
        }
    }

    /**
     * This enum contains preset block offsets for the I core.Piece in all rotations.
     * <p>
     * All core.Piece rotations apart from the I core.Piece can be calculated using clockwise and counterclockwise
     * roatation
     * matricies. However, since the I core.Piece's center of rotation isn't in the middle of a block, it's in between the
     * corners of the two middle blocks, we can't use a rotation matrix for it.
     */
    private enum IRotations {
        Initial(MatrixUtils.createRealMatrix(new double[][]{{-1, 0, 1, 2}, {-1, -1, -1, -1}})),
        CLOCKWISE_ONE(MatrixUtils.createRealMatrix(new double[][]{{1, 1, 1, 1}, {-2, -1, 0, 1}})),
        CLOCKWISE_TWO(MatrixUtils.createRealMatrix(new double[][]{{-1, 0, 1, 2}, {0, 0, 0, 0}})),
        CLOCKWISE_THREE(MatrixUtils.createRealMatrix(new double[][]{{0, 0, 0, 0}, {-2, -1, 0, 1}}));

        private RealMatrix blockOffsets;

        IRotations(RealMatrix blockOffsets) {
            this.blockOffsets = blockOffsets;
        }

        private static IRotations identifyRotationFromMatrix(RealMatrix matrix) {
            for (IRotations rotations : IRotations.values()) {
                if (rotations.blockOffsets.equals(matrix)) return rotations;
            }

            return null;
        }

        private IRotations getClockwiseRotation() {
            return IRotations.values()[(identifyRotationFromMatrix(blockOffsets).ordinal() + 1) % 4];
        }

        private IRotations getCounterClockwiseRotation() {
            return IRotations.values()[Math.abs(identifyRotationFromMatrix(blockOffsets).ordinal() - 1) % 4];
        }
    }

    enum DefaultPieceRotations {
        L(MatrixUtils.createRealMatrix(new double[][]{{0, 1, -1, -1}, {0, 0, 0, 1}})),
        T(MatrixUtils.createRealMatrix(new double[][]{{0, -1, 1, 0}, {0, 0, 0, 1}})),
        I(IRotations.Initial.blockOffsets),
        S(MatrixUtils.createRealMatrix(new double[][]{{0, 1, 0, -1}, {0, 0, 1, 1}})),
        Z(MatrixUtils.createRealMatrix(new double[][]{{0, -1, 0, 1}, {0, 0, 1, 1}})),
        O(MatrixUtils.createRealMatrix(new double[][]{{0, 0, 1, 1}, {0, 1, 0, 1}})),
        J(MatrixUtils.createRealMatrix(new double[][]{{0, 1, -1, 1}, {0, 0, 0, 1}}));

        PieceColour name;
        RealMatrix blockOffsets;

        DefaultPieceRotations(RealMatrix blockOffsets) {
            this.blockOffsets = blockOffsets;
        }

        static DefaultPieceRotations getDefaultPieceRotation(PieceColour info) {
            return DefaultPieceRotations.values()[info.ordinal()];
        }
    }

}
