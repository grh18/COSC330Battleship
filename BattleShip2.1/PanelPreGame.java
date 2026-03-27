import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class PanelPreGame extends GamePanel {

    private JLabel statusLabel;
    private JLayeredPane layeredPane;
    private DragLayer dragLayer;

    public interface ReadyListener {
        void onReady();
    }

    private ReadyListener readyListener;

    public PanelPreGame(BattleShipModel model) {
        super(model);
        startUI();
        update();
    }

    public void setReadyListener(ReadyListener readyListener) {
        this.readyListener = readyListener;
    }

    public void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    public void startUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Colors.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("Place Your Ships", SwingConstants.CENTER);
        title.setForeground(Colors.HEADER);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Build the board panel as before
        JPanel boardPanel = buildBoardPanel("Player 1", player1Board, null);

        // Wrap it in a JLayeredPane so we can float the drag layer on top
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(boardPanel.getPreferredSize());

        boardPanel.setBounds(0, 0,
                boardPanel.getPreferredSize().width,
                boardPanel.getPreferredSize().height);
        layeredPane.add(boardPanel, JLayeredPane.DEFAULT_LAYER);

        dragLayer = new DragLayer(this);
        dragLayer.setBounds(0, 0,
                boardPanel.getPreferredSize().width,
                boardPanel.getPreferredSize().height);
        dragLayer.setOpaque(false);
        layeredPane.add(dragLayer, JLayeredPane.DRAG_LAYER);

        JPanel centered = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centered.setBackground(Colors.BG);
        centered.add(layeredPane);
        add(centered, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 4));
        bottom.setBackground(Colors.BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        statusLabel = new JLabel("Drag ships to reposition. Press R to rotate while dragging.", SwingConstants.CENTER);
        statusLabel.setForeground(Colors.HEADER);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionButtons.setBackground(Colors.BG);

        JButton randomizeButton = new JButton("Randomize");
        styleButton(randomizeButton);
        randomizeButton.addActionListener(e -> {
            model.randomizeBoard();
            update();
            statusLabel.setText("Randomized!");
        });

        JButton readyButton = new JButton("Ready Up");
        styleButton(readyButton);
        readyButton.addActionListener(e -> {
            if (model.getPlayer1Ships().stream().allMatch(s -> s.getPositions().length == 0)) {
                statusLabel.setText("Place your ships first!");
                return;
            }
            if (readyListener != null) readyListener.onReady();
        });

        actionButtons.add(randomizeButton);
        actionButtons.add(readyButton);

        bottom.add(statusLabel);
        bottom.add(actionButtons);
        add(bottom, BorderLayout.SOUTH);

        // R key rotates the ship being dragged
        dragLayer.setFocusable(true);
        dragLayer.requestFocusInWindow();
        dragLayer.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("R"), "rotate");
        dragLayer.getActionMap().put("rotate", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (dragLayer.draggedShip != null) {
                    dragLayer.draggedShip.setOrientation(!dragLayer.draggedShip.getOrientation());
                    dragLayer.repaint();
                }
            }
        });
    }

    public void update() {
        renderGrid(true, true);
    }

    public void styleButton(JButton button) {
        button.setBackground(Colors.BUTTON);
        button.setForeground(Colors.HEADER);
        button.setBorder(BorderFactory.createLineBorder(Colors.BORDER));
    }

    // -------------------------------------------------------------------------
    // DragLayer — transparent panel floating above the JButton grid
    // -------------------------------------------------------------------------
    private class DragLayer extends JPanel {

        Ship draggedShip = null;
        int[][] savedPositions = null;
        boolean savedOrientation = false;
        
        Point dragPoint = null;

        Point originPixel = null;

        int grabCol = -1, grabRow = -1;

        DragLayer(PanelPreGame preGame) {
            MouseAdapter ma = new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    requestFocusInWindow();
                    Point p = e.getPoint();
                    int[] cell = pixelToCell(p);
                    if (cell == null) return;

                    int col = cell[0], row = cell[1];

                    // Find which ship occupies this cell
                    char[][] board = model.getPlayer1Board();
                    char id = board[row][col];
                    if (!Character.isUpperCase(id)) return;   // empty or already-hit cell

                    for (Ship ship : model.getPlayer1Ships()) {
                        if (ship.getID() == id) {
                            draggedShip = ship;
                            break;
                        }
                    }
                    if (draggedShip == null) return;

                    // Save state so we can revert on bad drop
                    int[][] pos = draggedShip.getPositions();
                    savedPositions = new int[pos.length][2];
                    for (int i = 0; i < pos.length; i++) {
                        savedPositions[i][0] = pos[i][0];
                        savedPositions[i][1] = pos[i][1];
                    }
                    savedOrientation = draggedShip.getOrientation();

                    grabCol = col;
                    grabRow = row;
                    originPixel = cellToPixel(col, row);
                    dragPoint = p;
                    repaint();
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (draggedShip == null) return;
                    dragPoint = e.getPoint();
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (draggedShip == null) return;

                    Point p = e.getPoint();
                    int[] cell = snapCell(p, draggedShip);

                    boolean placed = false;
                    if (cell != null) {
                        // Temporarily clear the ship's old cells
                        clearShipFromBoard(savedPositions);

                        // Try to place at new location
                        placed = model.placeShip(cell[0], cell[1], draggedShip);

                        if (!placed) {
                            // Restore old cells manually
                            restoreShipToBoard(savedPositions, draggedShip.getID());
                            draggedShip.setOrientation(savedOrientation);
                            restoreShipPositions();
                            statusLabel.setText("Invalid position — ship returned.");
                        } else {
                            statusLabel.setText("Ship placed!");
                        }
                    } else {
                        // Snapped outside the grid — revert
                        draggedShip.setOrientation(savedOrientation);
                        restoreShipPositions();
                        statusLabel.setText("Out of bounds — ship returned.");
                    }

                    draggedShip = null;
                    dragPoint = null;
                    preGame.update();
                    repaint();
                }
            };

            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (draggedShip == null || dragPoint == null) return;

            Graphics2D g2d = (Graphics2D) g.create();

            int len = draggedShip.getLength();
            boolean vertical = draggedShip.getOrientation();

            int ghostW = vertical ? BattleShipView.CELL_SIZE : len * BattleShipView.CELL_SIZE;
            int ghostH = vertical ? len * BattleShipView.CELL_SIZE : BattleShipView.CELL_SIZE;

            int gx = dragPoint.x - ghostW / 2;
            int gy = dragPoint.y - ghostH / 2;

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            g2d.setColor(Colors.SHIP);
            g2d.fillRoundRect(gx, gy, ghostW, ghostH, 8, 8);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.85f));
            for (int i = 0; i < len; i++) {
                boolean isEnd = (i == 0 || i == len - 1);
                BufferedImage base = isEnd ? imgEnd : imgBody;
                int rotation = 0;
                if (isEnd) {
                    rotation = vertical ? (i == 0 ? 0 : 180) : (i == 0 ? 270 : 90);
                } else {
                    rotation = vertical ? 0 : 90;
                }
                BufferedImage rotated = rotateImage(base, rotation);
                if (rotated != null) {
                    int ix = gx + (vertical ? 0 : i * BattleShipView.CELL_SIZE);
                    int iy = gy + (vertical ? i * BattleShipView.CELL_SIZE : 0);
                    g2d.drawImage(rotated, ix, iy,
                            BattleShipView.CELL_SIZE, BattleShipView.CELL_SIZE, null);
                }
            }

            int[] snapCell = snapCell(dragPoint, draggedShip);
            if (snapCell != null) {
                Point snapPx = cellToPixel(snapCell[0], snapCell[1]);
                int snapW = vertical ? BattleShipView.CELL_SIZE : len * BattleShipView.CELL_SIZE;
                int snapH = vertical ? len * BattleShipView.CELL_SIZE : BattleShipView.CELL_SIZE;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                g2d.setColor(Colors.HEADER);
                g2d.fillRect(snapPx.x, snapPx.y, snapW, snapH);
            }

            g2d.dispose();
        }

        private int[] pixelToCell(Point p) {
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    JButton btn = player1Board[row][col];
                    Point btnLoc = SwingUtilities.convertPoint(btn.getParent(), btn.getLocation(), DragLayer.this);
                    Rectangle bounds = new Rectangle(btnLoc.x, btnLoc.y, btn.getWidth(), btn.getHeight());
                    if (bounds.contains(p)) return new int[]{col, row};
                }
            }
            return null;
        }

        private Point cellToPixel(int col, int row) {
            JButton btn = player1Board[row][col];
            return SwingUtilities.convertPoint(btn.getParent(), btn.getLocation(), DragLayer.this);
        }

        private int[] snapCell(Point mouse, Ship ship) {
            boolean vertical = ship.getOrientation();
            int len = ship.getLength();

            int halfW = vertical ? BattleShipView.CELL_SIZE / 2 : (len * BattleShipView.CELL_SIZE) / 2;
            int halfH = vertical ? (len * BattleShipView.CELL_SIZE) / 2 : BattleShipView.CELL_SIZE / 2;
            Point topLeft = new Point(mouse.x - halfW, mouse.y - halfH);

            int[] cell = pixelToCell(topLeft);
            if (cell == null) {
                cell = pixelToCell(mouse);
            }
            if (cell == null) return null;

            char[][] board = model.getPlayer1Board();
            int col = cell[0], row = cell[1];
            try {
                if (vertical) {
                    if (row + len - 1 >= GRID_SIZE) return null;
                    for (int i = 0; i < len; i++) {
                        char c = board[row + i][col];
                        if (Character.isUpperCase(c) && c != ship.getID()) return null;
                    }
                } else {
                    if (col + len - 1 >= GRID_SIZE) return null;
                    for (int i = 0; i < len; i++) {
                        char c = board[row][col + i];
                        if (Character.isUpperCase(c) && c != ship.getID()) return null;
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                return null;
            }
            return cell;
        }

        private void clearShipFromBoard(int[][] positions) {
            char[][] board = model.getPlayer1Board();
            for (int[] pos : positions) {
                board[pos[1]][pos[0]] = ' ';
            }
        }

        private void restoreShipToBoard(int[][] positions, char id) {
            char[][] board = model.getPlayer1Board();
            for (int[] pos : positions) {
                board[pos[1]][pos[0]] = id;
            }
        }

        private void restoreShipPositions() {
            draggedShip.removePositions();
            for (int[] pos : savedPositions) {
                draggedShip.addPosition(new int[]{pos[0], pos[1]});
            }
        }
    }
}