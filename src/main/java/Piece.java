import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Piece {

    static RealMatrix counterClockwiseRotationMatrix = MatrixUtils.createRealMatrix(new double[][]{{0, 1}, {-1, 0}});
    static RealMatrix clockwiseRotationMatrix = MatrixUtils.createRealMatrix(new double[][]{{0, -1}, {1, 0}});

    int maxXOffset;
    int minXOffset;
    int maxYOffset;
    int minYOffset;
    PieceInfo pieceInfo;
    RealMatrix blockOffsets;
    int boardX;
    int boardY;
    boolean stopped;

    Piece(PieceInfo pieceInfo, int boardX, int boardY) {
        this.pieceInfo = pieceInfo;
        this.boardX = boardX;
        this.boardY = boardY;
        this.stopped = false;

        blockOffsets = DefaultPieceRotations.values()[pieceInfo.ordinal()].blockOffsets;
    }

    private static boolean isIndexOutOfBoardBounds(int x, int y) {
        return (x < 0 || x > Board.board.width - 1 || y < 0 || y > Board.board.height - 1);
    }

    static int max(int a, int b) {
        if (a > b) {
            return a;
        } else {
            return b;
        }
    }

    static int min(int a, int b) {
        if (a < b) {
            return a;
        } else {
            return b;
        }
    }

    void rotatePieceClockwise() {
        if (pieceInfo == PieceInfo.O) return;

        // Rotate the piece
        if (pieceInfo == PieceInfo.I) {
            IRotationsClockwise currentRotation = IRotationsClockwise.identifyRotationFromMatrix(blockOffsets);
            blockOffsets = IRotationsClockwise.getNextRotation(currentRotation).blockOffsets;
        } else {
            blockOffsets = clockwiseRotationMatrix.multiply(blockOffsets);
        }

        // Check if the new rotation puts the piece out of bounds
//        if (!wouldNewCenterBeValid(boardX, boardY)) {
//            // If the piece is now out of bounds, try to wall kick it
//            tryToWallKickPiece();
//
//            // If, after the wall kick the piece is still overlapping existing pieces
//            if (!wouldNewCenterBeValid(boardX, boardY)) {
//                // Try to floor kick
//                tryToFloorKickPiece();
//            }
//        }

        // As the piece has been rotated, the min and max values must be updated
        updateMaxMinValues();

    }

    void rotatePieceCounterClockwise() {
        if (pieceInfo == PieceInfo.O) return;

        // Rotate the piece
        if (pieceInfo == PieceInfo.I) {
            IRotationsClockwise currentRotation = IRotationsClockwise.identifyRotationFromMatrix(blockOffsets);
            blockOffsets = IRotationsClockwise.getPreviousRotation(currentRotation).blockOffsets;
        } else {
            blockOffsets = counterClockwiseRotationMatrix.multiply(blockOffsets);
        }

        // Check if the new rotation puts the piece out of bounds
//        if (!wouldNewCenterBeValid(boardX, boardY)) {
//            // If the piece is now out of bounds, try to wall kick it
//            tryToWallKickPiece();
//
//            // If, after the wall kick the piece is still overlapping existing pieces
//            if (!wouldNewCenterBeValid(boardX, boardY)) {
//                // Try to floor kick
//                tryToFloorKickPiece();
//            }
//        }

        // As the piece has been rotated, the min and max values must be updated
        updateMaxMinValues();
    }

    /**
     * Lower the piece on the board by 1 line. If the lowered piece would overlap other blocks then stop the piece
     * and return true, otherwise lower the piece and return false.
     *
     * @return true if the piece can't lower and has stopped, false if the piece has lowered
     */
    boolean lowerPieceOnBoard() {
        if (wouldNewCenterBeValid(boardX, boardY + 1)) {
            boardY++;
        } else {
//            placePieceOnBoard();
            return true;
        }

        return false;
    }

    void hardDropPiece() {
        while (!lowerPieceOnBoard()) {
            //System.out.println(boardY);
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
     * @return Weather the new center would be a valid position for the piece
     */
    private boolean wouldNewCenterBeValid(int newCenterX, int newCenterY) {
        for (int i = 0; i < 4; i++) {
            int blockX = (int) blockOffsets.getEntry(0, i) + newCenterX;
            int blockY = (int) blockOffsets.getEntry(1, i) + newCenterY;

            // If the new space is occupied by an already present block, then the new center is not valid
            if (isIndexOutOfBoardBounds(blockX, blockY) || Board.board.getBlock(blockX, blockY) != null) return false;
        }

        // All new spaces are empty so the new center is valid
        return true;
    }

    boolean movePieceLeft() {
        boolean hasPieceMovedOverall = true;
        boardX--; // Move the center of the block to the left

        // Check if the new blocks position overlaps with existing blocks, if so, move the block right
        if (!wouldNewCenterBeValid(boardX, boardY)) {
            boardX++;
            hasPieceMovedOverall = false;
        }

        // Place the block back on the board
        return hasPieceMovedOverall;
    }

    boolean movePieceRight() {
        boolean hasPieceMovedOverall = true;
        boardX++; // Move the center of the block to the right

        // Check if the new blocks position overlaps with existing blocks, if so, move the block left
        if (!wouldNewCenterBeValid(boardX, boardY)) {
            boardX--;
            hasPieceMovedOverall = false;
        }

        // Place the block back on the board
        return hasPieceMovedOverall;
    }

    void placePieceOnBoard() {
        for (int i = 0; i < 4; i++) {
            int blockX = (int) blockOffsets.getEntry(0, i) + boardX;
            int blockY = (int) blockOffsets.getEntry(1, i) + boardY;
            Board.board.set(blockX, blockY, new Block(blockX, blockY, pieceInfo.r, pieceInfo.g, pieceInfo.b));
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
     * @return Returns an array of ints that contain the indexes of the columns the piece is in
     */
    int[] whatColumsIsThisPieceIn() {
        updateMaxMinValues();

        if (pieceInfo == PieceInfo.I) {
            switch (IRotationsClockwise.identifyRotationFromMatrix(blockOffsets)) {
                case CLOCKWISE_ONE:
                    return new int[]{boardX + 1};
                case CLOCKWISE_THREE:
                    return new int[]{boardX};
            }
        }

        // If it isn't an I Piece in those specific rotations, calculate the columns the default way
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
        updateMaxMinValues();

        // Calculate the minimum offset. The piece can't be outside the board but with padding it could be outside the board
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

    void updateMaxMinValues() {
        // Find the max and min X offset
        this.maxXOffset = -99;
        this.minXOffset = 99;
        for (double d : blockOffsets.getData()[0]) {
            this.maxXOffset = max(this.maxXOffset, (int) d);
            this.minXOffset = min(this.minXOffset, (int) d);
        }

        // Find the max and min X offset
        this.maxYOffset = -99;
        this.minYOffset = 99;
        for (double d : blockOffsets.getData()[1]) {
            this.maxYOffset = max(this.maxYOffset, (int) d);
            this.minYOffset = min(this.minYOffset, (int) d);
        }
    }

    int calculatePieceWidth() {
        return whatColumsIsThisPieceIn().length;
    }

    int getRightMostBlockColumn() {
        updateMaxMinValues();
        return boardX + maxXOffset;
    }

    enum PieceInfo {
        L(242, 157, 2),
        T(161, 2, 233),
        I(1, 241, 241),
        S(2, 239, 0),
        Z(237, 2, 0),
        O(238, 241, 1),
        J(1, 1, 240);

        int r;
        int g;
        int b;

        PieceInfo(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    enum IRotationsClockwise {
        Initial(MatrixUtils.createRealMatrix(new double[][]{{-1, 0, 1, 2}, {-1, -1, -1, -1}})),
        CLOCKWISE_ONE(MatrixUtils.createRealMatrix(new double[][]{{1, 1, 1, 1}, {-2, -1, 0, 1}})),
        CLOCKWISE_TWO(MatrixUtils.createRealMatrix(new double[][]{{-1, 0, 1, 2}, {0, 0, 0, 0}})),
        CLOCKWISE_THREE(MatrixUtils.createRealMatrix(new double[][]{{0, 0, 0, 0}, {-2, -1, 0, 1}}));

        private RealMatrix blockOffsets;

        private IRotationsClockwise(RealMatrix blockOffsets) {
            this.blockOffsets = blockOffsets;
        }

        static IRotationsClockwise identifyRotationFromMatrix(RealMatrix matrix) {
            for (IRotationsClockwise rotations : IRotationsClockwise.values()) {
                if (rotations.blockOffsets.equals(matrix)) return rotations;
            }

            return null;
        }

        static IRotationsClockwise getNextRotation(IRotationsClockwise currentRotation) {
            return IRotationsClockwise.values()[(currentRotation.ordinal() + 1) % 4];
        }

        static IRotationsClockwise getPreviousRotation(IRotationsClockwise currentRotation) {
            return IRotationsClockwise.values()[Math.abs(currentRotation.ordinal() - 1) % 4];
        }
    }

    enum DefaultPieceRotations {
        L(MatrixUtils.createRealMatrix(new double[][]{{0, 1, -1, -1}, {0, 0, 0, 1}})),
        T(MatrixUtils.createRealMatrix(new double[][]{{0, -1, 1, 0}, {0, 0, 0, 1}})),
        I(IRotationsClockwise.Initial.blockOffsets),
        S(MatrixUtils.createRealMatrix(new double[][]{{0, 1, 0, -1}, {0, 0, 1, 1}})),
        Z(MatrixUtils.createRealMatrix(new double[][]{{0, -1, 0, 1}, {0, 0, 1, 1}})),
        O(MatrixUtils.createRealMatrix(new double[][]{{0, 0, 1, 1}, {0, 1, 0, 1}})),
        J(MatrixUtils.createRealMatrix(new double[][]{{0, 1, -1, 1}, {0, 0, 0, 1}}));

        PieceInfo name;
        RealMatrix blockOffsets;

        DefaultPieceRotations(RealMatrix blockOffsets) {
            this.blockOffsets = blockOffsets;
        }

        static DefaultPieceRotations getDefaultPieceRotation(PieceInfo info) {
            return DefaultPieceRotations.values()[info.ordinal()];
        }
    }

}
