package client.game;

/**
 * Utility class for checking winning conditions in Quarto.
 */
public class WinChecker {

    public static boolean checkWin(int[][] board) {
        // Check rows
        for (int i = 0; i < 4; i++) {
            int[] row = board[i];
            if (isFull(row) && hasCommonAttribute(row))
                return true;
        }

        // Check columns
        for (int col = 0; col < 4; col++) {
            int[] column = new int[4];
            for (int row = 0; row < 4; row++)
                column[row] = board[row][col];
            if (isFull(column) && hasCommonAttribute(column))
                return true;
        }

        // Check main diagonal
        int[] diag1 = new int[4];
        for (int i = 0; i < 4; i++)
            diag1[i] = board[i][i];
        if (isFull(diag1) && hasCommonAttribute(diag1))
            return true;

        // Check anti-diagonal
        int[] diag2 = new int[4];
        for (int i = 0; i < 4; i++)
            diag2[i] = board[i][3 - i];
        return isFull(diag2) && hasCommonAttribute(diag2);
    }

    private static boolean isFull(int[] line) {
        for (int val : line) {
            if (val == -1) return false;
        }
        return true;
    }

    private static boolean hasCommonAttribute(int[] pieces) {
        for (int bit = 0; bit < 4; bit++) {
            boolean allZero = true, allOne = true;
            for (int piece : pieces) {
                if (((piece >> bit) & 1) == 0) allOne = false;
                else allZero = false;
            }
            if (allZero || allOne)
                return true;
        }
        return false;
    }
}
