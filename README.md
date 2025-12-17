# Quarto Multiplayer Game 

This is an academic project implementing the **Quarto** board game in Java using **JavaFX** for the user interface, **TCP sockets** for multiplayer communication, and **MySQL** for player statistics.

The system supports:
- **Offline mode**: Play against a computer opponent (AI)
- **Online mode**: Play against another player over a network, including real-time chat

## 🎮 Screenshots

### 🧩 Main Menu
<img src="images/Main Menu.png" width="250"/>

### 🌐 Online Mode 
<img src="images/Online Mode.png" width="500"/>



## 🧩 System Requirements

- Java JDK 17 or higher
- JavaFX SDK 
- MySQL Server 
- Any Java-supporting IDE or command-line environment


## ⚙️ Setup Instructions

1. Make sure you have:
    - Java JDK 17 or higher installed
    - JavaFX SDK configured in your classpath/module path
    - MySQL Server running locally
   
2. Set up the MySQL Database:
     - Create a MySQL database named `quarto_db`
     - Run the script `quarto_db.sql`
     - Set your DB credentials in `DBHelper.java`

4. To run:
    - First launch `TCPServer.java` (for online games)
    - Then launch `Main.java` to start the game (offline or online)











