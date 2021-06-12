package core;

import processing.core.PApplet;

public class Window extends PApplet {
    // Input arguments
    protected static int heightInBlocks;
    protected static int widthInBlocks;
    protected static int cellDiameter;
    static int argSeed = 2;
    static int heightDiff;
    private static Board board = null;

    static boolean logging = true;
    static boolean placePieces = true;
    static boolean shouldSaveAndResetBoard = false;
    private static float chaos;
    static boolean debug = true;
    private static int saveCount = 0;
    // User Settings
    private static String argFileType = ".tiff"; // Default .tiff
    private static int argOutputNum = 1; // Default 1

    private static long start = System.currentTimeMillis();
    static long end;

    public static void main(String[] args) {
        parseArguments(args);
        PApplet.main("core.Window");
    }

    private static void parseArguments(String[] args) {
        // TODO: Add max height optional argument

        // No args. Print help message and exit
        if (args.length == 0) {
            System.out.println("Default argument order: [WIDTH] [HEIGHT] [DIAMETER] [HEIGHT DIFFERENCE] [CHAOS] " +
                    "[OPTIONAL ARGUMENTS]");

            System.out.println("    WIDTH: Integer - Width of the board in blocks");
            System.out.println("    HEIGHT: Integer - Height of the board in blocks");
            System.out.println("    DIAMETER: Integer - Diameter of the blocks in pixels");
            System.out.println("    HEIGHT DIFFERENCE: Integer - Number of blocks difference between the highest " +
                    "block and lowest block on the board");
            System.out.println("    CHAOS: Integer - Degree of chaos");
            System.out.println("    OPTIONAL ARGUMENTS: ");

            System.out.println("        You can specify optional flags after the default arguments in a format " +
                    "similar to other " +
                    "\n         command line programs by writing --NAME ARGUMENT after the default arguments, e.g. " +
                    "--filetype tiff or --seed 20\n");

            System.out.println("        --filetype - Specifies the output filetype for the image generated. Takes " +
                    "'tiff', " +
                    "\n         'png', 'jpg', 'tga' as arguments. Defaults to the tiff filetype\n");

            System.out.println("        --outputnum - Specifies the number of images the program will generate before" +
                    " exiting. " +
                    "\n         Higher numbers make the program take longer to exit. Takes an integer between 1 and " + Integer.MAX_VALUE + ". Defaults to 1\n");

            System.out.println("        --seed - Specifies the seed used to generate the random numbers used in the " +
                    "program. " +
                    "\n         Running the program with the same seed will produce the same set of images. Defaults " +
                    "to an effectively random seed\n");

            System.exit(0);
        }

        if (args.length < 5) {
            System.out.println("Needs at least 5 default arguments");
            System.exit(0);
        }

        String errorMessage = null;
        // Validate WIDTH
        try {
            widthInBlocks = Integer.valueOf(args[0]);
            if (widthInBlocks < 1) errorMessage = "WIDTH should be an integer above 0";
        } catch (Exception e) {
            errorMessage = "WIDTH argument is not valid";
        }

        // Validate HEIGHT
        try {
            heightInBlocks = Integer.valueOf(args[1]);
            if (heightInBlocks < 1) errorMessage = "HEIGHT should be an integer above 0";
        } catch (Exception e) {
            errorMessage = "HEIGHT argument is not valid";
        }

        // Validate DIAMETER
        try {
            cellDiameter = Integer.valueOf(args[2]);
            if (cellDiameter < 1) errorMessage = "DIAMETER should be an integer above 0";
        } catch (Exception e) {
            errorMessage = "DIAMTER argument is not valid";
        }

        // Validate HEIGHT DIFFERENCE
        try {
            heightDiff = Integer.valueOf(args[3]);
            if (heightDiff < 0) errorMessage = "HEIGHT DIFFERENCE should be an integer above or equal to 0";
        } catch (Exception e) {
            errorMessage = "HEIGHT DIFFERENCE argument is not valid";
        }

        // Validate CHAOS
        try {
            chaos = Integer.valueOf(args[4]) / 100f;
            if (!(chaos >= 0f && chaos <= 1f)) errorMessage = "CHAOS should be an integer between 0 and 100";
        } catch (Exception e) {
            errorMessage = "CHAOS argument is not valid";
        }

        if (errorMessage != null) {
            System.out.println(errorMessage);
            System.exit(0);
        }

        // Validate any optional arguments
        // Iterate through the optional argument pairs
        for (int index = 5; index < args.length; index += 2) {
            String userArg = args[index];

            // If the current optional argument isn't valid then print an error message and exit
            if (!validateArgumentName(userArg)) {
                System.out.println("Optional argument flag " + userArg + " is not valid");
                System.exit(0);
            }

            switch (userArg) {
                case "--filetype":
                    processFileTypeParameter(args[index + 1]);
                    break;
                case "--seed":
                    processSeed(args[index + 1]);
                    break;
                case "--outputnum":
                    processOutputNum(args[index + 1]);
                    break;
            }
        }

        // Set score weightings based on parameters, a height difference of 0 should only care about bumpiness and
        // height difference, not Towers for example
        if (heightDiff == 0) {
            PotentialBoardState.setHeightDiffMultiplier(1);
            PotentialBoardState.setBumpinessMultiplier(1);
            PotentialBoardState.setGoalDistScoreMultiplier(0);
        } else {
            // If we are creating Towers then set Height difference multiplier to 0, it will be changed in the height
            // difference phase
            PotentialBoardState.setHeightDiffMultiplier(0);
            PotentialBoardState.setBumpinessMultiplier(1);
            PotentialBoardState.setGoalDistScoreMultiplier(1);
        }

    }

    private static boolean validateArgumentName(String argumentName) {
        String[] validOptionalArguments = new String[]{"--filetype", "--seed", "--outputnum"};

        // Check if the argument is in the valid argument list
        for (String validArg : validOptionalArguments) {
            // User argument is valid
            if (validArg.equals(argumentName)) {
                return true;
            }
        }

        return false;
    }

    private static void processFileTypeParameter(String filetype) {
        switch (filetype) {
            case "tiff":
                argFileType = ".tiff";
                break;
            case "jpg":
                argFileType = ".jpg";
                break;
            case "png":
                argFileType = ".png";
                break;
            case "tga":
                argFileType = ".tga";
                break;

            default:
                System.out.println(filetype + " filetype argument is not valid");
                System.exit(0);

        }

        if (logging) System.out.println(filetype + " argument is validated");
    }

    private static void processSeed(String seed) {
        try {
            argSeed = Integer.parseInt(seed);
            if (logging) System.out.println(seed + " seed is valid");
        } catch (Exception e) {
            System.out.println(seed + " seed is not valid. It must be an integer");
            System.exit(0);
        }
    }

    private static void processOutputNum(String outputNum) {
        try {
            argOutputNum = Integer.parseInt(outputNum);

            // Output Num is invalid, throw a Runtime Exception so that the catch block runs
            if (argOutputNum < 1) throw new RuntimeException();

            if (logging) System.out.println(outputNum + " output number is valid");
        } catch (Exception e) {
            System.out.println(argOutputNum + " output number is not valid. It must be between 1 and " + Integer.MAX_VALUE);
            System.exit(0);
        }
    }

    public void settings() {
        size(widthInBlocks * cellDiameter, heightInBlocks * cellDiameter);
        board = new Board(widthInBlocks, heightInBlocks);
    }

    public void draw() {
        // If we have saved the target number of images, then exit
        if (saveCount == argOutputNum) {
            System.out.println("Program has generated " + saveCount + " images. Now exiting");
            System.out.println("Time: " + (System.currentTimeMillis() - start) / 1000);
            System.exit(0);
        }

        if (board != null) {
            frameRate(50);
            background(51);
            strokeWeight(1);

//            drawBackboard();
            drawPlacedBlocks();

            if (heightDiff != 0) {
                // Draw all X Goals
                for (TowerGoal goal : Board.towerGoals) {
                    fill(255, 0, 0);
                    ellipse(goal.getBoardX() * cellDiameter,
                            height - (goal.getBoardY() * cellDiameter), 10, 10);
                }

                // Draw the current X Goal
                fill(0, 255, 0);
                ellipse(Board.currentTowerGoal.getBoardX() * cellDiameter,
                        height - (Board.currentTowerGoal.getBoardY() * cellDiameter), 10, 10);
            }



            fill(255);
            text("Piece: " + Piece.PieceColour.identifyColourName(Board.currentPiece.pieceColour.r,
                    Board.currentPiece.pieceColour.g, Board.currentPiece.pieceColour.b), 20, 20);
        }

        // If we've found an acceptable Board then shouldSaveAndResetBoard will be true. If so, save the Board and reset
        if (shouldSaveAndResetBoard) {
            save(saveCount + argFileType);

            Board.resetBoard();

            shouldSaveAndResetBoard = false;
            saveCount++;
        }

        if (frameCount % 1 == 0 && placePieces) {
            for (int i = 0; i < 1; i++) {
                Board.simulateCurrentPiece();
                Board.loadNextPieceFromQueue();
            }
        }

    }

    protected void drawBackboard() {
        stroke(0.8f);

        for (int i = 0; i < Board.board.width; i++) {
            line(i * cellDiameter, 0, i * cellDiameter, height);
        }

        for (int i = 0; i < Board.board.height; i++) {
            line(0, i * cellDiameter, width, i * cellDiameter);
        }
    }

    protected void drawPlacedBlocks() {
        noStroke();

        for (int j = 0; j < Board.board.visibleHeight; j++) {
            for (int i = 0; i < Board.board.width; i++) {
                Block currentBlock = Board.board.getBlock(i, j);

                if (currentBlock != null) {
                    fill(currentBlock.r, currentBlock.g, currentBlock.b);
                    rect(i * cellDiameter, height - ((j + 1) * cellDiameter), cellDiameter,
                            cellDiameter);
                }
            }
        }
    }

    public void keyPressed() {
        if (keyCode == 32) {
            placePieces = !placePieces;
//            debug = true;
        }
    }


}
