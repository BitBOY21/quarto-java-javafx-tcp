package client.controllers;

import client.game.*;
import client.network.ConnectionManager;
import client.network.GameSessionListener;
import client.network.TCPClient;
import client.utils.FXMLLoaderUtils;
import client.utils.GameUIUtils;
import client.utils.SelectedPieceDisplayManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import java.io.IOException;
import java.util.Random;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Main game controller for Quarto gameplay.
 * Supports both offline (vs. computer) and online (vs. another player) modes.
 * Handles user actions, game flow, and chat communication.
 */
public class GameEngine implements GameSessionListener {

    // === FXML UI Components ===
    @FXML private GridPane piecesBoard;         // Grid for choosing pieces
    @FXML private GridPane gameBoard;           // Grid for placing pieces
    @FXML private GridPane selectedPiecePane;   // Panel to display current selected piece
    @FXML private Label title;
    @FXML private Label turnLabel;
    @FXML private TextArea chatArea;
    @FXML private TextField chatInput;
    @FXML private VBox chatBox;

    // === Game Logic ===
    private QuartoGame game;
    private final Random random = new Random();
    private SelectedPieceDisplayManager selectedPieceManager;
    private GameBoardManager boardManager;
    private PiecesBoardManager piecesBoardManager;

    // === Game State Flags ===
    private boolean waitingForPlacement = true;
    private boolean waitingForPieceChoice = false;
    private boolean gameEnded = false;
    private boolean gameReallyStarted = false;
    private boolean myTurn = false;               // was: myTurn
    private boolean isFirstPlayer = false;
    private boolean isOnlineGame;
    private boolean waitingForOpponent = false;
    private boolean opponentDisconnected = false;

    // === Networking ===
    private TCPClient client;

    /**
     * Initializes the game screen, UI managers, and network logic.
     * Handles both offline (vs. computer) and online (vs. player) modes.
     * Sets up board interaction, starting turn logic, and chat listener if applicable.
     */
    @FXML
    public void initialize() {
        game = new QuartoGame();// Create new game logic instance

        // Initialize board and piece UI managers
        boardManager = new GameBoardManager(gameBoard, game); // Initialize board UI manager
        boardManager.initializeBoard();
        boardManager.setBoardCellHandlers(this::handlePlacement);

        selectedPieceManager = new SelectedPieceDisplayManager(selectedPiecePane);
        selectedPieceManager.displaySelectedPiece(-1);
        piecesBoardManager = new PiecesBoardManager(piecesBoard, game);
        piecesBoardManager.initialize(this::handlePieceSelection);

        isOnlineGame = ConnectionManager.getIsOnlineGame(); // Enable Enter key to send chat

        chatInput.setOnAction(event -> onSendChat());

        if (isOnlineGame) {
            // Online setup: connect to server and disable board until ready
            client = ConnectionManager.getTcpClient();
            client.startGameSessionWithServer();
            client.setGameSessionListener(this);
            waitingForOpponent = true;

            title.setText("Quarto Online");
            piecesBoard.setDisable(true);
            gameBoard.setDisable(true);

            // Incoming chat messages
            client.setChatListener(msg -> {
                Platform.runLater(() -> {
                    chatArea.appendText(msg + "\n");
                });
            });

        } else {
            // Offline setup: randomly decide if player or computer starts
            boolean playerStarts = Math.random() < 0.5;

            if (playerStarts) {
                int pieceForPlayer = chooseRandomAvailablePiece();
                game.setCurrentPiece(pieceForPlayer);
                selectedPieceManager.displaySelectedPiece(pieceForPlayer);
                waitingForPlacement = true;
                waitingForPieceChoice = false;
            } else {
                computerMove();
                waitingForPlacement = false;
                waitingForPieceChoice = true;
            }

            turnLabel.setText(waitingForPlacement
                    ? "Place a Piece!"
                    : "Pick a Piece!");

            // Hide chat in offline mode
            if (chatBox != null) {
                chatBox.setVisible(false);
                chatBox.setManaged(false);
            }
        }
    }

    /**
     * Triggered when the player clicks the "Exit" button.
     * Delegates actual logic to performExit(), which handles different exit cases
     */
    @FXML
    private void exitScreen(ActionEvent event) {
        performExit();
    }

    /**
     * Handles player exit from the game.
     * If the game hasn't started yet – exits silently.
     * If game is in progress – asks for confirmation and records loss if player quits.
     */
    private void performExit() {
        // Exit quietly if game hasn't started yet
        if (isOnlineGame && client != null && !gameReallyStarted) {
            System.out.println("🟢 Exiting quietly — game hasn't started yet");
            goBackToMainMenu();
            return;
        }

        // If game is in progress, confirm with the user
        if (isOnlineGame && !gameEnded && client != null && !opponentDisconnected && gameReallyStarted) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Leave Game");
                alert.setHeaderText(null);
                alert.setContentText("If you leave now, it will count as a loss. Are you sure?");

                ButtonType yesBtn = new ButtonType("Yes, Exit");
                ButtonType noBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(yesBtn, noBtn);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == yesBtn) {
                    client.notifyILoss();
                    client.sendMessage("opponentWonByQuit"); // Notify opponent
                    goBackToMainMenu();
                }
            });
        } else {
            // Game already ended or offline – just return to menu
            goBackToMainMenu();
        }
    }

    /**
     * Switches back to the main menu screen.
     * Keeps the TCP client alive to allow post-game actions (like viewing stats).
     */
    private void goBackToMainMenu() {
        try {
            FXMLLoaderUtils.switchScene(new ActionEvent(title, null), "/client/view/fxml/MainController.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // We keep the TCPClient instance alive for statistics access
        ConnectionManager.setIsOnlineGame(false);
    }

    /**
     * Disables both game board and piece selection grid.
     * Typically used after win, draw, or quit.
     */
    private void disableAll() {
        gameBoard.setDisable(true);
        piecesBoard.setDisable(true);
    }

    /**
     * Sends the player's message to the opponent (only in online mode).
     * The message is also displayed locally in the chat area.
     */
    @FXML
    private void onSendChat() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty() && client != null) {
            client.sendMessage("chat " + message); // Send to opponent
            chatArea.appendText("You: " + message + "\n"); // Show in local chat
            chatInput.clear();
        }
    }

    /**
     * Called when the player selects a piece to give the opponent (or computer).
     * Disables piece board and enables the game board for placement phase.
     */
    private void handlePieceSelection(int pieceId) {
        gameReallyStarted = true;

        // Only allowed to select if not in placement phase
        if (!waitingForPlacement) {
            if (isOnlineGame) {
                client.sendMessage("moveChoose " + pieceId);
                myTurn = false; // End of player's turn

            }

            try {
                game.setCurrentPiece(pieceId);
                selectedPieceManager.displaySelectedPiece(pieceId);
                waitingForPlacement = true;
                waitingForPieceChoice = false;

                piecesBoardManager.setDisabled(true);
                turnLabel.setText("Opponent's turn");

                // In offline mode, immediately let the computer move
                if (!isOnlineGame) {
                    computerMove();
                }
            } catch (Exception ex) {
                GameUIUtils.showAlert("Error", ex.getMessage());
            }
        }
    }

    /**
     * Called when the player places the current piece on the board.
     * Updates board UI and checks for win/draw state.
     */
    private void handlePlacement(int row, int col) {
        gameReallyStarted = true;

        // Send move to opponent if playing online
        if (isOnlineGame && waitingForPlacement && !gameBoard.isDisable()) {
            client.sendMessage("movePlace " + row + " " + col);
            myTurn = false; // End of player's turn

        }

        try {
            boolean win = game.placeCurrentPiece(row, col);
            boardManager.updateBoard();
            piecesBoardManager.updateAvailability();
            selectedPieceManager.clear();

            if (win) {
                gameEnded = true;
                GameUIUtils.showAlert("Victory", isOnlineGame ? "You win!" : "Congratulations! You win!");
                disableAll();

                if (isOnlineGame) {
                    client.notifyIWon();                      // אתה המנצח
                }

                performExit();
                return;
            } else if (game.isBoardFull()) {
                gameEnded = true;
                GameUIUtils.showAlert("Game Over", "It's a draw!");
                disableAll();
                if (isOnlineGame) {
                    client.notifyIDraw();
                }
                performExit();
                return;
            }

            // Continue to next phase
            waitingForPlacement = false;
            waitingForPieceChoice = !isOnlineGame;

            if (!isOnlineGame) {
                turnLabel.setText("Pick a Piece!");
            } else {
                turnLabel.setText("Pick a Piece!");
            }

            piecesBoard.setDisable(false);
            gameBoard.setDisable(true);
        } catch (Exception ex) {
            GameUIUtils.showAlert("Error", ex.getMessage());
        }
    }

    /**
     * Handles the computer's move: choosing a cell and selecting the next piece for the player.
     */
    private void computerMove() {
        int[] bestPlacement = ComputerAI.findBestPlacement(game);
        if (bestPlacement[0] == -1) return; // No valid moves

        try {
            boolean win = game.placeCurrentPiece(bestPlacement[0], bestPlacement[1]);
            boardManager.updateBoard();
            piecesBoardManager.updateAvailability();

            if (win) {
                gameEnded = true;
                GameUIUtils.showAlert("Game Over", "Computer wins!");
                disableAll();
                return;
            } else if (game.isBoardFull()) {
                gameEnded = true;
                GameUIUtils.showAlert("Game Over", "It's a draw!");
                disableAll();
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        // Choose the next piece for the player
        int bestPiece = ComputerAI.chooseBestPieceForOpponent(game);

        try {
            game.setCurrentPiece(bestPiece);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        selectedPieceManager.displaySelectedPiece(bestPiece);
        waitingForPlacement = true;
        waitingForPieceChoice = false;

        piecesBoard.setDisable(true);
        gameBoard.setDisable(false);
        turnLabel.setText("Place a Piece!");
    }

    /**
     * Utility function to randomly choose an available piece ID from the board.
     */
    private int chooseRandomAvailablePiece() {
        int piece = -1;
        while (piece == -1) {
            int candidate = random.nextInt(16);
            if (game.getAvailablePieces()[candidate]) {
                piece = candidate;
            }
        }
        return piece;
    }

    /**
     * Flags that the opponent disconnected mid-game.
     */
    public void setOpponentDisconnected(boolean value) {
        this.opponentDisconnected = value;
    }

    /**
     * Called when the opponent places a piece on the board (online).
     */
    @Override
    public void opponentPlacedPiece(int row, int col) {
        Platform.runLater(() -> {
            try {
                gameReallyStarted = true;

                boolean win = game.placeCurrentPiece(row, col);
                boardManager.updateBoard();
                piecesBoardManager.updateAvailability();

                if (win) {
                    gameEnded = true;
                    GameUIUtils.showAlert("Game Over", "Opponent Wins!");
                    disableAll();
                    client.notifyILoss();
                    performExit();
                    return;
                } else if (game.isBoardFull()) {
                    gameEnded = true;
                    GameUIUtils.showAlert("Game Over", "It's a Draw!");
                    disableAll();
                    client.notifyIDraw();
                    performExit();
                    return;
                }

                // Update game state for player's turn
                waitingForPlacement = false;
                waitingForPieceChoice = true;
                myTurn = true;

                turnLabel.setText("Opponent's turn");
                selectedPieceManager.clear();

                // First player disables board until receiving next piece
                if (isFirstPlayer) {
                    gameBoard.setDisable(true);
                }
            } catch (Exception ex) {
                GameUIUtils.showAlert("Error", ex.getMessage());
            }
        });
    }

    /**
     * Called when the opponent chooses a piece for the player to place (online).
     */
    @Override
    public void opponentChosePiece(int pieceId) {
        Platform.runLater(() -> {
            try {
                gameReallyStarted = true;

                game.setCurrentPiece(pieceId);
                selectedPieceManager.displaySelectedPiece(pieceId);
                waitingForPlacement = true;
                waitingForPieceChoice = false;
                myTurn = true;

                turnLabel.setText("Place a Piece!");
                gameBoard.setDisable(false);
                piecesBoard.setDisable(true);
            } catch (Exception ex) {
                GameUIUtils.showAlert("Error", ex.getMessage());
            }
        });
    }


    /**
     * Sets up the initial state for an online game, depending on who starts first.
     */
    @Override
    public void startOnlineGameAcourdingToMyTurne(boolean isMyTurn) {

        isFirstPlayer = isMyTurn;
        myTurn = isMyTurn;

        Platform.runLater(() -> {
            if (isMyTurn) {
                // Player starts: enable piece selection
                waitingForPlacement = false;
                waitingForPieceChoice = true;
                piecesBoard.setDisable(false);
                gameBoard.setDisable(true);


                turnLabel.setText("Pick a Piece!");

            } else {
                // Opponent starts: wait for them to choose a piece
                waitingForPlacement = false;
                waitingForPieceChoice = false;
                piecesBoard.setDisable(true);
                gameBoard.setDisable(true);

                turnLabel.setText("Opponent's turn");
            }
        });
    }

    /**
     * Called when the opponent quits the game. Shows win message and updates stats.
     */
    public void opponentQuitAndYouWon() {
        Platform.runLater(() -> {
            gameEnded = true;

            GameUIUtils.showAlert("Victory", "Your opponent quit. You win");

            // End the game and update stats
            disableAll();
            if (client != null) client.notifyIWon();
            goBackToMainMenu();
        });
    }
}
