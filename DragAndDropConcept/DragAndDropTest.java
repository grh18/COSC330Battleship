import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class DragAndDropTest {
    public static void main(String args[]) throws IOException{
        // Creates a list of pieces and adds two Piece objects, using the images, and length of the pieces
        List<Piece> pieces = new ArrayList<>();
        pieces.add(newPiece("battleship2.png", 2));
        pieces.add(newPiece("battleship3.png", 3));

        // Starts the JFrame
        JFrame jframe = new MyFrame(pieces);

    }


    // Creates a new piece using name of the image file, and length of the piece
    public static Piece newPiece(String fileName, int pieceLength) throws IOException{
        File file = new File(fileName);

        // ImageIO.read(file) creates a BufferedImage, then gets resized using resize().
        // The resized BufferedImage gets turned into an ImageIcon with width and height, based on size of the piece
        // A new piece is made with the ImageIcon, a Point to give coords, and the length of the piece
        return new Piece(new ImageIcon( resize( ImageIO.read( file ), pieceLength * MyPanel.CELL_SIZE, MyPanel.CELL_SIZE) ), new Point(), pieceLength);
    }


    // Resizes a BufferedImage to desired resolution
    public static BufferedImage resize(BufferedImage originalImage, int width, int height) throws IOException {
        // Creates a new image for the resized result. TYPE_INT_ARGB allows for colors to be 0-255 opacity, allowing for transparent backgrounds.
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Creates a Graphics object that allows editing the resized BufferedImage
        Graphics2D g2d = resizedImage.createGraphics();

        // Tells the resizing algorithm how to deal with pixels not lining up after rescaling
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Draws the original image to the resized image with the desired size
        g2d.drawImage(originalImage, 0, 0, width, height, null);

        // Releases the resources from g2a
        g2d.dispose();

        // Returns the new resized image
        return resizedImage;
    }


    // The window for the program
    static class MyFrame extends JFrame {
        // How large the window will be initially
        private static final int SIZE = 1000;

        // Creates the window, and the board
        public MyFrame(List<Piece> pieces) {
            // Closes the JFrame when exited
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Sets the size of the window
            this.setSize(SIZE, SIZE);

            // Centers the window on the screen
            this.setLocationRelativeTo(null);

            // Creates the board with the pieces, and sets the boarder to black
            MyPanel myPanel = new MyPanel(pieces);
            myPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            // Adds the board to the window, sets background to white, and makes it visible
            this.add(myPanel);
            this.setBackground(Color.WHITE);
            this.setVisible(true);
        }
    }
}