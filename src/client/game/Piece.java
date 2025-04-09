package client.game;

/**
 * Represents a single piece in the Quarto game.
 * Each piece is uniquely identified by a 4-bit integer (0-15),
 * where each bit represents a property.
 */
public class Piece {
    private final int id;

    public Piece(int id) {
        if (id < 0 || id > 15)
            throw new IllegalArgumentException("Invalid piece id");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Returns the value (0 or 1) of a specific bit (attribute) in this piece.
     */
    public int getAttribute(int bitIndex) {
        return (id >> bitIndex) & 1;
    }

    /**
     * Returns true if all pieces share the same value for a specific bit.
     */
    public static boolean sharesAttribute(Piece[] pieces, int bit) {
        boolean allZero = true, allOne = true;
        for (Piece piece : pieces) {
            int bitVal = piece.getAttribute(bit);
            if (bitVal == 0) allOne = false;
            else allZero = false;
        }
        return allZero || allOne;
    }
}
