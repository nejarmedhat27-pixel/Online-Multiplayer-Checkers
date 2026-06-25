package org.example.onlinemultiplayercheckers;

import org.example.onlinemultiplayercheckers.db.DatabaseManager;
import org.example.onlinemultiplayercheckers.model.Board;
import org.example.onlinemultiplayercheckers.model.Move;
import org.example.onlinemultiplayercheckers.network.MoveValidator;
import org.example.onlinemultiplayercheckers.model.Piece;
import org.example.onlinemultiplayercheckers.network.NetworkManager;

import javafx.application.Platform;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import javafx.stage.Stage;
import org.example.onlinemultiplayercheckers.ui.CheckersApp;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.Optional;

public class GameView implements NetworkManager.NetworkListener {

    private static final int CELL = 72;

    private static final int BOARD_SIZE =
            Board.SIZE * CELL;

    private Board board;

    private NetworkManager network;

    private MoveValidator validator;

    private DatabaseManager db;

    private CheckersApp app;

    private boolean isHost;

    private boolean myTurn;

    private String myName;

    private String opponentName;

    private static final Color DARK_CELL =
            Color.web("#2d4a1e");

    private static final Color LIGHT_CELL =
            Color.web("#f0d9b5");

    private static final Color SELECT_COL =
            Color.web("#f6f669");

    private static final Color VALID_COL =
            Color.web("#cdd16e");

    private static final Color JUMP_COL =
            Color.web("#f6a52d");

    private static final Color RED_COLOR =
            Color.web("#c0392b");

    private static final Color BLK_COLOR =
            Color.web("#1a1a1a");

    private Pane boardPane;

    private BorderPane root;

    private Label statusLabel;

    private Label turnLabel;

    private Label capturedRedLabel;

    private Label capturedBlackLabel;

    private TextArea chatArea;

    private TextField chatInput;

    private VBox moveLog;

    private int selectedRow = -1;

    private int selectedCol = -1;

    private List<Move> validMovesForSelected =
            new ArrayList<>();

    private int gameId;

    private int capturedRed = 0;

    private int capturedBlack = 0;

    private boolean gameOver = false;

    private List<Move> replayMoves =
            new ArrayList<>();

    // Timer variables
    private Timeline timer;
    private int secondsLeft = 60;
    private Label timerLabel;

    // For saving game moves and replay
    private int moveCount = 0;
    private List<Move> gameMoves = new ArrayList<>();

    // For preventing infinite loops in rematch/draw
    private boolean rematchOffered = false;
    private boolean drawOffered = false;

    public GameView(
            Stage stage,
            NetworkManager network,
            boolean isHost,
            String myName,
            String opponentName,
            DatabaseManager db,
            CheckersApp app
    ) {

        this.network = network;
        this.isHost = isHost;
        this.myName = myName;
        this.opponentName = opponentName;
        this.db = db;
        this.app = app;

        board = new Board();
        validator = new MoveValidator();

        myTurn = (network == null) || isHost;

        gameId = db.startGame(
                isHost ? myName : opponentName,
                isHost ? opponentName : myName
        );

        buildUI();
        drawBoard();
        updateStatus();

        if (myTurn) {
            startTimer();
        }
    }

    public void setNetwork(NetworkManager network) {

        this.network = network;
    }

    private void buildUI() {

        root = new BorderPane();

        root.setStyle(
                "-fx-background-color:#1a1a2e;"
        );

        boardPane = new Pane();

        boardPane.setPrefSize(
                BOARD_SIZE,
                BOARD_SIZE
        );

        boardPane.setMaxSize(
                BOARD_SIZE,
                BOARD_SIZE
        );

        root.setCenter(boardPane);

        root.setRight(buildSidePanel());

        BorderPane.setMargin(
                boardPane,
                new Insets(20, 0, 20, 20)
        );

        BorderPane.setMargin(
                root.getRight(),
                new Insets(20)
        );
    }

    private VBox buildSidePanel() {

        VBox panel = new VBox(12);

        panel.setPrefWidth(220);

        panel.setStyle(
                "-fx-background-color:#16213e;" +
                        "-fx-background-radius:10;" +
                        "-fx-padding:15;"
        );

        Label gameTitle =
                new Label("♟ CHECKERS");

        gameTitle.setFont(
                Font.font(
                        "Georgia",
                        FontWeight.BOLD,
                        18
                )
        );

        gameTitle.setTextFill(
                Color.web("#e94560")
        );

        // Timer Label
        timerLabel = new Label("⏱ 60");
        timerLabel.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
        timerLabel.setTextFill(Color.web("#e94560"));

        turnLabel =
                new Label("⬤ BLACK's turn");

        turnLabel.setFont(
                Font.font("Georgia", 13)
        );

        turnLabel.setTextFill(
                Color.web("#aaaacc")
        );

        statusLabel =
                new Label("Waiting...");

        statusLabel.setWrapText(true);

        statusLabel.setTextFill(
                Color.web("#888899")
        );

        Label capTitle =
                new Label("CAPTURED");

        capTitle.setTextFill(
                Color.web("#e94560")
        );

        HBox capBox = new HBox(20);

        capBox.setAlignment(Pos.CENTER);

        capturedRedLabel =
                new Label("🔴 0");

        capturedBlackLabel =
                new Label("⚫ 0");

        capBox.getChildren().addAll(
                capturedRedLabel,
                capturedBlackLabel
        );

        moveLog = new VBox(2);

        ScrollPane logScroll =
                new ScrollPane(moveLog);

        logScroll.setPrefHeight(120);

        Button hintBtn =
                sideBtn("💡 Hint");

        hintBtn.setOnAction(e -> showHint());

        Button resignBtn =
                sideBtn("🏳 Resign");

        resignBtn.setOnAction(e -> resign());

        Button drawBtn = sideBtn("🤝 Offer Draw");
        drawBtn.setOnAction(e -> offerDraw());

        Button rematchBtn = sideBtn("🔄 Offer Rematch");
        rematchBtn.setOnAction(e -> offerRematch());

        Button replayBtn =
                sideBtn("▶ Replay");

        replayBtn.setOnAction(e -> startReplay());

        Button menuBtn =
                sideBtn("🏠 Menu");

        menuBtn.setOnAction(
                e -> app.showLobby()
        );

        chatArea = new TextArea();

        chatArea.setEditable(false);

        chatArea.setPrefRowCount(4);

        chatInput = new TextField();

        chatInput.setPromptText(
                "Say something..."
        );

        chatInput.setOnAction(
                e -> sendChat()
        );

        panel.getChildren().addAll(
                gameTitle,
                timerLabel,
                turnLabel,
                statusLabel,
                new Separator(),
                capTitle,
                capBox,
                new Separator(),
                logScroll,
                hintBtn,
                resignBtn,
                drawBtn,
                rematchBtn,
                replayBtn,
                menuBtn,
                new Separator(),
                chatArea,
                chatInput
        );

        return panel;
    }

    private Button sideBtn(String text) {

        Button button =
                new Button(text);

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        return button;
    }

    void drawBoard() {

        boardPane.getChildren().clear();

        for (int r = 0; r < Board.SIZE; r++) {

            for (int c = 0; c < Board.SIZE; c++) {

                Rectangle cell =
                        new Rectangle(
                                c * CELL,
                                r * CELL,
                                CELL,
                                CELL
                        );

                boolean dark =
                        (r + c) % 2 == 1;

                Color base =
                        dark
                                ? DARK_CELL
                                : LIGHT_CELL;

                if (dark &&
                        r == selectedRow &&
                        c == selectedCol) {

                    base = SELECT_COL;

                } else if (
                        dark &&
                                isValidTarget(r, c)
                ) {

                    final int currentRow = r;

                    final int currentCol = c;

                    boolean isJump =
                            validMovesForSelected
                                    .stream()
                                    .anyMatch(m ->
                                            m.getToRow() == currentRow &&
                                                    m.getToCol() == currentCol &&
                                                    m.isJump()
                                    );

                    base =
                            isJump
                                    ? JUMP_COL
                                    : VALID_COL;
                }

                cell.setFill(base);

                boardPane.getChildren().add(cell);

                final int fr = r;

                final int fc = c;

                cell.setOnMouseClicked(
                        e -> handleCellClick(fr, fc)
                );
            }
        }

        for (int r = 0; r < Board.SIZE; r++) {

            for (int c = 0; c < Board.SIZE; c++) {

                Piece p =
                        board.getPiece(r, c);

                if (p != null) {

                    drawPiece(p, r, c);
                }
            }
        }
    }

    private void drawPiece(
            Piece p,
            int row,
            int col
    ) {

        double cx =
                col * CELL + CELL / 2.0;

        double cy =
                row * CELL + CELL / 2.0;

        double radius =
                CELL * 0.38;

        Circle piece =
                new Circle(
                        cx,
                        cy,
                        radius
                );

        Color mainColor =
                p.getColor() == Piece.Color.RED
                        ? RED_COLOR
                        : BLK_COLOR;

        piece.setFill(
                new RadialGradient(
                        0,
                        0,
                        0.35,
                        0.3,
                        0.65,
                        true,
                        CycleMethod.NO_CYCLE,
                        new Stop(
                                0,
                                mainColor.brighter()
                        ),
                        new Stop(
                                1,
                                mainColor.darker()
                        )
                )
        );

        boardPane.getChildren().add(piece);

        if (p.isKing()) {

            Text kingText =
                    new Text(
                            cx - 7,
                            cy + 5,
                            "♛"
                    );

            boardPane.getChildren()
                    .add(kingText);
        }

        final int fr = row;

        final int fc = col;

        piece.setOnMouseClicked(
                e -> handleCellClick(fr, fc)
        );
    }

    private void handleCellClick(
            int row,
            int col
    ) {

        if (gameOver) {
            return;
        }

        if (network != null && !myTurn) {
            setStatus("Wait for opponent");
            return;
        }

        Piece p =
                board.getPiece(row, col);

        if (selectedRow >= 0) {

            if (isValidTarget(row, col)) {

                Move chosen =
                        validMovesForSelected
                                .stream()
                                .filter(m ->
                                        m.getToRow() == row &&
                                                m.getToCol() == col
                                )
                                .findFirst()
                                .orElse(null);

                if (chosen != null) {

                    attemptMove(chosen);

                    return;
                }
            }

            selectedRow = -1;

            selectedCol = -1;

            validMovesForSelected.clear();
        }

        if (p != null &&
                p.getColor() ==
                        board.getCurrentTurn()) {

            selectedRow = row;

            selectedCol = col;

            validMovesForSelected =
                    board.getValidMovesForPiece(
                            row,
                            col
                    );
        }

        drawBoard();
    }

    private boolean isValidTarget(
            int r,
            int c
    ) {

        return validMovesForSelected
                .stream()
                .anyMatch(m ->
                        m.getToRow() == r &&
                                m.getToCol() == c
                );
    }

    private void saveCurrentMove(Move move) {
        if (move == null || gameId == -1) return;
        try {
            moveCount++;
            gameMoves.add(move);
            db.saveMove(gameId, moveCount, move);
        } catch (Exception e) {
            System.err.println("Error saving move: " + e.getMessage());
        }
    }

    private void updateCapturedCount() {
        try {
            int initialRed = 12;
            int initialBlack = 12;
            int currentRed = board.getPieceCount(Piece.Color.RED);
            int currentBlack = board.getPieceCount(Piece.Color.BLACK);

            capturedRed = initialRed - currentRed;
            capturedBlack = initialBlack - currentBlack;
        } catch (Exception e) {
            System.err.println("Error updating captured count: " + e.getMessage());
        }
    }

    private void saveGameResult(String winner) {
        if (gameId == -1) return;
        try {
            int totalMoves = moveCount;
            db.finishGame(gameId, winner, totalMoves);

            if (winner != null) {
                boolean iWon = winner.equals(myName);
                db.updateWinLoss(myName, iWon, false);
                db.updateWinLoss(opponentName, !iWon, false);
            } else if (winner == null && gameOver) {
                db.updateWinLoss(myName, false, true);
                db.updateWinLoss(opponentName, false, true);
            }
        } catch (Exception e) {
            System.err.println("Error saving game result: " + e.getMessage());
        }
    }

    private void safeGameOver(String winner, String reason) {
        if (gameOver) return;

        gameOver = true;
        if (timer != null) {
            timer.stop();
            timer = null;
        }

        saveGameResult(winner);
        showWinScreen(winner, reason);
    }

    private void attemptMove(Move move) {
        if (gameOver || move == null) return;

        try {
            board.applyMove(move);

            saveCurrentMove(move);
            updateCapturedCount();
            drawBoard();
            updateStatus();

            if (board.getState() != Board.GameState.PLAYING) {
                String winner = null;
                if (board.getState() == Board.GameState.RED_WINS) {
                    winner = opponentName;
                } else if (board.getState() == Board.GameState.BLACK_WINS) {
                    winner = opponentName;
                }
                safeGameOver(winner, "Game over!");
                return;
            }

            startTimer();

            if (network != null) {
                network.sendMove(move);
                myTurn = false;
            }
        } catch (Exception e) {
            System.err.println("Error in attemptMove: " + e.getMessage());
            setStatus("Error occurred: " + e.getMessage());
        }
    }

    private void updateStatus() {
        Platform.runLater(() -> {
            capturedRedLabel.setText("🔴 " + capturedRed);
            capturedBlackLabel.setText("⚫ " + capturedBlack);
            updateTurnDisplay();
        });
    }

    private void updateTurnDisplay() {
        Platform.runLater(() -> {
            Piece.Color turn = board.getCurrentTurn();

            boolean myTurnActual = false;
            if (network == null) {
                myTurnActual = (turn == Piece.Color.BLACK && myName.equals("Player")) ||
                        (turn == Piece.Color.RED && myName.equals("Player 2"));
            } else {
                if (isHost) {
                    myTurnActual = (turn == Piece.Color.BLACK);
                } else {
                    myTurnActual = (turn == Piece.Color.RED);
                }
            }

            if (turn == Piece.Color.BLACK) {
                turnLabel.setText("⬤ BLACK's turn");
            } else {
                turnLabel.setText("⬤ RED's turn");
            }

            this.myTurn = myTurnActual;
        });
    }

    private void setStatus(String msg) {

        Platform.runLater(() ->
                statusLabel.setText(msg)
        );
    }

    private void showHint() {
        List<Move> allMoves = new ArrayList<>();
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                Piece p = board.getPiece(i, j);
                if (p != null && p.getColor() == board.getCurrentTurn()) {
                    allMoves.addAll(board.getValidMovesForPiece(i, j));
                }
            }
        }

        if (!allMoves.isEmpty()) {
            Move bestMove = allMoves.get(0);
            selectedRow = bestMove.getFromRow();
            selectedCol = bestMove.getFromCol();
            validMovesForSelected = board.getValidMovesForPiece(selectedRow, selectedCol);
            drawBoard();
            setStatus("💡 Hint: Move piece at " +
                    (char)('A' + selectedCol) + (selectedRow + 1));

            flashHintCell(bestMove.getFromRow(), bestMove.getFromCol());
        } else {
            setStatus("No valid moves available!");
        }
    }

    private void flashHintCell(int row, int col) {
        Timeline flash = new Timeline(
                new KeyFrame(Duration.millis(0), e -> highlightPiece(row, col)),
                new KeyFrame(Duration.millis(500), e -> drawBoard())
        );
        flash.setCycleCount(3);
        flash.play();
    }

    private void highlightPiece(int row, int col) {
        selectedRow = row;
        selectedCol = col;
        drawBoard();
    }

    private void resign() {

        gameOver = true;

        if (timer != null) {
            timer.stop();
            timer = null;
        }

        if (network != null && network.isConnected()) {
            network.sendResign();
        }

        saveGameResult(opponentName);

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION,
                        "You resigned. " + opponentName + " wins!",
                        ButtonType.OK
                );

        alert.showAndWait();

        app.showLobby();
    }

    private void offerDraw() {
        if (drawOffered) {
            setStatus("Draw already offered!");
            return;
        }

        if (network != null && network.isConnected()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Offer Draw");
            confirm.setHeaderText("Offer a draw to " + opponentName + "?");
            confirm.setContentText("They will have to accept or decline.");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                drawOffered = true;
                network.sendDrawOffer();
                setStatus("Draw offer sent...");
            }
        }
    }

    private void offerRematch() {
        if (rematchOffered) {
            setStatus("Rematch already offered!");
            return;
        }

        if (network != null && network.isConnected()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Rematch");
            confirm.setHeaderText("Offer a rematch to " + opponentName + "?");
            confirm.setContentText("They will have to accept or decline.");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                rematchOffered = true;
                network.sendRematch();
                setStatus("Rematch offer sent... Waiting for response");
            }
        }
    }

    private void startReplay() {
        if (gameMoves.isEmpty()) {
            setStatus("No moves to replay!");
            return;
        }

        Board savedBoard = new Board(board);
        boolean savedGameOver = this.gameOver;
        boolean savedMyTurn = this.myTurn;
        int savedMoveCount = this.moveCount;
        List<Move> savedGameMoves = new ArrayList<>(this.gameMoves);

        setStatus("Replaying game...");
        this.gameOver = true;
        if (timer != null) {
            timer.stop();
            timer = null;
        }

        Board replayBoard = new Board();
        Thread replayThread = new Thread(() -> {
            for (int i = 0; i < gameMoves.size(); i++) {
                final int index = i;
                final Move move = gameMoves.get(index);
                Platform.runLater(() -> {
                    try {
                        replayBoard.applyMove(move);
                        Board tempBoard = this.board;
                        this.board = replayBoard;
                        drawBoard();
                        this.board = tempBoard;
                        setStatus("Replay move " + (index + 1) + " of " + gameMoves.size());
                    } catch (Exception e) {
                        System.err.println("Error in replay: " + e.getMessage());
                    }
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
            Platform.runLater(() -> {
                setStatus("Replay finished");
                board = savedBoard;
                gameOver = savedGameOver;
                myTurn = savedMyTurn;
                moveCount = savedMoveCount;
                gameMoves = savedGameMoves;
                drawBoard();
                updateStatus();
                if (!gameOver) {
                    startTimer();
                }
                setStatus("Game resumed");
            });
        });
        replayThread.setDaemon(true);
        replayThread.start();
    }

    private void sendChat() {

        String msg =
                chatInput.getText().trim();

        if (msg.isEmpty()) {

            return;
        }

        chatArea.appendText(
                myName + ": " + msg + "\n"
        );

        if (network != null &&
                network.isConnected()) {

            network.sendChat(msg);
        }

        chatInput.clear();
    }

    private void startTimer() {
        if (gameOver || board == null) return;

        if (timer != null) {
            timer.stop();
            timer = null;
        }

        secondsLeft = 60;
        updateTimerDisplay();

        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (!gameOver && secondsLeft > 0) {
                secondsLeft--;
                updateTimerDisplay();
                if (secondsLeft <= 0) {
                    if (timer != null) timer.stop();
                    if (!gameOver) {
                        timeOut();
                    }
                }
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);
        timer.play();
    }

    private void updateTimerDisplay() {
        Platform.runLater(() -> {
            if (timerLabel != null) timerLabel.setText("⏱ " + secondsLeft);
        });
    }

    private void timeOut() {
        if (gameOver) return;

        gameOver = true;
        if (timer != null) {
            timer.stop();
            timer = null;
        }

        String winner = (myTurn ? opponentName : myName);
        saveGameResult(winner);
        showWinScreen(winner, "Time's up!");
    }

    private void showWinScreen(String winner, String reason) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");

            if (winner == null) {
                alert.setHeaderText("It's a Draw!");
            } else {
                alert.setHeaderText(winner + " wins!");
            }
            alert.setContentText(reason);

            ButtonType rematch = new ButtonType("Rematch");
            ButtonType menu = new ButtonType("Main Menu");
            alert.getButtonTypes().setAll(rematch, menu);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == rematch) {
                if (network != null && network.isConnected()) {
                    network.sendRematch();
                }
                resetGame();
            } else {
                app.showLobby();
            }
        });
    }

    private void resetGame() {
        try {
            if (timer != null) {
                timer.stop();
                timer = null;
            }

            board = new Board();
            gameOver = false;
            capturedRed = 0;
            capturedBlack = 0;
            selectedRow = -1;
            selectedCol = -1;
            validMovesForSelected.clear();
            moveCount = 0;
            gameMoves.clear();
            rematchOffered = false;
            drawOffered = false;

            myTurn = (network == null) || isHost;

            drawBoard();
            updateStatus();
            startTimer();

            gameId = db.startGame(
                    isHost ? myName : opponentName,
                    isHost ? opponentName : myName
            );
        } catch (Exception e) {
            System.err.println("Error resetting game: " + e.getMessage());
            app.showLobby();
        }
    }

    @Override
    public void onConnected() {
        Platform.runLater(() -> setStatus("Connected!"));
    }

    @Override
    public void onMoveReceived(Move move) {
        if (gameOver || move == null) return;

        Platform.runLater(() -> {
            try {
                board.applyMove(move);
                saveCurrentMove(move);
                updateCapturedCount();
                drawBoard();
                myTurn = true;
                startTimer();

                if (board.getState() != Board.GameState.PLAYING) {
                    String winner = null;
                    if (board.getState() == Board.GameState.RED_WINS) {
                        winner = opponentName;
                    } else if (board.getState() == Board.GameState.BLACK_WINS) {
                        winner = opponentName;
                    }
                    safeGameOver(winner, "Game over!");
                }
            } catch (Exception e) {
                System.err.println("Error in onMoveReceived: " + e.getMessage());
                setStatus("Error processing move");
            }
        });
    }

    @Override
    public void onBoardConfirm(boolean ok) {
        if (!ok) {
            setStatus("Board mismatch!");
        }
    }

    @Override
    public void onChatReceived(String msg) {
        Platform.runLater(() -> {
            chatArea.appendText(opponentName + ": " + msg + "\n");
        });
    }

    @Override
    public void onOpponentResigned() {
        Platform.runLater(() -> {
            saveGameResult(myName);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, opponentName + " resigned! You win!", ButtonType.OK);
            alert.showAndWait();
        });
    }

    @Override
    public void onDrawOffered() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Draw Offer");
            alert.setHeaderText(opponentName + " offers a draw!");
            alert.setContentText("Do you accept?");
            ButtonType accept = new ButtonType("Accept");
            ButtonType decline = new ButtonType("Decline");
            alert.getButtonTypes().setAll(accept, decline);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == accept) {
                if (network != null) {
                    network.sendDrawResponse(true);
                }
                gameOver = true;
                saveGameResult(null);
                showWinScreen(null, "Game ended in a draw!");
            } else {
                if (network != null) {
                    network.sendDrawResponse(false);
                }
                setStatus("Draw declined");
            }
        });
    }

    @Override
    public void onDrawResponse(boolean accepted) {
        Platform.runLater(() -> {
            if (accepted) {
                gameOver = true;
                saveGameResult(null);
                showWinScreen(null, "Draw accepted!");
            } else {
                setStatus("Draw offer declined");
            }
        });
    }

    @Override
    public void onRematchOffered() {
        Platform.runLater(() -> {
            if (rematchOffered) {
                if (network != null) {
                    network.sendRematch();
                }
                rematchOffered = false;
                resetGame();
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Rematch");
            alert.setHeaderText(opponentName + " wants a rematch!");
            alert.setContentText("Do you accept?");
            ButtonType accept = new ButtonType("Yes");
            ButtonType decline = new ButtonType("No");
            alert.getButtonTypes().setAll(accept, decline);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == accept) {
                if (network != null) {
                    network.sendRematch();
                }
                resetGame();
            } else {
                setStatus("Rematch declined");
            }
        });
    }

    @Override
    public void onConnectionLost() {
        setStatus("Connection lost");
    }

    public Pane getRoot() {
        return root;
    }
}