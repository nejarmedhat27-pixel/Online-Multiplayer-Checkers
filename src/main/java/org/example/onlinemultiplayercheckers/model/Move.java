package org.example.onlinemultiplayercheckers.model;

import java.util.ArrayList;
import java.util.List;

public class Move {

    private int fromRow;

    private int fromCol;

    private int toRow;

    private int toCol;

    private List<int[]> capturedPositions;

    public Move(
            int fromRow,
            int fromCol,
            int toRow,
            int toCol
    ) {

        this.fromRow = fromRow;

        this.fromCol = fromCol;

        this.toRow = toRow;

        this.toCol = toCol;

        capturedPositions =
                new ArrayList<>();
    }

    public int getFromRow() {

        return fromRow;
    }

    public int getFromCol() {

        return fromCol;
    }

    public int getToRow() {

        return toRow;
    }

    public int getToCol() {

        return toCol;
    }

    public List<int[]> getCapturedPositions() {

        return capturedPositions;
    }

    public void addCapture(
            int row,
            int col
    ) {

        capturedPositions.add(

                new int[]{

                        row,

                        col
                }

        );
    }


    // ===========================
    // Check if move is jump
    // ===========================

    public boolean isJump() {

        return !capturedPositions.isEmpty();
    }


    // ===========================
    // Convert move to network text
    // ===========================

    public String serialize() {

        StringBuilder sb =
                new StringBuilder();

        sb.append(fromRow)

                .append(",")

                .append(fromCol)

                .append(",")

                .append(toRow)

                .append(",")

                .append(toCol);


        for(int[] cap
                :
                capturedPositions){

            sb.append(";")

                    .append(cap[0])

                    .append(",")

                    .append(cap[1]);

        }

        return sb.toString();

    }


    // ===========================
    // Read move from network text
    // ===========================

    public static Move deserialize(
            String data
    ){

        String[] parts =
                data.split(";");


        String[] coords=
                parts[0].split(",");


        Move move=

                new Move(

                        Integer.parseInt(
                                coords[0]
                        ),

                        Integer.parseInt(
                                coords[1]
                        ),

                        Integer.parseInt(
                                coords[2]
                        ),

                        Integer.parseInt(
                                coords[3]
                        )

                );


        for(int i=1;i<parts.length;i++){

            String[] cap=
                    parts[i]
                            .split(",");


            move.addCapture(

                    Integer.parseInt(
                            cap[0]
                    ),

                    Integer.parseInt(
                            cap[1]
                    )

            );

        }

        return move;

    }


    @Override
    public String toString() {

        return

                "("
                        + fromRow
                        + ","
                        + fromCol
                        + ") -> ("
                        + toRow
                        + ","
                        + toCol
                        + ")";
    }

}