package client;

import client.utils.FXMLLoaderUtils;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Entry point of the Quarto game application.
 */
public class Main extends Application {

    /**
     * Starts the JavaFX application by loading the login screen.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the login screen from FXML
            Parent root = FXMLLoaderUtils.loadFXML("/client/view/fxml/LoginController.fxml");

            primaryStage.setTitle("Quarto Game");
            primaryStage.setResizable(false);
            primaryStage.setScene(new Scene(root)); // Use FXML preferred size
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load the login screen.");
        }
    }

    /**
     * Main method to launch the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
