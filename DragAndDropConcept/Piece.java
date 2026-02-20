import javax.swing.ImageIcon;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;

public class Piece {
    ImageIcon image;            // The picture that is displayed for the piece
    Point position;             // The coordinates of the piece
    Boolean orientation = true; // The orientation of the piece (true = horizontal, false = verticle)
    int pieceLength;            // How many spaces long the piece is

    // Contructor for the piece
    public Piece(ImageIcon image, Point position, int length) {
        this.image = image;
        this.position = position;
        this.pieceLength = length;
    }

    // Checks if a set of cordinates is within the piece, for selecting the piece
    public boolean contains(Point p) {
        int x = (int) position.getX();
        int y = (int) position.getY();
        int w = image.getIconWidth();
        int h = image.getIconHeight();

        // Checks if the given coords are on the piece, based on orientation
        if (orientation) {
            return p.x >= x && p.x <= x + w &&
                   p.y >= y && p.y <= y + h;
        } else {
            return p.x >= x + w - MyPanel.CELL_SIZE && p.x <= x + w && // Wierd offset for the piece to line up with hitbox after rotating
                   p.y >= y && p.y <= y + w;
        }
    }

    // Draws the piece on the panel
    public void draw(Graphics2D g2d) {
        int x = (int) position.getX();
        int y = (int) position.getY();

        // Saves the state of the g2d before transforming it
        // g2d is stored as a 3x3 matrix to determine where pixels will end up after a rotation, translation, etc
        AffineTransform saved = g2d.getTransform();

        // Rotates the piece by 90 degrees if it is verticle
        if (!orientation) {
            g2d.rotate(Math.toRadians(90), x, y);
            g2d.translate(0, -image.getIconWidth());
        }

        // Puts the image into g2d, with what cords to put it
        image.paintIcon(null, g2d, x, y);

        // Resets the g2d to the saved state
        g2d.setTransform(saved);
    }
}