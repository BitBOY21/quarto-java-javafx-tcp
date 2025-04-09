package client.network;

import java.io.*;
import java.net.*;

/**
 * TCPClient manages the connection between a client and the server.
 * It handles sending/receiving messages, processing commands, and interacting with the game session.
 */
public class TCPClient {

    private String userName;
    private static final int port = 1234;
    private static final String serverAddress = "localhost";
    private BufferedReader input;
    private PrintWriter output;
    private Socket socket;
    private java.util.function.Consumer<String> chatListener;

    // Response lock for synchronous communication
    private String response;
    private final Object lock = new Object();

    private GameSessionListener gameSessionListener;

    // Flags to handle connection termination
    private boolean hasHandledOpponentLeft = false;
    private boolean gameEndedGracefully = false;

    public void setGameSessionListener(GameSessionListener listener) {
        this.gameSessionListener = listener;
    }

    public TCPClient(String name) {
        userName = name;
        try {
            socket = new Socket(serverAddress, port);
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start background listener thread to handle incoming messages
        Thread listener = new Thread(() -> {
            try {
                String msg;
                while ((msg = input.readLine()) != null) {
                    processCommand(msg);
                }
            } catch (SocketException e) {
                System.out.println("TCPClient: Socket closed gracefully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        listener.setDaemon(true);
        listener.start();
    }

    /**
     * Processes messages received from the server based on protocol commands.
     */
    private void processCommand(String message) {
        String[] parts = message.split(" ", 2);
        String command = parts[0];
        String data = (parts.length > 1) ? parts[1] : "";

        switch (command) {
            case "isNameExistInDB":
            case "isPasswordCorrect":
            case "registerUser":
            case "getAllPlayers":
                setResponse(data);
                break;

            case "startOnlineGameMyTurn":
                if (gameSessionListener != null) {
                    gameSessionListener.startOnlineGameAcourdingToMyTurne(true);
                }
                break;


            case "startOnlineGameWait":
                System.out.println("Server: " + data);
                if (gameSessionListener != null) {
                    gameSessionListener.startOnlineGameAcourdingToMyTurne(false);
                }
                break;

            case "movePlace":
                try {
                    String[] tokens = data.split(" ");
                    int row = Integer.parseInt(tokens[0]);
                    int col = Integer.parseInt(tokens[1]);
                    if (gameSessionListener != null) {
                        gameSessionListener.opponentPlacedPiece(row, col);
                    }
                } catch (Exception ex) {
                    System.err.println("Error processing movePlace: " + ex.getMessage());
                }
                break;

            case "moveChoose":
                try {
                    int pieceId = Integer.parseInt(data.trim());
                    if (gameSessionListener != null) {
                        gameSessionListener.opponentChosePiece(pieceId);
                    }
                } catch (Exception ex) {
                    System.err.println("Error processing moveChoose: " + ex.getMessage());
                }
                break;

            case "iLossGame":
                System.out.println("Server registered loss: " + data);
                gameEndedGracefully = true;
                break;

            case "iWonGame":
                System.out.println("Server registered win: " + data);
                gameEndedGracefully = true;
                break;

            case "iDrawGame":
                System.out.println("Server registered draw: " + data);
                gameEndedGracefully = true;
                break;

            case "Error:":
                System.err.println("Server error: " + data);
                setResponse("Error:" + data);
                break;

            case "opponentLeft":
                if (hasHandledOpponentLeft || gameEndedGracefully) return;
                hasHandledOpponentLeft = true;

                System.out.println("Your opponent has left the game.");
                javafx.application.Platform.runLater(() -> {
                    if (gameSessionListener instanceof client.controllers.GameEngine engine) {
                        engine.setOpponentDisconnected(true);
                        engine.opponentQuitAndYouWon(); // 🆕 מבצע את כל הטיפול
                    }
                });
                break;

            case "chat":
                if (chatListener != null) {
                    chatListener.accept(data);
                }
                break;

            default:
                System.out.println("Unknown server response: " + message);
                setResponse(message);
                break;
        }
    }

    /**
     * Synchronized setter for server responses.
     */
    private void setResponse(String data) {
        synchronized (lock) {
            response = data;
            lock.notifyAll();
        }
    }

    /**
     * Blocks the current thread until a server response is received.
     */
    private void waitForResponse() {
        synchronized (lock) {
            while (response == null) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ----------------------------
    // Synchronous Request Methods
    // ----------------------------

    public boolean isNameExistInDB(String name) {
        sendMessage("isNameExistInDB " + name);
        waitForResponse();
        boolean result = Boolean.parseBoolean(response);
        response = null;
        return result;
    }

    public boolean isPasswordCorrect(String password) {
        sendMessage("isPasswordCorrect " + password);
        waitForResponse();
        boolean result = Boolean.parseBoolean(response);
        response = null;
        return result;
    }

    public void registerUser(String userName, String password) {
        sendMessage("registerUser " + userName + " " + password);
        waitForResponse();
        System.out.println("Registration result: " + response);
        response = null;
    }

    public void startGameSessionWithServer() {
        sendMessage("startOnlineGame " + userName);
    }

    public void notifyILoss() {
        sendMessage("iLossGame");
        gameEndedGracefully = true;
    }

    public void notifyIWon() {
        sendMessage("iWonGame");
        gameEndedGracefully = true;
    }

    public void notifyIDraw() {
        sendMessage("iDrawGame");
        gameEndedGracefully = true;
    }

    public String getPlayersData() {
        sendMessage("getAllPlayers");
        waitForResponse();
        String playersData = response;
        response = null;
        return playersData;
    }

    public void sendMessage(String message) {
        if (output != null) {
            output.println(message);
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        userName = name;
    }

    public void close() throws IOException {
        if (socket != null) socket.close();
        if (input != null) input.close();
        if (output != null) output.close();
    }

    public void notifyDisconnect() {
        sendMessage("disconnect");
    }

    public void setChatListener(java.util.function.Consumer<String> listener) {
        this.chatListener = listener;
    }

}
