package org.example.onlinemultiplayercheckers.ui;

import org.example.onlinemultiplayercheckers.GameView;
import org.example.onlinemultiplayercheckers.db.DatabaseManager;
import org.example.onlinemultiplayercheckers.network.NetworkManager;

import javafx.application.Application;
import javafx.application.Platform;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;

import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.URL;
import java.util.Scanner;

public class CheckersApp extends Application {

    private Stage primaryStage;
    private static DatabaseManager db;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        db = new DatabaseManager();
        stage.setTitle("Online Multiplayer Checkers");
        stage.setOnCloseRequest(e -> {
            db.close();
            Platform.exit();
        });
        showLobby();
        stage.show();
    }

    public void showLobby() {
        VBox root = new VBox(18);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color:#1a1a2e;");

        Label title = new Label("♟ CHECKERS");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 48));
        title.setTextFill(Color.web("#e94560"));

        Label subtitle = new Label("Online Multiplayer");
        subtitle.setFont(Font.font("Georgia", FontPosture.ITALIC, 18));
        subtitle.setTextFill(Color.web("#aaaacc"));

        TextField nameField = new TextField("Player");
        nameField.setMaxWidth(260);
        nameField.setStyle("-fx-background-color:#16213e;-fx-text-fill:white;-fx-border-color:#e94560;-fx-border-radius:4;-fx-background-radius:4;-fx-padding:8;");

        Button hostBtn = styledBtn("HOST GAME", "#e94560");
        Label portLabel = new Label("Listening on port 55201...");
        portLabel.setVisible(false);
        portLabel.setTextFill(Color.web("#aaaacc"));

        hostBtn.setOnAction(e -> {
            portLabel.setVisible(true);
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) playerName = "Player";
            startAsHost(playerName);
        });

        HBox joinBox = new HBox(8);
        joinBox.setAlignment(Pos.CENTER);
        TextField ipField = new TextField("localhost");
        ipField.setMaxWidth(180);
        ipField.setStyle("-fx-background-color:#16213e;-fx-text-fill:white;-fx-border-color:#0f3460;-fx-border-radius:4;-fx-background-radius:4;-fx-padding:8;");

        Button joinBtn = styledBtn("JOIN", "#0f3460");
        joinBtn.setMinWidth(80);
        joinBtn.setOnAction(e -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) playerName = "Player";
            startAsClient(playerName, ipField.getText().trim());
        });

        joinBox.getChildren().addAll(ipField, joinBtn);

        Button localBtn = styledBtn("LOCAL 2-PLAYER", "#533483");
        localBtn.setOnAction(e -> {
            String playerName = nameField.getText().trim();
            if (playerName.isEmpty()) playerName = "Player";
            startLocalGame(playerName);
        });

        Button statsBtn = styledBtn("VIEW STATS", "#222255");
        statsBtn.setOnAction(e -> showStats());

        Label joinLabel = new Label("--- or join ---");
        joinLabel.setTextFill(Color.web("#666688"));

        root.getChildren().addAll(title, subtitle, nameField, hostBtn, portLabel, joinLabel, joinBox,
                new Separator(), localBtn, statsBtn);

        Scene scene = new Scene(root, 480, 560);
        primaryStage.setScene(scene);
    }

    private Button styledBtn(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color:" + color + ";-fx-text-fill:white;-fx-font-weight:bold;-fx-padding:10 24;-fx-background-radius:4;-fx-cursor:hand;");
        button.setMinWidth(220);
        return button;
    }

    private void startAsHost(String name) {
        try {
            GameView gv = new GameView(primaryStage, null, true, name, "Opponent", db, this);
            NetworkManager net = new NetworkManager(gv);
            gv.setNetwork(net);
            net.startServer(NetworkManager.DEFAULT_PORT);

            String localIP = getLocalIPAddress();
            String publicIP = getPublicIPAddress();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Hosted");
            alert.setHeaderText("Share these details with your friend:");
            alert.setContentText("📡 Same WiFi (Local):\n" + localIP + ":" + NetworkManager.DEFAULT_PORT + "\n\n" +
                    "🌍 Internet Play (use ngrok):\n" + publicIP + "\n\n" +
                    "💡 For internet play, run: ngrok tcp " + NetworkManager.DEFAULT_PORT);
            alert.show();

            Scene scene = new Scene(gv.getRoot(), 900, 700);
            primaryStage.setScene(scene);
        } catch (Exception ex) {
            showError("Failed to start server: " + ex.getMessage());
        }
    }

    private void startAsClient(String name, String host) {
        try {
            GameView gv = new GameView(primaryStage, null, false, name, "Host", db, this);
            NetworkManager net = new NetworkManager(gv);
            gv.setNetwork(net);
            net.connectToServer(host, NetworkManager.DEFAULT_PORT);
            Scene scene = new Scene(gv.getRoot(), 900, 700);
            primaryStage.setScene(scene);
        } catch (Exception ex) {
            showError("Cannot connect to " + host + ": " + ex.getMessage());
        }
    }

    private void startLocalGame(String name) {
        GameView gv = new GameView(primaryStage, null, false, name, "Player 2", db, this);
        Scene scene = new Scene(gv.getRoot(), 900, 700);
        primaryStage.setScene(scene);
    }

    public void showStats() {
        StatsView sv = new StatsView(db, this);
        Scene scene = new Scene(sv.getRoot(), 600, 500);
        primaryStage.setScene(scene);
    }

    private String getLocalIPAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private String getPublicIPAddress() {
        try {
            URL url = new URL("https://checkip.amazonaws.com");
            Scanner s = new Scanner(url.openStream());
            String ip = s.nextLine().trim();
            s.close();
            return ip;
        } catch (Exception e) {
            return "Use ngrok - see instructions";
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}