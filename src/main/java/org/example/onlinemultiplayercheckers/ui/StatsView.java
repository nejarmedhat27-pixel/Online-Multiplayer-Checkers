package org.example.onlinemultiplayercheckers.ui;

import org.example.onlinemultiplayercheckers.db.DatabaseManager;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

public class StatsView {
    private VBox root;

    public StatsView(DatabaseManager db, CheckersApp app) {
        root = new VBox(16);
        root.setAlignment(Pos.TOP_CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color:#1a1a2e;");

        Label title = new Label("GAME STATISTICS");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#e94560"));

        double avg = db.getAverageGameLength();
        Label avgLabel = new Label(String.format("Average game length: %.1f moves", avg));
        avgLabel.setTextFill(Color.web("#aaaacc"));
        avgLabel.setFont(Font.font("Georgia", 14));

        Label winLossTitle = new Label("WIN/LOSS RECORD");
        winLossTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        winLossTitle.setTextFill(Color.web("#e94560"));

        VBox winLossList = new VBox(4);

        try {
            int[] playerStats = db.getWinLoss("Player");
            Label playerLabel = new Label("Player (You): " + playerStats[0] + " Wins | " + playerStats[1] + " Losses | " + playerStats[2] + " Draws");
            playerLabel.setTextFill(Color.web("#ccccff"));
            playerLabel.setFont(Font.font("Monospace", 12));
            winLossList.getChildren().add(playerLabel);
        } catch (Exception e) {
            Label error = new Label("No data yet");
            error.setTextFill(Color.web("#666688"));
            winLossList.getChildren().add(error);
        }

        Label recentTitle = new Label("RECENT GAMES");
        recentTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        recentTitle.setTextFill(Color.web("#e94560"));

        VBox gameList = new VBox(4);
        var games = db.getRecentGames();
        if (games.isEmpty()) {
            Label none = new Label("No completed games yet. Play a game first!");
            none.setTextFill(Color.web("#666688"));
            gameList.getChildren().add(none);
        } else {
            for (int[] g : games) {
                Label l = new Label("Game #" + g[0] + " --- " + g[1] + " moves");
                l.setTextFill(Color.web("#ccccff"));
                l.setFont(Font.font("Monospace", 12));
                gameList.getChildren().add(l);
            }
        }

        Button back = new Button("← Back to Menu");
        back.setStyle("-fx-background-color:#0f3460;-fx-text-fill:white;-fx-cursor:hand;-fx-background-radius:4;-fx-padding:8 20;");
        back.setOnAction(e -> app.showLobby());

        root.getChildren().addAll(title, avgLabel, new Separator(), winLossTitle, winLossList,
                new Separator(), recentTitle, gameList, back);

        // Print all stats to console for debugging
        db.printAllStats();
    }

    public VBox getRoot() { return root; }
}