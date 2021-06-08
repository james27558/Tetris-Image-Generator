/**
 * Represents a single individual square on the board.
 */
public class Block {

    int boardX;
    int boardY;
    int r;
    int g;
    int b;

    /**
     * Initialises the block on the board with the colour black rgb(0,0,0)
     *
     * @param boardX
     * @param boardY
     */
    Block(int boardX, int boardY) {
        this.boardX = boardX;
        this.boardY = boardY;
    }

    /**
     * Initialises the block on the board with a given colour
     * @param boardX
     * @param boardY
     * @param r Red component, 0 - 255
     * @param g Green component, 0 - 255
     * @param b Blue component, 0 - 255
     */
    Block(int boardX, int boardY, int r, int g, int b) {
        this(boardX, boardY);

        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Sets the colour of the block
     * @param r Red component, 0 - 255
     * @param g Green component, 0 - 255
     * @param b Blue component, 0 - 255
     */
    void setColour(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Sets the colour to that of another block
     * @param block Block to copy the colour from
     */
    void setColour(Block block) {
        r = block.r;
        g = block.g;
        b = block.b;
    }

}
