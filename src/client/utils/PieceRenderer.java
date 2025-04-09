package client.utils;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * Utility class to render a Quarto piece as a graphic node.
 * Each piece is represented using 4 binary attributes:
 * Bit 3 - Height    (0 = short, 1 = tall)
 * Bit 2 - Color     (0 = red, 1 = blue)
 * Bit 1 - Shape     (0 = square, 1 = circle)
 * Bit 0 - Fill      (0 = solid, 1 = hollow)
 */
public class PieceRenderer {

    /**
     * Creates a JavaFX Node representing the piece visually using shape, color, and style.
     * @param pieceId Piece ID (0-15), or -1 for empty/placeholder
     * @return JavaFX Node with the visual representation
     */
    public static Node createPieceGraphic(int pieceId) {
        StackPane pane = new StackPane();
        // Set uniform size for all pieces so they align well in the grid
        pane.setPrefSize(60, 60);
        pane.setMinSize(60, 60);
        pane.setMaxSize(60, 60);

        if (pieceId == -1) return pane;// Empty placeholder
        // Extract binary properties from piece ID
        boolean isTall = ((pieceId >> 3) & 1) == 1;
        boolean isBlue = ((pieceId >> 2) & 1) == 1;
        boolean isCircle = ((pieceId >> 1) & 1) == 1;
        boolean isHollow = (pieceId & 1) == 1;

        double size = isTall ? 50 : 30; // Choose piece size based on height bit
        Color color = isBlue ? Color.ROYALBLUE : Color.CRIMSON; //Determine color
        Shape shape = isCircle ? new Circle(size / 2) : new Rectangle(size, size); // Create the shape: circle or square

        // Set fill and stroke based on solid/hollow property
        if (isHollow) {
            shape.setFill(Color.TRANSPARENT);
            shape.setStroke(color);
            shape.setStrokeWidth(3);
        } else {
            shape.setFill(color);
            shape.setStroke(Color.TRANSPARENT);
        }

        // Add the shape to the pane
        pane.getChildren().add(shape);

        // Set tooltip description (helpful for debugging or accessibility)
        String desc = (isTall ? "Tall" : "Short") + " " +
                (isBlue ? "blue" : "red") + " " +
                (isHollow ? "hollow" : "solid") + " " +
                (isCircle ? "circle" : "square");

        Tooltip.install(pane, new Tooltip(desc));
        return pane;
    }
}
