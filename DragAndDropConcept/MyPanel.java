import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class MyPanel extends JPanel{
    List<Piece> pieces = new ArrayList<>(); // A list of all the pieces on the board
    Piece selectedPiece = null;             // No piece is currently being changed
    Point prevPoint;                        // Tracks the innitial location of the mouse for dragging purposes
    static final int CELL_SIZE = 50;        // How many pixels wide each grid cell is

    // Snaps (rounds) value to the nearest cell
    private int snap(int value) {
        return (int) (Math.round((double) value / CELL_SIZE) * CELL_SIZE);
    }

    // Acts as the board for the pieces to align with
    public MyPanel(List<Piece> pieceList) {
        pieces = pieceList;
        // Adds listeners to detect a mouse click/release, and for mouse movement
        this.addMouseListener(new ClickListener());
        this.addMouseMotionListener(new DragListener());

        // Lets the panel recieve key inputs, and gives immediate focus to the panel
        this.setFocusable(true);
        this.requestFocusInWindow();

        // Detects when "R" is pressed (while the window is focused) and it performs the action "rotate"
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("R"), "rotate");
        // Defines what the "rotate" action does. If a piece is selected then it changes the orientation.
        getActionMap().put("rotate", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (selectedPiece != null) {
                    selectedPiece.orientation = !selectedPiece.orientation;
                    repaint();
                }
            }
        });
    }

    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        Graphics2D g2d = (Graphics2D) g;
        for (Piece piece : pieces) {
            piece.draw(g2d);
        }
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.BLACK);
        int width = getWidth();
        int height = getHeight();
        for (int x = 0; x < width; x += CELL_SIZE) g.drawLine(x, 0, x, height);
        for (int y = 0; y < height; y += CELL_SIZE) g.drawLine(0, y, width, y);
    }

    private class ClickListener extends MouseAdapter{
        public void mousePressed(MouseEvent event) {
            selectedPiece = null;
            for (int i = pieces.size() - 1; i >= 0; i--) {
                if (pieces.get(i).contains(event.getPoint())) {
                    selectedPiece = pieces.get(i);
                    break;
                }
            }
            prevPoint = event.getPoint();
        }

        public void mouseReleased(MouseEvent event) {
            if (selectedPiece != null) {
                selectedPiece.position.setLocation(
                    snap((int) selectedPiece.position.getX()),
                    snap((int) selectedPiece.position.getY())
                );
                repaint();
            }
        }
    }

    private class DragListener extends MouseMotionAdapter{
        public void mouseDragged(MouseEvent event) {
            if (selectedPiece != null) {
                Point currPoint = event.getPoint();
                int dx = (int) (currPoint.getX() - prevPoint.getX());
                int dy = (int) (currPoint.getY() - prevPoint.getY());
                selectedPiece.position.translate(dx,dy);
                prevPoint = currPoint;
                repaint();
            }
        }
    }
}
