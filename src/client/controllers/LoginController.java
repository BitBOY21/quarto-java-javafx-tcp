package client.controllers;

import client.network.TCPClient;
import client.network.ConnectionManager;
import client.utils.FXMLLoaderUtils;
import client.utils.GameUIUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Controller responsible for user login handling.
 * Allows new registration or existing user authentication.
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    /**
     * Handles login button click.
     * Performs either registration or password validation, then transitions to main menu.
     */
    @FXML
    public void handleLogin() {
        String username = usernameField.getText();

        if (username == null || username.trim().isEmpty()) {
            GameUIUtils.showAlert("Login Error", "Username cannot be empty.");
            return;
        }

        TCPClient client = new TCPClient(username);

        if (!client.isNameExistInDB(username)) { // New user: register
            client.registerUser(username, passwordField.getText());
        } else { // Existing user: validate password
            String password = passwordField.getText();
            if (!client.isPasswordCorrect(password)) {
                GameUIUtils.showAlert("Login Error", "Incorrect password. Please try again.");
                try {
                    client.close(); // Close connection on failure
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        // Save client globally and switch to main menu
        ConnectionManager.setCurrentUsername(username);
        ConnectionManager.setTcpClient(client);
        try {
            FXMLLoaderUtils.switchScene(new ActionEvent(usernameField, null), "/client/view/fxml/MainController.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            GameUIUtils.showAlert("Error", "Failed to load main menu.");
        }
    }
}
