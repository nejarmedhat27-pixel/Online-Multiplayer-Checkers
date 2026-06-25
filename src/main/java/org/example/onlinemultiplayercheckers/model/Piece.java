package org.example.onlinemultiplayercheckers.model;

public class Piece {

    public enum Color {

        RED,
        BLACK
    }

    private Color color;

    private boolean king;

    private int row;

    private int col;

    public Piece(
            Color color,
            int row,
            int col
    ) {

        this.color = color;

        this.row = row;

        this.col = col;

        this.king = false;
    }

    public Color getColor() {

        return color;
    }

    public boolean isKing() {

        return king;
    }

    public void promoteToKing() {

        king = true;
    }

    public int getRow() {

        return row;
    }

    public int getCol() {

        return col;
    }

    public void setPosition(
            int row,
            int col
    ) {

        this.row = row;

        this.col = col;
    }
}