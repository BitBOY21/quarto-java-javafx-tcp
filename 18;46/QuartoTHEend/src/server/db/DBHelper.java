package server.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import client.models.Player;

/**
 * DBHelper provides static utility methods to interact with the MySQL database
 * for user registration, authentication, and player statistics management.
 */
public class DBHelper {

    // Update the URL, USER, and PASSWORD with your MySQL configuration.
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/quarto_db";
    private static final String USER = "root";
    private static final String PASSWORD = "123456789";

    /**
     * Establishes a connection to the MySQL database.
     */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Checks if a user already exists in the database.
     *
     * @param username the username to check
     * @return true if the user exists, false otherwise
     */
    public static boolean userExists(String username) {
        String sql = "SELECT COUNT(*) FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                int count = rs.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Validates the password for the given username.
     *
     * @param username the username to check
     * @param password the password to validate
     * @return true if password matches, false otherwise
     */
    public static boolean checkPassword(String username, String password) {
        String sql = "SELECT password FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()){
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Registers a new user in the database if username is available.
     *
     * @param username the new username
     * @param password the password to store
     * @return true if registration succeeded, false otherwise
     */
    public static boolean registerUser(String username, String password) {
        if (userExists(username)) {
            return false;
        }
        String sql = "INSERT INTO players(username, password, win_percentage, games_played, ranking) VALUES(?, ?, 0, 0, 0)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all players from the database including calculated losses.
     *
     * @return list of players
     */
    public static List<Player> getAllPlayers() {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT username, win_percentage, games_played, ranking, wins, draws, " +
                "(games_played - wins - draws) AS losses FROM players ORDER BY ranking DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                double winPerc = rs.getDouble("win_percentage");
                int gamesPlayed = rs.getInt("games_played");
                double ranking = rs.getDouble("ranking");
                int wins = rs.getInt("wins");
                int draws = rs.getInt("draws");
                int losses = rs.getInt("losses");

                players.add(new Player(username, winPerc, gamesPlayed, ranking, wins, draws, losses));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    /**
     * Updates the database with a new win for the specified player.
     */
    public static boolean updateLoss(String username) {
        String selectSql = "SELECT games_played, win_percentage, wins, draws, losses FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, username);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                int gamesPlayed = rs.getInt("games_played");
                int wins = rs.getInt("wins");
                int draws = rs.getInt("draws");
                int losses = rs.getInt("losses");
                int newGamesPlayed = gamesPlayed + 1;
                int newLosses = losses + 1;

                double newWinPerc = (newGamesPlayed > 0) ? ((double) wins / newGamesPlayed) * 100.0 : 0;
                double newRanking = (newGamesPlayed > 0) ? newWinPerc * (1 + Math.log(newGamesPlayed)) : 0;

                String updateSql = "UPDATE players SET games_played = ?, win_percentage = ?, ranking = ?, losses = ? WHERE username = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, newGamesPlayed);
                    updateStmt.setDouble(2, newWinPerc);
                    updateStmt.setDouble(3, newRanking);
                    updateStmt.setInt(4, newLosses);
                    updateStmt.setString(5, username);
                    int rowsAffected = updateStmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the database with a new draw for the specified player.
     */
    public static boolean updateDraw(String username) {
        String selectSql = "SELECT games_played, wins, draws FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, username);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                int gamesPlayed = rs.getInt("games_played");
                int wins = rs.getInt("wins");
                int draws = rs.getInt("draws"); // ✅ חדש

                int newGamesPlayed = gamesPlayed + 1;
                int newDraws = draws + 1; // ✅ חדש

                double newWinPerc = (newGamesPlayed > 0) ? ((double) wins / newGamesPlayed) * 100.0 : 0;
                double newRanking = (newGamesPlayed > 0) ? newWinPerc * (1 + Math.log(newGamesPlayed)) : 0;

                String updateSql = "UPDATE players SET games_played = ?, win_percentage = ?, ranking = ?, draws = ? WHERE username = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, newGamesPlayed);
                    updateStmt.setDouble(2, newWinPerc);
                    updateStmt.setDouble(3, newRanking);
                    updateStmt.setInt(4, newDraws);        // ✅ חדש
                    updateStmt.setString(5, username);     // ⚠️ שם עבר להיות אינדקס 5
                    int rowsAffected = updateStmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the database with a new loss for the specified player.
     */
    public static boolean updateWin(String username) {
        String selectSql = "SELECT games_played, win_percentage, wins FROM players WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, username);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                int gamesPlayed = rs.getInt("games_played");
                double winPerc = rs.getDouble("win_percentage");
                int wins = rs.getInt("wins");
                // Increment wins and games played.
                int newGamesPlayed = gamesPlayed + 1;
                int newWins = wins + 1;
                double newWinPerc = (newGamesPlayed > 0) ? ((double) newWins / newGamesPlayed) * 100.0 : 0;
                // Calculate new ranking:
                double newRanking = (newGamesPlayed > 0) ? newWinPerc * (1 + Math.log(newGamesPlayed)) : 0;

                String updateSql = "UPDATE players SET games_played = ?, wins = ?, win_percentage = ?, ranking = ? WHERE username = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, newGamesPlayed);
                    updateStmt.setInt(2, newWins);
                    updateStmt.setDouble(3, newWinPerc);
                    updateStmt.setDouble(4, newRanking);
                    updateStmt.setString(5, username);
                    int rowsAffected = updateStmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
