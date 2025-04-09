package client.network;

/**
 * Listener interface for handling game session events in online multiplayer mode.
 */
public interface GameSessionListener {

    /**
     * Called when the online game begins.
     * Indicates whether it's the current player's turn (Player 1) or not.
     *
     * @param isMyTurn true if the player starts first, false otherwise
     */    void startOnlineGameAcourdingToMyTurne(boolean isMyTurn);

    /**
     * Called when the opponent places a piece on the board.
     *
     * @param row the row of the placed piece
     * @param col the column of the placed piece
     */    void opponentPlacedPiece(int row, int col);

    /**
     * Called when the opponent chooses the next piece for the player.
     *
     * @param pieceId the ID of the selected piece
     */    void opponentChosePiece(int pieceId);
}
