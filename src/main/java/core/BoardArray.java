package core;

public class BoardArray {
    Block[][] array;
    int offset;

    int height;
    int visibleHeight;
    int width;

    BoardArray(int width, int height) {
        // Make the offset the height / 4 if it is bigger than 4, else make it 4
        offset = Window.max(height / 2, 10);
//        offset = 20;

        array = new Block[height + offset][width];
        this.height = height + offset;
        this.visibleHeight = height;
        this.width = width;
    }

    /**
     * Transforms the y index from being array focused (index 0 is the top row of the board, the 0th array) to being
     * core.Board focused (index 0 is the bottom row of the board)
     *
     * @param y Y index to transform
     * @return core.Board focused y index
     */
    int getY(int y) {
        return height - 1 - y;
    }

    public Block getBlock(int x, int y) {
        return array[getY(y)][x];
    }

    public void set(int x, int y, Block block) {
        array[getY(y)][x] = block;
    }


    /**
     * Gets a row of Blocks form the core.BoardArray
     * @param y Row index to get
     * @return Line of Blocks at index y
     */
    Block[] getLine(int y) {
        return array[y];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < height; j++) {
            for (Block block : array[j]) {
                sb.append(block == null ? "." : block);
                sb.append(" ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
