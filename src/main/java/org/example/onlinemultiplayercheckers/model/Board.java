package org.example.onlinemultiplayercheckers.model;

import java.util.ArrayList;
import java.util.List;

public class Board {

    public static final int SIZE = 8;

    private Piece[][] grid;

    private Piece.Color currentTurn;

    private List<Move> moveHistory;

    public enum GameState {

        PLAYING,
        RED_WINS,
        BLACK_WINS
    }

    private GameState state;

    public Board() {

        grid = new Piece[SIZE][SIZE];

        moveHistory = new ArrayList<>();

        currentTurn = Piece.Color.BLACK;

        state = GameState.PLAYING;

        initializeBoard();
    }

    public Board(Board other) {
        this.grid = new Piece[SIZE][SIZE];
        this.moveHistory = new ArrayList<>(other.moveHistory);
        this.currentTurn = other.currentTurn;
        this.state = other.state;

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece p = other.grid[row][col];
                if (p != null) {
                    Piece copy = new Piece(p.getColor(), row, col);
                    if (p.isKing()) copy.promoteToKing();
                    this.grid[row][col] = copy;
                }
            }
        }
    }

    private void initializeBoard() {

        for (int row = 0; row < 3; row++) {

            for (int col = 0; col < SIZE; col++) {

                if ((row + col) % 2 == 1) {

                    grid[row][col] =
                            new Piece(
                                    Piece.Color.BLACK,
                                    row,
                                    col
                            );
                }
            }
        }

        for (int row = 5; row < SIZE; row++) {

            for (int col = 0; col < SIZE; col++) {

                if ((row + col) % 2 == 1) {

                    grid[row][col] =
                            new Piece(
                                    Piece.Color.RED,
                                    row,
                                    col
                            );
                }
            }
        }
    }

    public Piece getPiece(int row, int col) {

        if (!inBounds(row,col)) {

            return null;
        }

        return grid[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        if (inBounds(row, col)) {
            grid[row][col] = piece;
            if (piece != null) {
                piece.setPosition(row, col);
            }
        }
    }

    public int getPieceCount(Piece.Color color) {
        int count = 0;
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                Piece p = grid[row][col];
                if (p != null && p.getColor() == color) {
                    count++;
                }
            }
        }
        return count;
    }
    public boolean canDraw() {
        int movesWithoutCapture = 0;
        // Check last 40 moves for no captures
        int startIndex = Math.max(0, moveHistory.size() - 40);
        for (int i = startIndex; i < moveHistory.size(); i++) {
            Move m = moveHistory.get(i);
            if (!m.isJump()) {
                movesWithoutCapture++;
            } else {
                movesWithoutCapture = 0;
            }
        }
        return movesWithoutCapture >= 40;
    }

    public boolean inBounds(int row,int col) {

        return row >=0 &&
                row<SIZE &&
                col>=0 &&
                col<SIZE;
    }

    public Piece.Color getCurrentTurn() {

        return currentTurn;
    }

    public GameState getState() {

        return state;
    }

    public List<Move> getMoveHistory() {

        return moveHistory;
    }


    public boolean hasAnyMandatoryJump() {

        for(int row=0;row<SIZE;row++) {

            for(int col=0;col<SIZE;col++) {

                Piece piece=grid[row][col];

                if(piece!=null &&
                        piece.getColor()==currentTurn) {

                    List<Move> moves=
                            getMovesIgnoringMandatory(
                                    row,
                                    col
                            );

                    for(Move move:moves) {

                        if(move.isJump()) {

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private List<Move> getMovesIgnoringMandatory(
            int row,
            int col
    ){

        List<Move> moves =
                new ArrayList<>();

        Piece piece=
                grid[row][col];

        if(piece==null) {

            return moves;
        }

        int[] directions;

        if(piece.isKing()) {

            directions =
                    new int[]{-1,1};

        }

        else{

            directions=
                    piece.getColor()
                            ==
                            Piece.Color.BLACK
                            ?
                            new int[]{1}
                            :
                            new int[]{-1};
        }

        for(int dr:directions){

            for(int dc:new int[]{-1,1}){

                int newRow =
                        row+dr;

                int newCol =
                        col+dc;

                if(inBounds(newRow,newCol)
                        &&
                        grid[newRow][newCol]
                                ==null){

                    moves.add(

                            new Move(
                                    row,
                                    col,
                                    newRow,
                                    newCol
                            )
                    );
                }

                int jumpRow=
                        row+dr*2;

                int jumpCol=
                        col+dc*2;

                int middleRow=
                        row+dr;

                int middleCol=
                        col+dc;

                if(inBounds(
                        jumpRow,
                        jumpCol
                )){

                    Piece middle=
                            grid[middleRow]
                                    [middleCol];

                    if(middle!=null
                            &&
                            middle.getColor()
                                    !=
                                    piece.getColor()
                            &&
                            grid[jumpRow]
                                    [jumpCol]
                                    ==null){

                        Move jump=

                                new Move(
                                        row,
                                        col,
                                        jumpRow,
                                        jumpCol
                                );

                        jump.addCapture(
                                middleRow,
                                middleCol
                        );

                        moves.add(jump);
                    }
                }

            }

        }

        return moves;

    }

    public List<Move> getValidMovesForPiece(
            int row,
            int col
    ) {

        List<Move> allMoves=
                getMovesIgnoringMandatory(
                        row,
                        col
                );

        if(!hasAnyMandatoryJump()){

            return allMoves;
        }

        List<Move> jumps=
                new ArrayList<>();

        for(Move m:allMoves){

            if(m.isJump()){

                jumps.add(m);

            }

        }

        return jumps;

    }

    public boolean applyMove(Move move) {

        Piece piece =
                grid[move.getFromRow()]
                        [move.getFromCol()];

        if(piece==null){

            return false;
        }


        boolean wasJump=
                move.isJump();


        for(int[] cap:
                move.getCapturedPositions()){

            grid[
                    cap[0]
                    ][
                    cap[1]
                    ]=null;

        }


        grid[
                move.getFromRow()
                ][
                move.getFromCol()
                ]=null;


        grid[
                move.getToRow()
                ][
                move.getToCol()
                ]=piece;


        piece.setPosition(

                move.getToRow(),

                move.getToCol()

        );


        if(piece.getColor()
                ==
                Piece.Color.BLACK
                &&
                move.getToRow()
                        ==
                        SIZE-1){

            piece.promoteToKing();
        }


        if(piece.getColor()
                ==
                Piece.Color.RED
                &&
                move.getToRow()
                        ==
                        0){

            piece.promoteToKing();
        }


        moveHistory.add(move);

        if(wasJump){

            List<Move> nextMoves=

                    getMovesIgnoringMandatory(
                            move.getToRow(),
                            move.getToCol()
                    );


            for(Move m:nextMoves){

                if(m.isJump()){

                    return false;
                }

            }

        }


        currentTurn=

                currentTurn
                        ==
                        Piece.Color.BLACK

                        ?

                        Piece.Color.RED

                        :

                        Piece.Color.BLACK;


        updateGameState();

        return true;
    }
    private void updateGameState() {

        int red=0;

        int black=0;

        for(int row=0;row<SIZE;row++){

            for(int col=0;col<SIZE;col++){

                Piece p=
                        grid[row][col];

                if(p!=null){

                    if(p.getColor()
                            ==
                            Piece.Color.RED){

                        red++;

                    }

                    else{

                        black++;
                    }

                }

            }

        }


        if(red==0){

            state=
                    GameState.BLACK_WINS;

        }


        if(black==0){

            state=
                    GameState.RED_WINS;

        }

    }

}