package client.network;

/**
 * A utility class to manage the current TCP connection and game session context.
 * Stores whether the game is online and the current player's username.
 */
public class ConnectionManager {
    private static TCPClient tcpClient;
    private static boolean isOnlineGame = false;
    private static String currentUsername; // 🆕

    /**
     * Gets the active TCPClient instance used for communication.
     * @return the TCPClient
     */
    public static TCPClient getTcpClient() {
        return tcpClient;
    }

    /**
     * Sets the active TCPClient instance.
     * @param client the TCPClient to set
     */
    public static void setTcpClient(TCPClient client) {
        tcpClient = client;
    }

    /**
     * Checks if the game is currently in online mode.
     * @return true if online, false if offline
     */
    public static boolean getIsOnlineGame() {
        return isOnlineGame;
    }

    /**
     * Sets the game mode to online or offline.
     * @param flag true for online mode, false for offline
     */
    public static void setIsOnlineGame(boolean flag) {
        isOnlineGame = flag;
    }

    /**
     * Sets the username of the current player.
     * @param name the username to set
     */
    public static void setCurrentUsername(String name) { // 🆕
        currentUsername = name;
    }

    /**
     * Gets the current player's username.
     * @return the username
     */
    public static String getCurrentUsername() { // 🆕
        return currentUsername;
    }
}
