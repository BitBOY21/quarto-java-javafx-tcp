package client.game;

/**
 * Represents the 4x4 game board holding pieces.
 */
public class Board {
    private Piece[][] grid;

    public Board() {
        grid = new Piece[4][4];
    }

    /**
     * Returns the piece at the specified position.
     */
    public Piece getPiece(int row, int col) {
        return grid[row][col];
    }

    /**
     * Places a piece at the given cell if it's empty.
     */
    public void placePiece(int row, int col, Piece piece) {
        if (grid[row][col] != null)
            throw new IllegalArgumentException("Cell already occupied");
        grid[row][col] = piece;
    }

    /**
     * Returns the internal grid.
     */
    public Piece[][] getGrid() {
        return grid;
    }

    /**
     * Creates a copy of the board.
     */
    public Board copy() {
        Board newBoard = new Board();
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                newBoard.grid[i][j] = this.grid[i][j];
        return newBoard;
    }


}
