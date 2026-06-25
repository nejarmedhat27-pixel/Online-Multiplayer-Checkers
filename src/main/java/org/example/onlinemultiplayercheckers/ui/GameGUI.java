// ===============================
// GameGUI.java
// ===============================

package org.example.onlinemultiplayercheckers.ui;

import org.example.onlinemultiplayercheckers.model.Board;
import org.example.onlinemultiplayercheckers.model.Piece;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class GameGUI extends Application {

    private Board board = new Board();

    private GridPane grid = new GridPane();

    private final int TILE_SIZE = 80;

    @Override
    public void start(Stage stage) {

        drawBoard();

        Scene scene = new Scene(grid);

        stage.setTitle("Checkers Game");

        stage.setScene(scene);

        stage.show();
    }

    private void drawBoard() {

        grid.getChildren().clear();

        for (int row = 0; row < 8; row++) {

            for (int col = 0; col < 8; col++) {

                Rectangle tile = new Rectangle(
                        TILE_SIZE,
                        TILE_SIZE
                );

                if ((row + col) % 2 == 0) {

                    tile.setFill(Color.BEIGE);

                } else {

                    tile.setFill(Color.BROWN);
                }

                grid.add(tile, col, row);

                Piece piece = board.getPiece(row, col);

                if (piece != null) {

                    Circle circle =
                            new Circle(TILE_SIZE / 2.5);

                    if (piece.getColor() == Piece.Color.BLACK) {

                        circle.setFill(Color.BLACK);

                    } else {

                        circle.setFill(Color.RED);
                    }

                    grid.add(circle, col, row);
                }
            }
        }
    }
}