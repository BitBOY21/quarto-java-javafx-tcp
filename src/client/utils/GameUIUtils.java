package client.utils;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * Utility class for showing alerts and extracting data from JavaFX nodes.
 */
public class GameUIUtils {

    /**
     * Shows a simple informational alert with the given title and message.
     */
    public static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Extracts the piece ID from a graphic (StackPane with binary text inside).
     * Used to detect which piece was clicked.
     * @return piece ID in base 10 or -1 if it couldn't be parsed
     */
    public static int getPieceIdFromGraphic(Node graphic) {
        if (graphic instanceof StackPane sp && sp.getChildren().size() > 1) {
            Node second = sp.getChildren().get(1);
            if (second instanceof Text txt) {
                try {
                    return Integer.parseInt(txt.getText(), 2);
                } catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }


}
