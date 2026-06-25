package org.example.onlinemultiplayercheckers.db;

import org.example.onlinemultiplayercheckers.model.Move;
import java.sql.*;
import java.util.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:checkers.db";
    private Connection conn;

    public DatabaseManager() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            initTables();
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            System.err.println("DB init failed: " + e.getMessage());
        }
    }

    private void initTables() throws SQLException {
        Statement s = conn.createStatement();

        s.execute("CREATE TABLE IF NOT EXISTS WinLossRecord (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_name TEXT NOT NULL, " +
                "wins INTEGER DEFAULT 0, " +
                "losses INTEGER DEFAULT 0, " +
                "draws INTEGER DEFAULT 0)");

        s.execute("CREATE TABLE IF NOT EXISTS GameHistory (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "player_red TEXT, " +
                "player_black TEXT, " +
                "winner TEXT, " +
                "total_moves INTEGER DEFAULT 0, " +
                "played_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        s.execute("CREATE TABLE IF NOT EXISTS ReplayMoves (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "game_id INTEGER NOT NULL, " +
                "move_number INTEGER NOT NULL, " +
                "move_data TEXT NOT NULL)");

        s.close();
        System.out.println("Tables created/verified successfully!");
    }

    public int startGame(String playerRed, String playerBlack) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO GameHistory (player_red, player_black, total_moves) VALUES (?,?,0)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, playerRed);
            ps.setString(2, playerBlack);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                System.out.println("Game started with ID: " + id);
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void saveMove(int gameId, int moveNumber, Move move) {
        if (move == null || gameId == -1) return;
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO ReplayMoves (game_id, move_number, move_data) VALUES (?,?,?)"
            );
            ps.setInt(1, gameId);
            ps.setInt(2, moveNumber);
            ps.setString(3, move.serialize());
            ps.executeUpdate();
            System.out.println("Move " + moveNumber + " saved for game " + gameId);
        } catch (SQLException e) {
            System.err.println("Error saving move: " + e.getMessage());
        }
    }

    public void finishGame(int gameId, String winner, int totalMoves) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE GameHistory SET winner=?, total_moves=? WHERE id=?"
            );
            ps.setString(1, winner);
            ps.setInt(2, totalMoves);
            ps.setInt(3, gameId);
            ps.executeUpdate();
            System.out.println("Game " + gameId + " finished. Winner: " + winner + ", Moves: " + totalMoves);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateWinLoss(String name, boolean won, boolean draw) {
        try {
            PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM WinLossRecord WHERE player_name=?"
            );
            check.setString(1, name);
            ResultSet rs = check.executeQuery();

            if (!rs.next()) {
                PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO WinLossRecord (player_name, wins, losses, draws) VALUES (?,0,0,0)"
                );
                ins.setString(1, name);
                ins.executeUpdate();
                System.out.println("New player added: " + name);
            }

            String col;
            if (draw) {
                col = "draws";
            } else if (won) {
                col = "wins";
            } else {
                col = "losses";
            }

            PreparedStatement upd = conn.prepareStatement(
                    "UPDATE WinLossRecord SET " + col + " = " + col + " + 1 WHERE player_name=?"
            );
            upd.setString(1, name);
            upd.executeUpdate();
            System.out.println("Updated " + name + " - " + col + ": +1");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int[] getWinLoss(String name) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT wins, losses, draws FROM WinLossRecord WHERE player_name=?"
            );
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new int[]{rs.getInt("wins"), rs.getInt("losses"), rs.getInt("draws")};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{0, 0, 0};
    }

    public double getAverageGameLength() {
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT AVG(total_moves) FROM GameHistory WHERE winner IS NOT NULL"
            );
            if (rs.next()) {
                double avg = rs.getDouble(1);
                System.out.println("Average game length: " + avg);
                return avg;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<String> getReplayMoves(int gameId) {
        List<String> moves = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT move_data FROM ReplayMoves WHERE game_id=? ORDER BY move_number"
            );
            ps.setInt(1, gameId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                moves.add(rs.getString("move_data"));
            }
            System.out.println("Loaded " + moves.size() + " moves for game " + gameId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return moves;
    }

    public List<int[]> getRecentGames() {
        List<int[]> games = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT id, total_moves, winner FROM GameHistory ORDER BY id DESC LIMIT 10"
            );
            while (rs.next()) {
                games.add(new int[]{rs.getInt("id"), rs.getInt("total_moves")});
                System.out.println("Game #" + rs.getInt("id") + " - Moves: " + rs.getInt("total_moves") + " - Winner: " + rs.getString("winner"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return games;
    }

    public void printAllStats() {
        System.out.println("\n========== STATISTICS ==========");
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT * FROM WinLossRecord"
            );
            System.out.println("--- Win/Loss Record ---");
            while (rs.next()) {
                System.out.println("Player: " + rs.getString("player_name") +
                        " | Wins: " + rs.getInt("wins") +
                        " | Losses: " + rs.getInt("losses") +
                        " | Draws: " + rs.getInt("draws"));
            }

            ResultSet rs2 = conn.createStatement().executeQuery(
                    "SELECT * FROM GameHistory ORDER BY id DESC LIMIT 5"
            );
            System.out.println("\n--- Recent Games ---");
            while (rs2.next()) {
                System.out.println("Game #" + rs2.getInt("id") +
                        " | " + rs2.getString("player_red") + " vs " + rs2.getString("player_black") +
                        " | Winner: " + rs2.getString("winner") +
                        " | Moves: " + rs2.getInt("total_moves"));
            }
            System.out.println("================================\n");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (conn != null) {
                conn.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}