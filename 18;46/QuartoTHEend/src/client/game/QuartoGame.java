package client.game;

import java.util.Arrays;

/**
 * Main game logic and state holder for a Quarto game.
 */
public class QuartoGame {
    private Board board;
    private boolean[] availablePieces; // Tracks which pieces have not yet been used
    private Piece currentPiece;        // The piece that must be placed this turn
    private int currentPlayer;         // Either 1 or 2

    public QuartoGame() {
        board = new Board();
        availablePieces = new boolean[16];
        Arrays.fill(availablePieces, true);
        currentPiece = null;
        currentPlayer = 1;
    }

    /**
     * Sets the piece to be placed this turn.
     */
    public void setCurrentPiece(int pieceId) {
        if (pieceId < 0 || pieceId >= 16)
            throw new IllegalArgumentException("Invalid piece id");
        if (!availablePieces[pieceId])
            throw new IllegalArgumentException("Piece already used");

        currentPiece = new Piece(pieceId);
    }

    /**
     * Places the current piece on the board at the specified position.
     * Returns true if the move wins the game.
     */
    public boolean placeCurrentPiece(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4)
            throw new IllegalArgumentException("Invalid board position");
        if (currentPiece == null)
            throw new IllegalStateException("No piece selected to place");

        board.placePiece(row, col, currentPiece);
        availablePieces[currentPiece.getId()] = false;

        boolean win = WinChecker.checkWin(getIntBoard());

        currentPiece = null;
        currentPlayer = (currentPlayer == 1) ? 2 : 1;

        return win;
    }

    /**
     * Converts the board's pieces to a 2D array of piece IDs for win checking.
     */
    public int[][] getIntBoard() {
        int[][] intBoard = new int[4][4];
        Piece[][] grid = board.getGrid();
        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 4; j++)
                intBoard[i][j] = (grid[i][j] == null) ? -1 : grid[i][j].getId();
        return intBoard;
    }

    // --- Getters ---
    public Board getBoard() {
        return board;
    }

    public boolean[] getAvailablePieces() {
        return availablePieces;
    }

    public int getCurrentPiece() {
        return (currentPiece == null) ? -1 : currentPiece.getId();
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean isBoardFull() {
        for (boolean available : availablePieces) {
            if (available) return false;
        }
        return true;
    }


    /**
     * Returns a deep copy of the game state.
     */
    public QuartoGame copy() {
        QuartoGame copy = new QuartoGame();
        copy.board = this.board.copy();
        copy.availablePieces = Arrays.copyOf(this.availablePieces, 16);
        copy.currentPiece = (this.currentPiece == null) ? null : new Piece(this.currentPiece.getId());
        copy.currentPlayer = this.currentPlayer;
        return copy;
    }
}
