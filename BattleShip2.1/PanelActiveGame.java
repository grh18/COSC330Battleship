import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class PanelActiveGame extends GamePanel {
    private JLabel turnLabel;
    private JLabel statusLabel;

    private MoveListener moveListener;
    
    
    public PanelActiveGame(BattleShipModel model) {
        super(model);
        startUI();
        update();
    }

    public void setMoveListener(MoveListener listener) {
        this.moveListener = listener;
    }

    private void startUI() {

        setLayout(new BorderLayout(0,0));
        setBackground(Colors.BG);
        setBorder(BorderFactory.createEmptyBorder(16,20,16,20));

        JLabel title = new JLabel("BATTLESHIP", SwingConstants.CENTER);
        title.setForeground(Colors.HEADER);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel boardsPanel = new JPanel(new GridLayout(1,2,1,2));
        boardsPanel.setBackground(Colors.BG);
        boardsPanel.add(buildBoardPanel("Player 1", player1Board, (c, r) -> { if (moveListener != null) moveListener.onMove(c, r); }));
        boardsPanel.add(buildBoardPanel("Player 2", player2Board, (c, r) -> { if (moveListener != null) moveListener.onMove(c, r); }));

        add(boardsPanel, BorderLayout.CENTER );

        JPanel statusPanel = new JPanel(new GridLayout(2,1, 0, 2));
        statusPanel.setBackground(Colors.BG);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        turnLabel = new JLabel("", SwingConstants.CENTER);
        turnLabel.setForeground(Colors.HEADER);

        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setForeground(Colors.HEADER);

        statusPanel.add(turnLabel);
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);

    }

    public void update() {
        boolean p1Turn = model.getPlayerTurn();
        
        renderGrid(true, true);
        renderGrid(false, true);

        setGridEnabled(player2Board, model.getPlayer2Board(), p1Turn);
        setGridEnabled(player1Board, model.getPlayer1Board(), !p1Turn);

        turnLabel.setText(p1Turn ? "Player 1's turn -- Fire on Player 2's Board" : "Player 2's turn -- Fire on Player 1's Board");
    }

    private void setGridEnabled(JButton[][] buttons, char[][] board, boolean enabled) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                buttons[row][col].setEnabled(enabled);
                buttons[row][col].setForeground(enabled ? Colors.WATER : Colors.INNACTIVE);
            }
        }
    }

    public void setStatus(String msg) {
        statusLabel.setText(msg);
    }

    public void showGameOver(String winner) {
        turnLabel.setText(winner + " wins!");
        statusLabel.setText("Game over.");
    }

    public void setWaiting() {
        setGridEnabled(player1Board, model.getPlayer1Board(), false);
        setGridEnabled(player2Board, model.getPlayer2Board(), false);
        turnLabel.setText("Waiting for opponent...");
    }

    public void updateNetworked(boolean isMyTurn) {
        renderGrid(true, true);
        renderOpponentGrid();
        setGridEnabled(player2Board, model.getPlayer2Board(), isMyTurn);
        setGridEnabled(player1Board, model.getPlayer1Board(), false);
        turnLabel.setText(isMyTurn ? "Your turn — Fire on Opponent's Board" : "Opponent is firing...");
    }

    private void renderOpponentGrid() {
        char[][] board = model.getPlayer2Board();

        // First pass: water and misses
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                JButton btn = player2Board[row][col];
                btn.setText("");
                btn.setIcon(board[row][col] == 'x' ? scaleToButton(imgMiss) : scaleToButton(imgWater));
            }
        }

        // Second pass: hits and sunk ships
        for (Ship ship : model.getPlayer2Ships()) {
            int[][] positions = ship.getPositions();
            if (positions.length == 0) continue;

            boolean sunk = ship.checkIfSunk(board);
            boolean vertical = ship.getOrientation();

            for (int i = 0; i < positions.length; i++) {
                int col = positions[i][0];
                int row = positions[i][1];
                if (!Character.isLowerCase(board[row][col])) continue;

                if (sunk) {
                    boolean isEnd = (i == 0 || i == positions.length - 1);
                    BufferedImage base = isEnd ? imgHitEnd : imgHitBody;
                    int deg = isEnd ? (vertical ? (i == 0 ? 0 : 180) : (i == 0 ? 270 : 90)) : (vertical ? 0 : 90);
                    player2Board[row][col].setIcon(scaleToButton(rotateImage(base, deg)));
                } else {
                    player2Board[row][col].setIcon(scaleToButton(imgHitWater));
                }
            }
        }
    }

}
interface MoveListener {
    void onMove(int col, int row);
}