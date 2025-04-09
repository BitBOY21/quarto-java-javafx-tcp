package client.utils;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

/**
 * Responsible for rendering the currently selected piece in the UI.
 */
public class SelectedPieceDisplayManager {

    private final GridPane selectedPiecePane;

    public SelectedPieceDisplayManager(GridPane selectedPiecePane) {
        this.selectedPiecePane = selectedPiecePane;
    }

    /**
     * Displays the current piece in the designated pane.
     * If pieceId is -1, the pane is cleared.
     */
    public void displaySelectedPiece(int pieceId) {
        selectedPiecePane.getChildren().clear();

        if (pieceId == -1) return;

        Button btn = new Button();
        btn.setPrefSize(60, 60);
        btn.setGraphic(PieceRenderer.createPieceGraphic(pieceId));
        btn.getStyleClass().add("piece-button");

        selectedPiecePane.add(btn, 0, 0);
    }

    /**
     * Clears the display pane.
     */
    public void clear() {
        selectedPiecePane.getChildren().clear();
    }
}
