package client.controllers;

import client.models.Player;
import client.network.ConnectionManager;
import client.network.TCPClient;
import client.utils.FXMLLoaderUtils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.io.IOException;

/**
 * Controller class for the statistics screen.
 * Displays player statistics in a JavaFX table.
 */
public class StatisticsController {
    @FXML private TableView<Player> statsTable;
    @FXML private TableColumn<Player, String> usernameColumn;
    @FXML private TableColumn<Player, String> winPercColumn;
    @FXML private TableColumn<Player, Integer> gamesPlayedColumn;
    @FXML private TableColumn<Player, String> rankingColumn;
    @FXML private TableColumn<Player, Integer> winsColumn;
    @FXML private TableColumn<Player, Integer> drawsColumn;
    @FXML private TableColumn<Player, Integer> lossesColumn;

    /**
     * Called automatically when the FXML is loaded.
     * Initializes the table, configures columns, loads data from server.
     */
    @FXML
    public void initialize() {
        statsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // Bind each column to Player property
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        gamesPlayedColumn.setCellValueFactory(cellData -> cellData.getValue().gamesPlayedProperty().asObject());
        winsColumn.setCellValueFactory(cellData -> cellData.getValue().winsProperty().asObject());
        drawsColumn.setCellValueFactory(cellData -> cellData.getValue().drawsProperty().asObject());
        lossesColumn.setCellValueFactory(cellData -> cellData.getValue().lossesProperty().asObject());

        // Format win percentage (whole number or 2 decimals)
        winPercColumn.setCellValueFactory(cellData -> {
            double value = cellData.getValue().getWinPercentage();
            String display = (value % 1 == 0)
                    ? String.format("%.0f%%", value)
                    : String.format("%.2f%%", value);
            return new ReadOnlyObjectWrapper<>(display);
        });

        // Format ranking (whole number or 2 decimals)
        rankingColumn.setCellValueFactory(cellData -> {
            double value = cellData.getValue().getRanking();
            String display = (value % 1 == 0)
                    ? String.format("%.0f", value)
                    : String.format("%.2f", value);
            return new ReadOnlyObjectWrapper<>(display);
        });

        // Apply external CSS styling
        statsTable.getStylesheets().add(getClass().getResource("/client/view/style.css").toExternalForm());

        // Load data from the server in background
        loadDataFromServer();
    }

    /**
     * Loads player statistics from the server in a background thread.
     * Parses the response into Player objects and fills the table.
     */
    private void loadDataFromServer() {
        Task<ObservableList<Player>> task = new Task<>() {
            @Override
            protected ObservableList<Player> call() {
                TCPClient client = ConnectionManager.getTcpClient();
                ObservableList<Player> players = FXCollections.observableArrayList();

                if (client == null) {
                    System.err.println("⚠ Cannot load statistics: TCPClient is null");
                    return players; // Return empty list instead of throwing an error
                }

                String response = client.getPlayersData();
                if (response != null && !response.trim().isEmpty()) {
                    String[] entries = response.split(";");
                    for (String entry : entries) {
                        String[] parts = entry.split(",");
                        if (parts.length == 7) {
                            try {
                                String username = parts[0].trim();
                                double winPerc = Double.parseDouble(parts[1].trim());
                                int gamesPlayed = Integer.parseInt(parts[2].trim());
                                double ranking = Double.parseDouble(parts[3].trim());
                                int wins = Integer.parseInt(parts[4].trim());
                                int draws = Integer.parseInt(parts[5].trim());
                                int losses = Integer.parseInt(parts[6].trim());

                                players.add(new Player(username, winPerc, gamesPlayed, ranking, wins, draws, losses));
                            } catch (NumberFormatException e) {
                                System.err.println("⚠ Invalid player data: " + entry);
                            }
                        }
                    }
                }
                return players;
            }
        };

        task.setOnSucceeded(e -> statsTable.setItems(task.getValue()));
        task.setOnFailed(e -> task.getException().printStackTrace());
        new Thread(task).start();
    }

    /**
     * Handles the "Back" button click – returns to the main menu.
     */
    @FXML
    private void exitScreen(ActionEvent event) {
        try {
            FXMLLoaderUtils.switchScene(event, "/client/view/fxml/MainController.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
