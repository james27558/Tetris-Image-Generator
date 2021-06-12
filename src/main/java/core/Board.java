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

    static ArrayList<TowerGoal> towerGoals;
    static TowerGoal currentTowerGoal;

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

        if (Window.heightDiff != 0) generateTowerGoals();

        currentPieceList = Board.generatePieceList();

        currentPiece = new Piece(currentPieceList.remove(0), pieceStartX, pieceStartY);
    }

    /**
     * Generates a random number of Tower goals between 1 and 5 and sets the first goal as the current goal. Wipes
     * the list of current Tower goals
     */
    static private void generateTowerGoals() {
        // Reset the list of Tower Goals
        towerGoals = new ArrayList<>();

        // Generate a random number of Tower goals between 1 and 5
        for (int i = 0; i < random.nextInt(5) + 1; i++) {
            int xGoal = (int) Window.map(random.nextFloat(), 0, 1, 0, board.width - 1);
            int yGoal = (int) Window.map(random.nextFloat(), 0, 1, 0, board.visibleHeight);

            towerGoals.add(new TowerGoal(xGoal, yGoal));
        }

        currentTowerGoal = towerGoals.get(0);
    }

    /**
     * Generates a shuffled PieceColour list with all 7 tetrominos with no duplicate Pieces
     *
     * @return A shuffled PieceColour list with all 7 tetrominos
     */
    public static ArrayList<PieceColour> generatePieceList() {
        // Fill the piece list with all tetrominos
        ArrayList<PieceColour> piecelist = new ArrayList<>(Arrays.asList(PieceColour.values()));
        // Shuffle the array list in place
        Collections.shuffle(piecelist, random);

        return piecelist;
    }

    /**
     * If the currentPieceList isn't empty, the PieceColour at index 0 is taken from the list and made into the current
     * Piece. If the currentPieceList is empty then a new PieceColour list is generated and the member at the start
     * of the sequence is made into the current Piece.
     */
    static void loadNextPieceFromQueue() {
        if (!currentPieceList.isEmpty()) {
            currentPiece = new Piece(currentPieceList.remove(0), pieceStartX, pieceStartY);
        } else {
            currentPieceList = generatePieceList();

            currentPiece = new Piece(currentPieceList.remove(0), pieceStartX, pieceStartY);
        }
    }

    /**
     * Checks if columns in the Board contain any holes
     *
     * @param columns List of column indices to check
     * @return true if any of those columns contain any holes, false otherwise
     */
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

    /**
     *
     */
    static void resetBoard() {
        generateTowerGoals();

        for (int i = 0; i < board.width; i++) {
            for (int j = 0; j < board.height; j++) {
                board.set(i, j, null);
            }
        }
    }

    static int getColumnHeight(int x) {
        for (int i = Board.board.visibleHeight - 1; i >= 0; i--) {
            if (Board.board.getBlock(x, i) != null) return i;
        }

        return 0;
    }

    /**
     * Checks if the Board has no holes, in other words the Board is "perfect"
     *
     * @return true if the Board is perfect, false otherwise
     */
    private static boolean isBoardPerfect() {

        for (int i = 0; i < board.width; i++) {
            for (int j = 0; j < board.visibleHeight; j++) {
                if (board.getBlock(i, j) == null) return false;
            }
        }

        return true;
    }

    static void simulateCurrentPiece() {
        // TODO: Move getting all possible Piece positions into another function

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

                // If where the Piece spawns is obstructed then continue the loop to move it right, don't try and
                // simulate the hard drop
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

        if (Window.logging) {
            for (PotentialBoardState state : allOptions) {
                System.out.println(state);
            }

            System.out.println("Best candidate: " + bestCandidate);
        }

        if (bestCandidate != null) {
            bestCandidate.getPlacedPiece().placePieceOnBoard();
            pieceCount++;

            // If we are making Towers then check if we have reached the current goal
            if (Window.heightDiff != 0) {
                // If the placed Piece is close to the current Tower goal
                if (Window.dist(bestCandidate.pieceX, bestCandidate.pieceY, currentTowerGoal.getBoardX(),
                        currentTowerGoal.getBoardY()) < 2) {
                    // Then this Tower is complete, we can remove it from the list
                    towerGoals.remove(0);

                    // If there are no Towers left to make, move onto the heightDiff phase
                    if (towerGoals.isEmpty()) {
                        // TODO: Add height difference phase
                        System.out.println("Height diff phase");
                        Window.placePieces = false;
                    } else {
                        // Otherwise, there are still Towers to create, so we should set the next Tower goal as our
                        // current goal
                        currentTowerGoal = towerGoals.get(0);
                    }
                }
            }
        } else {
            // Otherwise, check if the board is perfect
            if (isBoardPerfect()) {
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
    static private PotentialBoardState pickCandidate(ArrayList<PotentialBoardState> candidates) {
        if (candidates.size() == 0) return null;

        // If there are equally good candidates, they'll be put in this list
        ArrayList<PotentialBoardState> filteredCandidates = new ArrayList<>();
        float lowestScore = Integer.MAX_VALUE;
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
