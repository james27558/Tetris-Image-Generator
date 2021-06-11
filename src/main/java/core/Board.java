package core;

import core.Piece.PieceColour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Board {
    public static BoardArray board;
    static Piece currentPiece;
    static PieceColour holdPieceColour;

    private static Random random;
    private static int pieceStartX = 3;
    private static int pieceStartY = 2;
    static boolean canHold = true;
    private static ArrayList<PieceColour> currentPieceList;
    private static int pieceCount = 0;

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

        currentPiece = new Piece(currentPieceList.remove(0), pieceStartX, pieceStartY);
    }

    public static ArrayList<PieceColour> generatePieceList() {

        ArrayList<PieceColour> piecelist = new ArrayList<>(Arrays.asList(PieceColour.values()));

        Collections.shuffle(piecelist, random);

        return piecelist;
    }


    static void loadNextPieceFromQueue() {
        if (!currentPieceList.isEmpty()) {
            currentPiece = new Piece(currentPieceList.remove(0), pieceStartX, pieceStartY);
        } else {
            currentPieceList = generatePieceList();

            currentPiece = new Piece(currentPieceList.remove(0), pieceStartX, pieceStartY);
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


    private static boolean doColumnsContainHoles(int[] columns) {
        for (int columnIndex : columns) {
            // For all blocks in the column except the lowest layer
            for (int i = board.height - 1; i > 0; i--) {
                // If there is a block with a hole underneath it, return true
                Block thisBlock = board.getBlock(columnIndex, i);
                Block underneathBlock = board.getBlock(columnIndex, i - 1);
                if (thisBlock != null && underneathBlock == null) return true;
            }
        }

        return false;
    }

    /**
     * @return The height difference of the board in blocks, a level board has a height difference of 0
     */
    private static int calculateBoardHeightDiff() {
        // Initialise variables to their max value, they will be modified by this function
        int minHeight = Integer.MAX_VALUE;
        int maxHeight = Integer.MIN_VALUE;

        // Iterate through all the columns
        for (int i = 0; i < board.width; i++) {

            // Move down the column until we find a block
            // Start at the top of the core.Board, index visibleHeight - 1
            for (int j = board.height - 1; j >= 0; j--) {

                // If we see a block then we can stop for this column
                if (board.getBlock(i, j) != null) {
                    // Set it as the new maximum height if it is bigger than the current maximum
                    if (j > maxHeight) maxHeight = j;
                    // Set it as the new minimum height if it is smaller than the current minimum
                    if (j < minHeight) minHeight = j;

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

        ArrayList<PotentialBoardState> allOptions = new ArrayList<>();

        // Repeat for all rotations
        for (int rotateCount = 0; rotateCount <= numberOfTimesToRotate; rotateCount++) {
            // Move the piece so it is flush with the top left side of the board
            currentPiece.boardX = Math.abs(currentPiece.minXOffset);
            // Every time we drop the Piece, we need to reset it so that it's flush with the top of the board
            int yReset = board.height - 1 - currentPiece.maxYOffset;


            // If the I piece is vertical such that its center block is to the left of the piece, start the center block
            // at -1 so that the line piece is flush with the left side of the board
            if (currentPiece.pieceColour == PieceColour.I && rotateCount == 1) currentPiece.boardX = -1;

            // For the first piece, start it at the top of the board, at the end of the loop this will happen for the
            // piece after it
            currentPiece.boardY = yReset;
            // Repeat while the edge of the piece hasn't reached the edge of the board
            do {

                // If where the Piece spawns is obstructed then move it right, don't try and simulate the hard drop
                if (!currentPiece.wouldNewCenterBeValid(currentPiece.boardX, currentPiece.boardY)) {
                    continue;
                }

                // Otherwise, hard drop the piece
                currentPiece.hardDropPiece();

                // Place the Piece on the board
                currentPiece.placePieceOnBoard();
//                if (Window.debug) System.out.println(board);

                // If the board doesn't contain any holes after the piece has been placed then add the board state
                // into the list of candidates
                if (!doColumnsContainHoles(currentPiece.whatColumsIsThisPieceInWithPaddingColumns()))
                    allOptions.add(new PotentialBoardState(currentPiece.pieceColour, currentPiece.boardX,
                            currentPiece.boardY, rotateCount, calculateBoardHeightDiff()));

                currentPiece.removePieceOnBoard();
//                if (Window.debug) System.out.println(board);

                currentPiece.boardY = yReset;
            } while (currentPiece.movePieceRight());

            currentPiece.rotatePieceClockwise();
        }

        // Now that all positions have been simulated, we need to pick a position to use
        PotentialBoardState bestCandidate = pickCandidate(allOptions);

        if (Window.logging && currentPiece.pieceColour == PieceColour.I) {
            for (PotentialBoardState state : allOptions) {
                System.out.println(state);
            }
        }

        if (Window.logging && currentPiece.pieceColour == PieceColour.I) {
            System.out.println("Best candidate: " + bestCandidate);
        }

        if (bestCandidate != null) {
            bestCandidate.getPlacedPiece().placePieceOnBoard();
            pieceCount++;
        } else {

            int nullCount = 0;
            for (int i = 0; i < board.width; i++) {
                for (int j = 0; j < board.visibleHeight; j++) {
                    if (board.getBlock(i, j) == null) nullCount++;
                }
            }


            if (nullCount == 0) {
                System.out.println("Perfect core.Board " + pieceCount);
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
