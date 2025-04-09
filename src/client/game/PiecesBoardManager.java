package client.game;

import client.utils.PieceRenderer;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import java.util.function.IntConsumer;

/**
 * Manages the grid of 16 selectable pieces in the Quarto game.
 * Responsible for displaying pieces and managing selection availability.
 */
public class PiecesBoardManager {
    private final GridPane piecesBoard;
    private final QuartoGame game;

    /**
     * Constructs a PiecesBoardManager.
     *
     * @param piecesBoard The GridPane where pieces are shown.
     * @param game        The game logic instance.
     */
    public PiecesBoardManager(GridPane piecesBoard, QuartoGame game) {
        this.piecesBoard = piecesBoard;
        this.game = game;
    }

    /**
     * Initializes the grid with 16 buttons, each representing a unique piece.
     *
     * @param onPieceSelected Callback to handle piece selection.
     */
    public void initialize(IntConsumer onPieceSelected) {
        piecesBoard.getChildren().clear();

        for (int i = 0; i < 16; i++) {
            Button btn = new Button();
            btn.setPrefSize(40, 40);
            final int pieceId = i;
            btn.setGraphic(PieceRenderer.createPieceGraphic(pieceId));
            btn.getStyleClass().add("piece-button");
            btn.setUserData(pieceId);

            btn.setOnAction(e -> onPieceSelected.accept(pieceId));
            piecesBoard.add(btn, i % 8, i / 8); // 8 per row
        }

        updateAvailability();
    }

    /**
     * Updates piece buttons based on current availability in the game state.
     */
    public void updateAvailability() {
        for (var node : piecesBoard.getChildren()) {
            if (node instanceof Button btn) {
                int pieceId = (int) btn.getUserData();
                btn.setDisable(!game.getAvailablePieces()[pieceId]);
            }
        }
    }

    /**
     * Enables or disables the entire piece selection board.
     */
    public void setDisabled(boolean disabled) {
        piecesBoard.setDisable(disabled);
    }

    /**
     * Returns the GridPane representing the piece selection board.
     */
    public GridPane getBoard() {
        return piecesBoard;
    }
}
