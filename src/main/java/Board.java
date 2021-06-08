import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Board {
    static BoardArray board;
    static Piece currentPiece;
    static Piece.PieceColour holdPieceColour;

    static Random random;
    static int pieceStartX = 3;
    static int pieceStartY = 2;
    static boolean canHold = true;
    static ArrayList<Piece.PieceColour> currentPieceList;
    static ArrayList<Piece.PieceColour> nextPieceList;
    static int pieceCount = 0;

    static {
        if (Window.argSeed == -1) {
            // No seed specified by the user so use a random one
            random = new Random();
        } else {
            // Seed has been specified by the user so use that
            random = new Random(Window.argSeed);
        }
    }

    Board(int widthInBlocks, int heightInBlocks) {
        board = new BoardArray(widthInBlocks, heightInBlocks);

        currentPieceList = Board.generatePieceList();
        nextPieceList = Board.generatePieceList();

        currentPiece = new Piece(currentPieceList.remove(0), pieceStartX, pieceStartY);
    }

    static ArrayList<Piece.PieceColour> generatePieceList() {
        ArrayList piecelist = new ArrayList<Piece.PieceColour>();

        for (Piece.PieceColour piece : Piece.PieceColour.values()) {
            piecelist.add(piece);
            piecelist.add(piece);
        }

        Collections.shuffle(piecelist, random);

        return piecelist;
    }

    static void hardDropCurrentPiece() {
        while (!currentPiece.lowerPieceOnBoard()) {
        }

        loadNextPieceFromQueue();
    }

    static void loadNextPieceFromQueue() {
        if (currentPieceList.size() > 0) {
            currentPiece = new Piece(currentPieceList.remove(0), pieceStartX, pieceStartY);
        } else {
            currentPieceList = nextPieceList;
            nextPieceList = generatePieceList();

            currentPiece = new Piece(currentPieceList.remove(0), pieceStartX, pieceStartY);
        }
    }

    static void lowerCurrentPiece() {
        if (currentPiece.lowerPieceOnBoard()) {
            loadNextPieceFromQueue();
        }
    }

    static boolean isLineComplete(Block[] line) {
        for (Block block : line) {
            if (block == null) return false;
        }

        return true;
    }

    static boolean isLineEmpty(Block[] line) {
        for (Block block : line) {
            if (block != null) return false;
        }

        return true;
    }

    /**
     * Returns the piece info of the nth piece in the piece list, if the currentPieceList isn't big enough, ie. n = 3
     * and the currentPieceList is only 1 piece long, then it will pick the 2nd piece from the nextPieceList
     *
     * @param n nth piece to get in the piece list, starting at 1. n = 1 is the next piece in the queue
     * @return the piece info of the nth piece
     */
    static Piece.PieceColour getNthPieceInQueue(int n) {
        // If the currentPieceList isn't big enough then get from nextPieceList
        if (n > currentPieceList.size()) {
            return nextPieceList.get(n - currentPieceList.size() - 1);
        } else {
            return currentPieceList.get(n - 1);
        }
    }

    static boolean doColumnsContainHoles(int[] columns) {
        for (int columnIndex : columns) {
            // For all blocks in the column except the lowest layer
            for (int i = 0; i < board.height - 1; i++) {
                // If there is a block with a hole underneath it
                if (board.getBlock(columnIndex, i) != null && board.getBlock(columnIndex, i + 1) == null) return true;
            }
        }

        return false;
    }

    /**
     * @return The height difference of the board in blocks
     */
    static int calculateBoardHeightDiff() {
        // Initialise variables to their max value, they will be modified by this function
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;

        // Iterate through all the columns
        for (int i = 0; i < board.width; i++) {

            // Move down the column until we find a block
            for (int j = 0; j < board.height; j++) {
                if (j == board.height - 1) maxHeight = j;

                if (board.getBlock(i, j) != null) {
                    if (j < minHeight) minHeight = j;
                    if (j > maxHeight) maxHeight = j;
                    // Stop searching the column when the block is reached
                    break;
                }
            }

        }

        return maxHeight - minHeight;
    }

    static void resetBoard() {
        for (int i = 0; i < board.width; i++) {
            for (int j = 0; j < board.height; j++) {
                board.set(i, j, null);
            }
        }
    }

    void simulateCurrentPiece() {

        // Calculate the number of times we need to rotate the piece
        int numberOfTimesToRotate;
        switch (currentPiece.pieceColour) {
            case I:
                numberOfTimesToRotate = 1;
                break;
            case O:
                numberOfTimesToRotate = 0;
                break;
            default:
                numberOfTimesToRotate = 3;
                break;
        }

        ArrayList<PotentialBoardState> allOptions = new ArrayList();

        // Repeat for all rotations
        for (int rotateCount = 0; rotateCount <= numberOfTimesToRotate; rotateCount++) {
            // Update the min and max values for the piece as it has been rotated
            currentPiece.updateMaxMinValues();
            // Move the piece so it is flush with the left side of the board
            currentPiece.boardX = Math.abs(currentPiece.minXOffset);

            // If the I piece is vertical such that its center block is to the left of the piece, start the center block
            // at -1 so that the line piece is flush with the left side of the board
            if (currentPiece.pieceColour == Piece.PieceColour.I && rotateCount == 1) currentPiece.boardX = -1;

            // Repeat while the edge of the piece hasn't reached the edge of the board
            for (int i = 0; i <= board.width - currentPiece.calculatePieceWidth(); i++) {
                currentPiece.boardY = 2;
                currentPiece.hardDropPiece();

                currentPiece.placePieceOnBoard();
                if (!doColumnsContainHoles(currentPiece.whatColumsIsThisPieceInWithPaddingColumns()))
                    allOptions.add(new PotentialBoardState(currentPiece.pieceColour, currentPiece.boardX,
                            currentPiece.boardY, rotateCount, calculateBoardHeightDiff()));
                currentPiece.removePieceOnBoard();

                currentPiece.boardX++;
            }

            currentPiece.rotatePieceClockwise();
        }

        // Now that all positions have been simulated, we need to pick a position to use
        PotentialBoardState bestCandidate = pickCandidate(allOptions);

        if (Window.logging && currentPiece.pieceColour == Piece.PieceColour.I) {
            for (PotentialBoardState state : allOptions) {
                System.out.println(state);
            }
        }

        if (Window.logging && currentPiece.pieceColour == Piece.PieceColour.I) {
            System.out.println("Best candidate: " + bestCandidate);
        }
        if (bestCandidate != null) {
            bestCandidate.producePiece().placePieceOnBoard();
            pieceCount++;
        } else {

            int nullCount = 0;
            for (int i = 0; i < board.width; i++) {
                for (int j = Board.board.offset; j < board.height; j++) {
                    if (board.getBlock(i, j) == null) nullCount++;
                }
            }


            if (nullCount == 0) {
                System.out.println("Perfect Board " + pieceCount);
                Window.shouldSaveAndResetBoard = true;

            } else {
                resetBoard();
            }


//            throw new RuntimeException("Piece can't be placed");
        }
    }

    /**
     * Returns the best candidate from a list of potential board state. If there are candidates that are equally good then
     * they'll be picked randomly from the set of the best candidates
     *
     * @param candidates All potential board states
     * @return Best candidate
     */
    private PotentialBoardState pickCandidate(ArrayList<PotentialBoardState> candidates) {
        if (candidates.size() == 0) return null;

        // If there are equally good candidates, they'll be put in this list
        ArrayList<PotentialBoardState> filteredCandidates = new ArrayList<>();
        float lowestScore = 9999;
        for (PotentialBoardState state : candidates) {
            if (state.score < lowestScore) lowestScore = state.score;
        }

        for (PotentialBoardState state : candidates) {
            if (state.score == lowestScore) filteredCandidates.add(state);
        }

        int randomNumber = random.nextInt(filteredCandidates.size());
        return filteredCandidates.get(randomNumber);
    }


}
