public class Block {

    int boardX;
    int boardY;
    int r;
    int g;
    int b;

    Block(int boardX, int boardY) {
        this.boardX = boardX;
        this.boardY = boardY;
    }

    Block(int boardX, int boardY, int r, int g, int b) {
        this(boardX, boardY);

        this.r = r;
        this.g = g;
        this.b = b;
    }

    void setColour(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    void setColour(Block block) {
        r = block.r;
        g = block.g;
        b = block.b;
    }

}
