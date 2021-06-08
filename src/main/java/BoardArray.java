public class BoardArray {
    Block[][] array;
    int offset;

    int height;
    int width;

    BoardArray(int width, int height) {
        // Make the offset the height / 4 if it is bigger than 4, else make it 4
        offset = (height / 4 > 4) ? height / 4 : 4;

        array = new Block[height + offset][width];
        this.height = height + offset;
        this.width = width;
    }

    Block getBlock(int x, int y) {
        return array[y][x];
    }

    void set(int x, int y, Block block) {
        array[y][x] = block;
    }

    void set(Block block) {
        array[block.boardY][block.boardX] = block;
    }

    Block[] getLine(int y) {
        return array[y];
    }


}
