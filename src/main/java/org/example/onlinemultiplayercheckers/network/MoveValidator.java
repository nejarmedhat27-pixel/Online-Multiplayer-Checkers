package org.example.onlinemultiplayercheckers.network;

import org.example.onlinemultiplayercheckers.model.Board;
import org.example.onlinemultiplayercheckers.model.Move;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MoveValidator {

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "move-validation-thread");
                t.setDaemon(true);
                return t;
            });

    public interface ValidationResult {
        void onResult(boolean valid, String reason, Move validatedMove);
    }

    public void validateAsync(
            Board board,
            Move move,
            ValidationResult callback
    ) {

        executor.submit(() -> {

            try {

                List<Move> validMoves =
                        board.getValidMovesForPiece(
                                move.getFromRow(),
                                move.getFromCol()
                        );

                boolean valid = false;
                Move matched = null;

                for (Move vm : validMoves) {

                    if (vm.getToRow() == move.getToRow()
                            && vm.getToCol() == move.getToCol()) {

                        valid = true;
                        matched = vm;
                        break;
                    }
                }

                String reason =
                        valid
                                ? "ok"
                                : buildReason(board, move);

                callback.onResult(valid, reason, matched);

            } catch (Exception e) {

                callback.onResult(
                        false,
                        "Validation error: " + e.getMessage(),
                        null
                );
            }
        });
    }

    private String buildReason(Board board, Move move) {

        if (board.getPiece(move.getFromRow(), move.getFromCol()) == null) {
            return "No piece at source";
        }

        if (board.getPiece(move.getFromRow(), move.getFromCol())
                .getColor() != board.getCurrentTurn()) {
            return "Not your turn";
        }

        return "Illegal move";
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}