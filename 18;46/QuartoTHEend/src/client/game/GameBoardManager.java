package client.game;

import client.utils.PieceRenderer;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

import java.util.function.BiConsumer;

/**
 * This class is responsible for initializing and updating
 * the graphical game board (4x4 grid) in the Quarto game.
 */
public class GameBoardManager {

    private final GridPane gameBoard;
    private final QuartoGame game;

    /**
     * Constructs a GameBoardManager with the specified board and game logic.
     *
     * @param gameBoard The JavaFX GridPane that represents the game board.
     * @param game The instance of the game logic (QuartoGame).
     */
    public GameBoardManager(GridPane gameBoard, QuartoGame game) {
        this.gameBoard = gameBoard;
        this.game = game;
    }

    /**
     * Initializes the board with 4x4 buttons, each showing an empty graphic.
     */
    public void initializeBoard() {
        gameBoard.getChildren().clear();

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                Button btn = new Button();
                btn.setPrefSize(60, 60);
                btn.setGraphic(PieceRenderer.createPieceGraphic(-1));
                btn.getStyleClass().add("game-tile");
                gameBoard.add(btn, col, row);
            }
        }
    }

    /**
     * Sets an event handler for each board cell.
     *
     * @param handler A BiConsumer that receives (row, col) when a button is clicked.
     */
    public void setBoardCellHandlers(BiConsumer<Integer, Integer> handler) {
        for (Node node : gameBoard.getChildren()) {
            if (node instanceof Button btn) {
                btn.setOnAction(e -> {
                    Integer row = GridPane.getRowIndex(btn);
                    Integer col = GridPane.getColumnIndex(btn);
                    handler.accept(row, col);
                });
            }
        }
    }

    /**
     * Updates each cell with the piece from the current game state.
     */
    public void updateBoardGraphics() {
        for (Node node : gameBoard.getChildren()) {
            if (node instanceof Button btn) {
                Integer row = GridPane.getRowIndex(btn);
                Integer col = GridPane.getColumnIndex(btn);
                Piece p = game.getBoard().getPiece(row, col);
                int pieceId = (p == null) ? -1 : p.getId();
                btn.setGraphic(PieceRenderer.createPieceGraphic(pieceId));
            }
        }
    }

    /**
     * Alias for updateBoardGraphics – used externally for clarity.
     */
    public void updateBoard() {
        for (Node node : gameBoard.getChildren()) {
            if (node instanceof Button btn) {
                Integer row = GridPane.getRowIndex(btn);
                Integer col = GridPane.getColumnIndex(btn);
                Piece p = game.getBoard().getPiece(row, col);
                int pieceId = (p == null) ? -1 : p.getId();
                btn.setGraphic(PieceRenderer.createPieceGraphic(pieceId));
            }
        }
    }

}
