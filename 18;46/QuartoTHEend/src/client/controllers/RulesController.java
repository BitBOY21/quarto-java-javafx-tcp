package client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import client.utils.FXMLLoaderUtils;
import java.io.IOException;

/**
 * Controller for the Rules screen.
 * Displays the Quarto game rules using styled TextFlow.
 */
public class RulesController {

    @FXML
    private TextFlow rulesFlow;

    /**
     * Initializes the rules screen with stylized game instructions.
     */
    @FXML
    public void initialize() {
        rulesFlow.getChildren().addAll(
                bold("Quarto – Game Rules\n\n"),

                bold("OBJECTIVE:\n"),
                regular("Place four pieces in a row (vertical, horizontal, or diagonal)\nthat share at least one common attribute.\n\n"),

                bold("ATTRIBUTES:\n"),
                regular("Each piece is defined by 4 traits:\n"),
                regular("• Size: Big / Small\n"),
                regular("• Color: Red / Blue\n"),
                regular("• Shape: Square / Circle\n"),
                regular("• Hole: With / Without Hole\n\n"),

                bold("HOW TO PLAY:\n"),
                regular("• Players take turns.\n"),
                regular("• On your turn:\n"),
                regular("   1. Place the piece your opponent gave you.\n"),
                regular("   2. Choose the next piece for your opponent.\n\n"),

                regular("• You cannot move a placed piece.\n"),
                regular("• The first player only chooses the first piece.\n"),
                regular("• The game ends in a draw if all 16 pieces are used with no winner.\n\n"),

                bold("Be smart. Think ahead. And shout: QUARTO!")
        );
    }

    /**
     * Helper to create bold-styled Text.
     */
    private Text bold(String content) {
        Text t = new Text(content);
        t.setStyle("-fx-font-weight: bold");
        return t;
    }

    /**
     * Helper to create regular Text.
     */
    private Text regular(String content) {
        return new Text(content);
    }

    /**
     * Handles "Back" button to return to main menu.
     */
    @FXML
    private void exitToMainMenu(ActionEvent event) {
        try {
            FXMLLoaderUtils.switchScene(event, "/client/view/fxml/MainController.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
