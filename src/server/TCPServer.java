package server;

import java.io.*;
import java.net.*;
import client.models.Player;
import server.db.DBHelper;
import java.util.List;

/**
 * TCPServer handles incoming client connections and manages game sessions.
 * Each client is handled in a dedicated thread using a nested ClientHandler class.
 */
public class TCPServer {

    private static final int port = 1234;
    private static ClientHandler waitingClient = null;     // Holds a single client waiting to be matched

    public static void main(String[] args) {
        new TCPServer().startServer();
    }

    /**
     * Starts the TCP server on the specified port and listens for clients.
     */
    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("TCPServer is listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles individual client sessions, including authentication, gameplay, and messaging.
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;
        private String username;
        private ClientHandler opponent;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            try {
                input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                output = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                System.err.println("Error initializing client handler: " + e.getMessage());
            }
        }

        @Override
        public void run() {
            String clientMessage;
            try {
                while ((clientMessage = input.readLine()) != null) {
                    System.out.println("Received: " + clientMessage);
                    processCommand(clientMessage);
                }
            } catch (IOException e) {
                System.out.println("Connection with client " + clientSocket.getInetAddress() + " closed.");
            } finally {
                closeConnection();
            }
        }

        /**
         * Processes client messages by dispatching commands and managing gameplay actions.
         */
        private void processCommand(String message) {
            String[] parts = message.split(" ");
            String command = parts[0];

            switch (command) {
                case "isNameExistInDB":
                    if (parts.length < 2) {
                        output.println("isNameExistInDB Error: Missing username.");
                        break;
                    }
                    String name = parts[1];
                    boolean exists = DBHelper.userExists(name);
                    this.username = name;
                    output.println("isNameExistInDB " + exists);
                    break;

                case "isPasswordCorrect":
                    if (this.username == null) {
                        output.println("isPasswordCorrect Error: Username not set.");
                    } else if (parts.length < 2) {
                        output.println("isPasswordCorrect Error: Missing password.");
                    } else {
                        String password = parts[1];
                        boolean correct = DBHelper.checkPassword(this.username, password);
                        output.println("isPasswordCorrect " + correct);
                    }
                    break;

                case "registerUser":
                    if (parts.length < 3) {
                        output.println("registerUser Error: Missing username or password.");
                    } else {
                        String regUsername = parts[1];
                        String regPassword = parts[2];
                        boolean registered = DBHelper.registerUser(regUsername, regPassword);
                        if (registered) {
                            this.username = regUsername;
                            output.println("registerUser success");
                        } else {
                            output.println("registerUser failed");
                        }
                    }
                    break;

                case "getAllPlayers":
                    List<Player> playersList = DBHelper.getAllPlayers();
                    StringBuilder responseBuilder = new StringBuilder();
                    for (Player player : playersList) {
                        responseBuilder.append(player.getUsername())
                                .append(",").append(player.getWinPercentage())
                                .append(",").append(player.getGamesPlayed())
                                .append(",").append(player.getRanking())
                                .append(",").append(player.getWins())
                                .append(",").append(player.getDraws())
                                .append(",").append(player.getLosses())
                                .append(";");
                    }
                    output.println("getAllPlayers " + responseBuilder.toString());
                    break;

                case "startOnlineGame":
                    if (this.username == null && parts.length >= 2) {
                        this.username = parts[1];
                    }

                    if (waitingClient == null || waitingClient.equals(this)) {
                        waitingClient = this;
                        output.println("startOnlineGameWait Waiting for opponent...");
                    } else {
                        ClientHandler opponentClient = this;
                        startSession(waitingClient, opponentClient);
                        waitingClient = null;
                    }
                    break;

                case "movePlace":
                    if (opponent != null) {
                        opponent.sendMessage("movePlace " + joinParts(parts, 1));
                    }
                    break;

                case "moveChoose":
                    if (opponent != null) {
                        opponent.sendMessage("moveChoose " + joinParts(parts, 1));
                    }
                    break;

                case "iWonGame":

                    System.out.println("iWonGame called for username: " + this.username);

                    boolean isWinUpdated = DBHelper.updateWin(this.username);
                    System.out.println("Server: [" + username + "] registered WIN: " + isWinUpdated);
                    output.println("iWonGame " + (isWinUpdated ? "success" : "failed"));
                    break;

                case "iLossGame":
                    if (opponent == null) {
                        System.out.println("Skipping loss registration – no opponent yet.");
                        break;
                    }
                    boolean isLossUpdated = DBHelper.updateLoss(this.username);
                    System.out.println("Server: [" + username + "] registered LOSS: " + isLossUpdated);
                    output.println("iLossGame " + (isLossUpdated ? "success" : "failed"));
                    break;

                case "iDrawGame":
                    boolean isDrawUpdated = DBHelper.updateDraw(this.username);
                    System.out.println("Server: [" + username + "] registered DRAW: " + isDrawUpdated);
                    output.println("iDrawGame " + (isDrawUpdated ? "success" : "failed"));
                    break;

                case "disconnect":
                    System.out.println(username + " disconnected via command.");
                    closeConnection();
                    break;

                case "chat":
                    if (opponent != null) {
                        String chatMessage = joinParts(parts, 1);
                        opponent.sendMessage("chat " + username + ": " + chatMessage);
                    }
                    break;

                case "opponentWonByQuit":
                    if (opponent != null) {
                        opponent.output.println("opponentLeft"); // ✅ שינוי כאן!

                    }
                    break;

                default:
                    output.println("Error: Unknown command: " + command);
                    break;

            }
        }

        /**
         * Utility method to reconstruct the remainder of a command string.
         */
        private String joinParts(String[] parts, int start) {
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < parts.length; i++) {
                sb.append(parts[i]);
                if (i < parts.length - 1) sb.append(" ");
            }
            return sb.toString();
        }

        /**
         * Initiates a game session between two connected clients.
         */
        private void startSession(ClientHandler client1, ClientHandler client2) {
            try {
                client1.opponent = client2;
                client2.opponent = client1;
                boolean player1Starts = Math.random() < 0.5;
                if (player1Starts) {
                    client1.output.println("startOnlineGameMyTurn Match found! You are Player 1");
                    client2.output.println("startOnlineGameWait Match found! You are Player 2");
                } else {
                    client1.output.println("startOnlineGameWait Match found! You are Player 1");
                    client2.output.println("startOnlineGameMyTurn Match found! You are Player 2");
                }

            } catch (Exception e) {
                System.out.println("client.game session failed: " + e.getMessage());
            }
        }

        /**
         * Sends a text message to the connected client.
         */
        public void sendMessage(String message) {
            if (output != null) output.println(message);
        }

        /**
         * Closes all streams and notifies opponent if connection drops.
         */
        private void closeConnection() {
            try {
                System.out.println("Closing connection for " + username);
                if (this == waitingClient) waitingClient = null;


                if (opponent != null) {
                    System.out.println("Disconnecting: Notifying opponentLeft");
                    opponent.sendMessage("opponentLeft");
                    opponent.opponent = null;
                    this.opponent = null;
                } else {
                    System.out.println("Disconnecting: No opponent to notify.");
                }

                if (input != null) input.close();
                if (output != null) output.close();
                if (clientSocket != null) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
