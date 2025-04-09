package client.models;

import javafx.beans.property.*;

/**
 * Represents a player and their game statistics.
 * Used for data binding in JavaFX UI.
 */
public class Player {
    private final StringProperty username;
    private final DoubleProperty winPercentage;
    private final IntegerProperty gamesPlayed;
    private final DoubleProperty ranking;
    private final IntegerProperty wins;
    private final IntegerProperty draws;
    private final IntegerProperty losses;

    /**
     * Constructs a Player with full statistical data.
     *
     * @param username      the player's username
     * @param winPercentage the player's win percentage
     * @param gamesPlayed   total games played
     * @param ranking       player's current ranking score
     * @param wins          number of wins
     * @param draws         number of draws
     * @param losses        number of losses
     */
    public Player(String username, double winPercentage, int gamesPlayed, double ranking, int wins, int draws, int losses) {
        this.username = new SimpleStringProperty(username);
        this.winPercentage = new SimpleDoubleProperty(winPercentage);
        this.gamesPlayed = new SimpleIntegerProperty(gamesPlayed);
        this.ranking = new SimpleDoubleProperty(ranking);
        this.wins = new SimpleIntegerProperty(wins);
        this.draws = new SimpleIntegerProperty(draws);
        this.losses = new SimpleIntegerProperty(losses);
    }

    // Properties (used for JavaFX table/view binding)

    public StringProperty usernameProperty() {
        return username;
    }

    public DoubleProperty winPercentageProperty() {
        return winPercentage;
    }

    public IntegerProperty gamesPlayedProperty() {
        return gamesPlayed;
    }

    public DoubleProperty rankingProperty() {
        return ranking;
    }

    public IntegerProperty winsProperty() {
        return wins;
    }

    public IntegerProperty drawsProperty() {
        return draws;
    }

    public IntegerProperty lossesProperty() {
        return losses;
    }

    // Getters

    public String getUsername() {
        return username.get();
    }

    public double getWinPercentage() {
        return winPercentage.get();
    }

    public int getGamesPlayed() {
        return gamesPlayed.get();
    }

    public double getRanking() {
        return ranking.get();
    }

    public int getWins() {
        return wins.get();
    }

    public int getDraws() {
        return draws.get();
    }

    public int getLosses() {
        return losses.get();
    }
}
