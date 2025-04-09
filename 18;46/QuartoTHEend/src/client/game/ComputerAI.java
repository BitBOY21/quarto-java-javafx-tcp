package client.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple AI for Quarto using fixed-depth Minimax with Alpha-Beta Pruning.
 * Looks ahead 3 full moves (AI Place+Choose -> Opp Place+Choose -> AI Place+Choose -> Evaluate).
 */
public class ComputerAI {

    // --- Constants ---
    private static final double WIN_SCORE = 10000.0; // Score for winning
    private static final double LOSE_SCORE = -10000.0; // Score for losing
    private static final int SEARCH_DEPTH = 3;       // Fixed lookahead depth (3 full turns)
    // Simple Heuristic weights
    private static final double THREE_IN_LINE_WEIGHT = 50.0; // High reward for threats
    private static final double TWO_IN_LINE_WEIGHT = 5.0;
    private static final double ONE_IN_LINE_WEIGHT = 0.5;
    private static final double CENTER_BONUS = 2.0; // Small bonus for center squares
    private static final double GIVING_WINNING_PIECE_PENALTY = -9000.0; // Very bad to give opponent a win

    /**
     * Represents a potential move evaluated by the AI.
     * Only score is essential for simple minimax, but storing move details helps debugging.
     */
    private static class Move {
        int row = -1, col = -1; // Placement position
        int pieceToGive = -1;  // Piece to give opponent
        double score;          // Evaluation score

        Move(double score) { this.score = score; }
        Move(int r, int c, int p, double s) { this.row = r; this.col = c; this.pieceToGive = p; this.score = s;}
        @Override public String toString() { return String.format("Move[Pos=(%d,%d), Give=%d, Score=%.1f]", row, col, pieceToGive, score); }
    }

    // --- Public API Methods ---

    /**
     * Finds the best square to place the currently held piece using a 3-move lookahead.
     * @param game The current game state. Assumes game.getCurrentPiece() != -1.
     * @return An array {row, col} representing the best placement.
     */
    public static int[] findBestPlacement(QuartoGame game) {
        // Start minimax for the placement phase of the AI's turn (maximizing player)
        Move bestMove = minimax(game, SEARCH_DEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true, true);

        if (bestMove == null || bestMove.row == -1) {
            System.err.println("AI Simple Warning: No valid placement found, choosing random.");
            return findRandomEmptySquare(game); // Fallback
        }
        return new int[]{bestMove.row, bestMove.col};
    }

    /**
     * Chooses the best piece to give to the opponent using a 3-move lookahead.
     * @param game The game state *after* the AI has placed its piece.
     * @return The ID (0-15) of the piece to give to the opponent.
     */
    public static int chooseBestPieceForOpponent(QuartoGame game) {
        // Start minimax for the choice phase of the AI's turn (still maximizing overall)
        // The recursive calls will handle the opponent minimizing.
        Move bestChoice = minimax(game, SEARCH_DEPTH, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true, false); // Note: Still depth 3, phase is choice

        if (bestChoice == null || bestChoice.pieceToGive == -1) {
            System.err.println("AI Simple Warning: No valid piece found to give, choosing random.");
            return chooseRandomAvailablePiece(game); // Fallback
        }
        // System.out.println("AI Simple Choice: Piece "+bestChoice.pieceToGive + " Score: " + bestChoice.score);
        return bestChoice.pieceToGive;
    }

    // --- Fallback Methods ---
    private static int[] findRandomEmptySquare(QuartoGame game) {
        List<int[]> emptySquares = getValidPlacements(game);
        if (emptySquares.isEmpty()) return new int[]{-1, -1};
        Collections.shuffle(emptySquares); // Add randomness
        return emptySquares.get(0);
    }
    private static int chooseRandomAvailablePiece(QuartoGame game) {
        List<Integer> available = getAvailablePieceIds(game);
        if (available.isEmpty()) return -1;
        Collections.shuffle(available); // Add randomness
        return available.get(0);
    }

    // --- Core Search Logic ---

    /**
     * Simple Minimax with Alpha-Beta Pruning.
     *
     * @param game The current game state.
     * @param depth Remaining search depth (full turns).
     * @param alpha Best score found so far for the maximizing player.
     * @param beta Best score found so far for the minimizing player.
     * @param isMaxPlayer True if the current turn is for the AI (maximizing player).
     * @param isPlacementPhase True if the current action is placing a piece. False if choosing a piece.
     * @return The best Move (score, and relevant action details) found from this state.
     */
    private static Move minimax(QuartoGame game, int depth, double alpha, double beta, boolean isMaxPlayer, boolean isPlacementPhase) {

        // --- Base Cases ---
        // 1. Check if previous move won
        if (WinChecker.checkWin(game.getIntBoard())) {
            // If isMaxPlayer's turn, it means Min player just won.
            return new Move(isMaxPlayer ? LOSE_SCORE : WIN_SCORE);
        }

        // 2. Check for Draw conditions
        boolean noPiecesLeft = getAvailablePieceIds(game).isEmpty();
        boolean noPieceToPlace = isPlacementPhase && game.getCurrentPiece() == -1;
        if ((noPiecesLeft && noPieceToPlace) || (!isPlacementPhase && noPiecesLeft)) {
            return new Move(0.0); // Draw
        }

        // 3. Depth limit reached
        if (depth == 0) {
            return new Move(evaluateBoard(game));
        }

        // --- Recursive Step ---
        Move bestOverallMove = null; // Stores the best move details (pos, piece, score)

        if (isPlacementPhase) {
            // --- Placing the piece ---
            int pieceIdToPlace = game.getCurrentPiece();
            if (pieceIdToPlace == -1) { // Should be caught by draw check, but safety first
                return new Move(evaluateBoard(game));
            }

            double bestScore = isMaxPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            List<int[]> possiblePlacements = getValidPlacements(game);
            if(possiblePlacements.isEmpty()) return new Move(0.0); // Board full draw

            // Check immediate wins first (simple optimization)
            for (int[] placement : possiblePlacements) {
                QuartoGame quickSim = game.copy();
                try {
                    if (quickSim.placeCurrentPiece(placement[0], placement[1])) {
                        double score = (isMaxPlayer ? WIN_SCORE : LOSE_SCORE);
                        // System.out.println("Depth " + depth + ": Immediate win found at ("+placement[0]+","+placement[1]+")");
                        return new Move(placement[0], placement[1], -1, score); // Found best move
                    }
                } catch (Exception e) {}
            }

            // Explore non-winning placements
            for (int[] placement : possiblePlacements) {
                int r = placement[0];
                int c = placement[1];
                QuartoGame simGame = game.copy();
                try {
                    simGame.placeCurrentPiece(r, c); // Place (we know it's not a win from above)

                    // Recurse to the piece choice phase (same player, same depth level)
                    Move resultFromChoice = minimax(simGame, depth, alpha, beta, isMaxPlayer, false);

                    if (resultFromChoice == null) continue; // Should not happen normally

                    double currentScore = resultFromChoice.score;

                    // Update best score and best move details
                    if (isMaxPlayer) {
                        if (currentScore > bestScore) {
                            bestScore = currentScore;
                            // Store the placement (r, c) and the chosen piece from the recursive call
                            bestOverallMove = new Move(r, c, resultFromChoice.pieceToGive, bestScore);
                        }
                        alpha = Math.max(alpha, bestScore);
                    } else { // Minimizing player
                        if (currentScore < bestScore) {
                            bestScore = currentScore;
                            bestOverallMove = new Move(r, c, resultFromChoice.pieceToGive, bestScore);
                        }
                        beta = Math.min(beta, bestScore);
                    }

                    // Alpha-Beta Pruning
                    if (beta <= alpha) {
                        break; // Prune remaining placements
                    }
                } catch (Exception e) { System.err.println("AI Simple Error P: " + e); }
            } // End placement loop

            // If no move improved the initial score (e.g., all branches pruned badly), return score.
            if (bestOverallMove == null){
                // This could happen if all placements lead to immediate loss in the next phase and get pruned.
                // Return the score bound that caused pruning.
                return new Move(isMaxPlayer ? alpha : beta);
            }
            return bestOverallMove;

        } else {
            // --- Choosing the piece for the opponent ---
            double bestScore = isMaxPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
            List<Integer> availablePieces = getAvailablePieceIds(game);
            if(availablePieces.isEmpty()) return new Move(0.0); // Draw

            int bestPiece = -1;

            // Simple move ordering: Check losing pieces last
            List<Integer> safePieces = new ArrayList<>();
            List<Integer> losingPieces = new ArrayList<>();
            for(int pieceId : availablePieces){
                if(canOpponentWinWithPiece(game, pieceId)){
                    losingPieces.add(pieceId);
                } else {
                    safePieces.add(pieceId);
                }
            }
            // Combine, safe first
            List<Integer> orderedPieces = new ArrayList<>(safePieces);
            orderedPieces.addAll(losingPieces);


            for (int pieceId : orderedPieces) {
                double currentScore;
                // If this piece lets opponent win immediately, assign penalty directly
                boolean isLosingPiece = losingPieces.contains(pieceId); // Check if pre-calculated as losing
                if (isLosingPiece) {
                    // Assign penalty, don't recurse further down this obviously bad path unless forced
                    currentScore = isMaxPlayer ? GIVING_WINNING_PIECE_PENALTY : -GIVING_WINNING_PIECE_PENALTY;
                    // Only consider this if no safe options exist or if it's better than other losing options
                    if (isMaxPlayer && currentScore <= alpha && !safePieces.isEmpty()) continue; // Skip if worse than alpha and safe options exist
                    if (!isMaxPlayer && currentScore >= beta && !safePieces.isEmpty()) continue; // Skip if worse than beta and safe options exist
                } else {
                    // Simulate giving the piece and recurse for opponent's placement turn
                    QuartoGame simGame = game.copy();
                    try {
                        simGame.setCurrentPiece(pieceId);
                        // Depth decreases, player switches, phase becomes placement
                        Move resultFromPlacement = minimax(simGame, depth - 1, alpha, beta, !isMaxPlayer, true);

                        if (resultFromPlacement == null) continue;
                        currentScore = resultFromPlacement.score;

                    } catch (Exception e) { System.err.println("AI Simple Error C: " + e); continue; }
                }


                // Update best score and best piece to give
                if (isMaxPlayer) { // AI is choosing piece, wants to maximize score after opponent moves
                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        bestPiece = pieceId;
                    }
                    alpha = Math.max(alpha, bestScore);
                } else { // Opponent is choosing piece, wants to minimize score after AI moves
                    if (currentScore < bestScore) {
                        bestScore = currentScore;
                        bestPiece = pieceId;
                    }
                    beta = Math.min(beta, bestScore);
                }

                // Alpha-Beta Pruning
                if (beta <= alpha) {
                    break; // Prune remaining piece choices
                }
            } // End piece choice loop

            // If no piece choice was made (e.g., all pruned), return score bound.
            if(bestPiece == -1){
                // If no pieces were safe and all losing pieces were pruned away.
                // Need to select *something*. Select the first available piece as fallback.
                if (!availablePieces.isEmpty()) bestPiece = availablePieces.get(0);
                else return new Move(0.0); // Should be caught by earlier check

                // Return the score bound that caused pruning, associated with the fallback piece
                bestScore = isMaxPlayer ? alpha : beta;
            }
            // Return the best score found and the piece associated with it
            bestOverallMove = new Move(bestScore);
            bestOverallMove.pieceToGive = bestPiece; // Only pieceToGive matters here
            return bestOverallMove;
        }
    }


    // --- Heuristic Evaluation ---

    /**
     * Simple heuristic evaluation based on lines with matching attributes.
     * @param game The game state to evaluate.
     * @return A heuristic score (positive favors AI, negative favors opponent).
     */
    private static double evaluateBoard(QuartoGame game) {
        // Win check is handled in minimax base case. Evaluate potential here.
        double score = 0;
        Board board = game.getBoard();

        // Evaluate all 10 lines (rows, columns, diagonals)
        score += evaluateLinePotential(getLine(board, 0, -1));
        score += evaluateLinePotential(getLine(board, 1, -1));
        score += evaluateLinePotential(getLine(board, 2, -1));
        score += evaluateLinePotential(getLine(board, 3, -1));
        score += evaluateLinePotential(getLine(board, -1, 0));
        score += evaluateLinePotential(getLine(board, -1, 1));
        score += evaluateLinePotential(getLine(board, -1, 2));
        score += evaluateLinePotential(getLine(board, -1, 3));
        score += evaluateLinePotential(getLine(board, -1, -1, true)); // Main Diag
        score += evaluateLinePotential(getLine(board, -1, -1, false)); // Anti Diag

        // Add small bonus for center control
        if (board.getPiece(1, 1) != null) score += CENTER_BONUS;
        if (board.getPiece(1, 2) != null) score += CENTER_BONUS;
        if (board.getPiece(2, 1) != null) score += CENTER_BONUS;
        if (board.getPiece(2, 2) != null) score += CENTER_BONUS;

        // This score is from the perspective of the player whose turn it WOULD be.
        // Minimax handles flipping signs appropriately.
        return score;
    }

    /** Evaluates potential for a single line based on shared attributes */
    private static double evaluateLinePotential(Piece[] line) {
        if (line == null) return 0;
        double lineScore = 0;
        // Check potential for each of the 4 attributes
        for (int attributeIndex = 0; attributeIndex < 4; attributeIndex++) {
            int count = 0;
            Integer commonValue = null; // 0 or 1
            boolean possible = true;

            for (Piece p : line) {
                if (p != null) {
                    int pieceAttributeValue = p.getAttribute(attributeIndex);
                    if (commonValue == null) {
                        commonValue = pieceAttributeValue;
                        count++;
                    } else if (commonValue == pieceAttributeValue) {
                        count++;
                    } else {
                        possible = false; // Conflicting attributes
                        break;
                    }
                }
            }

            // If a common attribute exists among pieces in the line
            if (possible && count > 0) {
                switch (count) {
                    case 1: lineScore += ONE_IN_LINE_WEIGHT; break;
                    case 2: lineScore += TWO_IN_LINE_WEIGHT; break;
                    case 3: lineScore += THREE_IN_LINE_WEIGHT; break;
                    // Case 4 is a win, handled by WinChecker in minimax base case.
                }
            }
        }
        return lineScore;
    }

    // --- Helper Methods ---

    /** Checks if giving pieceId allows the opponent to win on their next placement. */
    private static boolean canOpponentWinWithPiece(QuartoGame game, int pieceId) {
        if (pieceId < 0 || pieceId > 15) return false;
        int[][] currentIntBoard = game.getIntBoard();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (currentIntBoard[r][c] == -1) { // Check only empty squares
                    currentIntBoard[r][c] = pieceId; // Simulate placing
                    if (WinChecker.checkWin(currentIntBoard)) {
                        currentIntBoard[r][c] = -1; // Backtrack
                        return true; // Found a winning placement
                    }
                    currentIntBoard[r][c] = -1; // Backtrack
                }
            }
        }
        return false; // No winning placement found
    }

    /** Gets a line (row, col, or diag) from the board as Piece[] */
    private static Piece[] getLine(Board board, int row, int col, boolean... diagonal) {
        Piece[] line = new Piece[4];
        try {
            if (diagonal.length > 0) { // Diagonal
                boolean mainDiag = diagonal[0];
                for (int i = 0; i < 4; i++) line[i] = board.getPiece(i, mainDiag ? i : 3 - i);
            } else if (row != -1) { // Row
                for (int c = 0; c < 4; c++) line[c] = board.getPiece(row, c);
            } else if (col != -1) { // Column
                for (int r = 0; r < 4; r++) line[r] = board.getPiece(r, col);
            } else return null;
        } catch (ArrayIndexOutOfBoundsException e) { return null; } // Safety
        return line;
    }

    /** Gets list of valid placements {row, col} */
    private static List<int[]> getValidPlacements(QuartoGame game) {
        List<int[]> p = new ArrayList<>(); Board b = game.getBoard();
        for (int r=0; r<4; r++) for (int c=0; c<4; c++) if (b.getPiece(r,c)==null) p.add(new int[]{r,c});
        return p;
    }

    /** Gets list of available piece IDs */
    private static List<Integer> getAvailablePieceIds(QuartoGame game) {
        List<Integer> ids = new ArrayList<>(); boolean[] available = game.getAvailablePieces();
        for (int i = 0; i < 16; i++) if (available[i]) ids.add(i);
        return ids;
    }
}