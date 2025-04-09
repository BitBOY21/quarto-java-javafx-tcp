package client.controllers;

import client.network.ConnectionManager;
import client.network.TCPClient;
import client.utils.FXMLLoaderUtils;
import client.utils.GameUIUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Controller for the main menu screen.
 * Handles navigation to other screens and game modes.
 */
public class MainController {

    /**
     * Opens the game rules screen.
     */
    @FXML
    private void showRules(ActionEvent event) {
        try {
            FXMLLoaderUtils.switchScene(event, "/client/view/fxml/RulesController.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            GameUIUtils.showAlert("Error", "Failed to open Rules screen.");
        }
    }

    /**
     * Starts an offline game against the computer.
     */
    @FXML
    private void playOffline(ActionEvent event) {
        try {
            ConnectionManager.setIsOnlineGame(false); // Ensure we're not in online mode
            FXMLLoaderUtils.switchScene(event, "/client/view/fxml/GameEngine.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            GameUIUtils.showAlert("Error", "Failed to start offline game.");
        }
    }

    /**
     * Starts an online game by creating a new TCP connection.
     */
    @FXML
    private void playOnline(ActionEvent event) {
        try {
            ConnectionManager.setIsOnlineGame(true);

            // Create new TCP client for the online session
            String username = ConnectionManager.getCurrentUsername();
            TCPClient newClient = new TCPClient(username);
            ConnectionManager.setTcpClient(newClient);

            FXMLLoaderUtils.switchScene(event, "/client/view/fxml/GameEngine.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            GameUIUtils.showAlert("Error", "Failed to start online game.");
        }
    }

    /**
     * Opens the statistics screen.
     */
    @FXML
    private void showStatistics(ActionEvent event) {
        try {
            FXMLLoaderUtils.switchScene(event, "/client/view/fxml/StatisticsController.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            GameUIUtils.showAlert("Error", "Failed to open Statistics screen.");
        }
    }

    /**
     * Exits the application and closes TCP connection if exists.
     */
    @FXML
    private void exitApp(ActionEvent event) {
        TCPClient client = ConnectionManager.getTcpClient();
        if (client != null) {
            client.notifyDisconnect(); // נשלח הודעה לשרת שהשחקן עזב
        }
        ConnectionManager.setTcpClient(null); // מחיקת הלקוח
        System.exit(0);
    }

}
