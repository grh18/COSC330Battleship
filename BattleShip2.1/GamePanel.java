import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;

public class GamePanel extends JPanel {
    protected BattleShipModel model;
    protected final int GRID_SIZE = BattleShipModel.GRID_SIZE;

    // Both grids live here — children use them via protected access
    protected JButton[][] player1Board;
    protected JButton[][] player2Board;

    protected BufferedImage imgEnd;
    protected BufferedImage imgBody;
    protected BufferedImage imgHitEnd;
    protected BufferedImage imgHitBody;
    protected BufferedImage imgWater;
    protected BufferedImage imgMiss;
    protected BufferedImage imgHitWater;

    public GamePanel(BattleShipModel model) {
        this.model = model;
        player1Board = new JButton[BattleShipModel.GRID_SIZE][BattleShipModel.GRID_SIZE];
        player2Board = new JButton[BattleShipModel.GRID_SIZE][BattleShipModel.GRID_SIZE];
        loadImages();
    }

    protected JPanel buildBoardPanel(String playerName, JButton[][] buttons, MoveListener moveListener) {
        JPanel wrapper = new JPanel(new BorderLayout(0, 6));
        wrapper.setBackground(Colors.BG);

        JLabel label = new JLabel(playerName + "'s Board", SwingConstants.CENTER);
        label.setForeground(Colors.HEADER);
        label.setBackground(Colors.BUTTON);
        label.setBorder(BorderFactory.createLineBorder(Colors.BORDER, 2));
        wrapper.add(label, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 2, 2));
        grid.setBackground(Colors.BG);
        grid.setBorder(BorderFactory.createLineBorder(Colors.BORDER, 2));

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(BattleShipView.CELL_SIZE, BattleShipView.CELL_SIZE));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setOpaque(true);
                final int c = col, r = row;
                btn.addActionListener(e -> {
                    if (moveListener != null) moveListener.onMove(c, r);
                });
                btn.setEnabled(true);
                buttons[row][col] = btn;
                grid.add(btn);
            }
        }

        JPanel rowLabels = new JPanel(new GridLayout(GRID_SIZE, 1, 2, 2));
        rowLabels.setBackground(Colors.BG);
        rowLabels.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        for (int i = 0; i < GRID_SIZE; i++) {
            JLabel cell = new JLabel(String.valueOf((char)('A' + i)), SwingConstants.CENTER);
            cell.setForeground(Colors.HEADER);
            cell.setPreferredSize(new Dimension(BattleShipView.CELL_SIZE, BattleShipView.CELL_SIZE));
            rowLabels.add(cell);
        }

        JPanel colLabels = new JPanel(new GridLayout(1, GRID_SIZE, 2, 2));
        colLabels.setBackground(Colors.BG);
        colLabels.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        for (int i = 0; i < GRID_SIZE; i++) {
            JLabel cell = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            cell.setForeground(Colors.HEADER);
            cell.setPreferredSize(new Dimension(BattleShipView.CELL_SIZE, BattleShipView.CELL_SIZE));
            colLabels.add(cell);
        }

        JPanel gridWithRows = new JPanel(new BorderLayout());
        gridWithRows.setBackground(Colors.BG);
        gridWithRows.add(grid, BorderLayout.CENTER);
        gridWithRows.add(rowLabels, BorderLayout.EAST);

        JPanel gridWithKeys = new JPanel(new BorderLayout());
        gridWithKeys.setBackground(Colors.BG);
        gridWithKeys.setBorder(BorderFactory.createLineBorder(Colors.BORDER, 2));
        gridWithKeys.add(gridWithRows, BorderLayout.CENTER);
        gridWithKeys.add(colLabels, BorderLayout.SOUTH);

        wrapper.add(gridWithKeys, BorderLayout.CENTER);
        return wrapper;
    }


    public void renderGrid(boolean player1, boolean showShips) {
        char[][] board = (player1 ? model.getPlayer1Board() : model.getPlayer2Board());
        JButton[][] buttonBoard = (player1 ? player1Board : player2Board);
        ArrayList<Ship> ships = (player1 ? model.getPlayer1Ships() : model.getPlayer2Ships());

        // First pass: set water and miss for all cells
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                char cell = board[row][col];
                JButton btn = buttonBoard[row][col];
                btn.setText("");
 
                if (cell == 'x') {
                    btn.setIcon(scaleToButton(imgMiss));
                } else if (!Character.isLetter(cell) || cell == ' ') {
                    btn.setIcon(scaleToButton(imgWater));
                } else {
                    // Ship or hit ship — handled in second pass below
                    btn.setIcon(null);
                    btn.setBackground(Colors.BG);
                }
            }
        }
 
        if (!showShips) return;
 
        // Second pass: render ship images using Ship position/orientation data
        for (Ship ship : ships) {
            int[][] positions = ship.getPositions();
            if (positions.length == 0) continue;
 
            boolean vertical = ship.getOrientation();
 
            for (int i = 0; i < positions.length; i++) {
                int col = positions[i][0]; // positions stored as {col, row}
                int row = positions[i][1];
 
                boolean isEnd = (i == 0 || i == positions.length - 1);
                boolean cellHit = Character.isLowerCase(board[row][col]);
 
                BufferedImage baseImg;
                if (isEnd) {
                    baseImg = cellHit ? imgHitEnd : imgEnd;
                } else {
                    baseImg = cellHit ? imgHitBody : imgBody;
                }
 
                // Determine rotation:
                // Vertical ship: index 0 = top end (0°), last = bottom end (180°)
                // Horizontal ship: index 0 = left end (270°), last = right end (90°)
                int rotation = 0;
                if (isEnd) {
                    if (vertical) {
                        rotation = (i == 0) ? 0 : 180;
                    } else {
                        rotation = (i == 0) ? 270 : 90;
                    }
                } else {
                    rotation = vertical ? 0 : 90;
                }
 
                BufferedImage rotated = rotateImage(baseImg, rotation);
                buttonBoard[row][col].setIcon(scaleToButton(rotated));
            }
        }
    }

    protected void loadImages() {
        imgEnd      = loadImage("images/ship_end.png");
        imgBody     = loadImage("images/ship_body.png");
        imgHitEnd   = loadImage("images/hit_end.png");
        imgHitBody  = loadImage("images/hit_body.png");
        imgWater    = loadImage("images/water.png");
        imgMiss     = loadImage("images/miss.png");
        imgHitWater = loadImage("images/enemy_hit.png");
    }

    protected BufferedImage loadImage(String file) {
        try {
            return ImageIO.read(new File(file));
        } catch (IOException e) {
            System.out.println("Could not load image: " + file);
            return null;
        }
    }

    protected BufferedImage rotateImage(BufferedImage image, int degree) {
        if (image == null) return null;
        double radians = Math.toRadians(degree);
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage rotated = new BufferedImage(w, h, image.getType());
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = AffineTransform.getRotateInstance(radians, w / 2.0, h / 2.0);
        g2d.setTransform(at);
        g2d.drawImage(image,0,0,null);
        g2d.dispose();
        return rotated;
    }

    protected ImageIcon scaleToButton(BufferedImage image) {
        if (image == null) return null;
        Image scaled = image.getScaledInstance((int)(BattleShipView.CELL_SIZE * 1.05), (int)(BattleShipView.CELL_SIZE * 1.05), Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
