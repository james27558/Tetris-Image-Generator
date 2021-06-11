package core;

/**
 * Represents a single individual square on the board.
 */
public class Block {

    int r;
    int g;
    int b;

    /**
     * Initialises the block with a given colour
     * @param r Red component, 0 - 255
     * @param g Green component, 0 - 255
     * @param b Blue component, 0 - 255
     */
    public Block(int r, int g, int b) {
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
    public void setColour(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    /**
     * Sets the colour to that of another block
     * @param block core.Block to copy the colour from
     */
    void setColour(Block block) {
        r = block.r;
        g = block.g;
        b = block.b;
    }

    @Override
    public String toString() {
        return Piece.PieceColour.identifyColourName(r, g, b);
    }
}
