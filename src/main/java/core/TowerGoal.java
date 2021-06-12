package core;

/**
 * Holds information about a tower the program is trying to create
 */
class TowerGoal {
    private int boardX;
    private int boardY;

    TowerGoal(int x, int y) {
        boardX = x;
        boardY = y;
    }

    int getBoardX() {
        return boardX;
    }

    int getBoardY() {
        return boardY;
    }
}
