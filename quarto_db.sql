
-- Create the Quarto game database
CREATE DATABASE IF NOT EXISTS quarto_db;
USE quarto_db;

-- Create players table
CREATE TABLE IF NOT EXISTS players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    games_played INT DEFAULT 0,
    wins INT DEFAULT 0,
    draws INT DEFAULT 0,
    losses INT DEFAULT 0,
    win_percentage DOUBLE DEFAULT 0.0,
    ranking DOUBLE DEFAULT 0.0
);
