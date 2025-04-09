package client.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Utility class for switching scenes using FXML files.
 */
public class FXMLLoaderUtils {

    /**
     * Switches the current scene to the one specified by fxmlPath.
     * @param event The event that triggered the switch (usually from a button click)
     * @param fxmlPath Path to the FXML file to load
     * @throws IOException if the FXML file cannot be loaded
     */
    public static void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(FXMLLoaderUtils.class.getResource(fxmlPath));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Loads and returns the Parent node of the specified FXML path.
     * This method can be used outside of an ActionEvent context (e.g., in Main).
     *
     * @param fxmlPath Path to the FXML file (e.g., "/client.view/LoginController.fxml")
     * @return Parent node loaded from the FXML
     * @throws IOException if the FXML cannot be loaded
     */
    public static Parent loadFXML(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(FXMLLoaderUtils.class.getResource(fxmlPath));
        return loader.load();
    }
}
