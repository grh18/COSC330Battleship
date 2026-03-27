import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PanelPostGame extends GamePanel {
    private int GRID_SIZE = BattleShipModel.GRID_SIZE;

    private JLabel scoreLabel;
    private JLabel winnerLabel;

    public interface PlayAgainListener {
        void onPlayAgain();
    }

    private PlayAgainListener playAgainListener;

    public PanelPostGame(BattleShipModel model) {
        super(model); 
        player1Board = new JButton[GRID_SIZE][GRID_SIZE];
        player2Board = new JButton[GRID_SIZE][GRID_SIZE];
        startUI();
    }

    public void setPlayAgainListener(PlayAgainListener listener) {
        this.playAgainListener = listener;
    }

    public void update(String winner, int p1s, int p2s) {
        winnerLabel.setText(winner + " Wins!");
        scoreLabel.setText("Score -- Player 1: " + p1s + " | Player 2: " + p2s);
        renderBoards();
    }

    private void startUI() {
        setLayout(new BorderLayout(0,0));
        setBackground(Colors.BG);
        setBorder(BorderFactory.createEmptyBorder(16,20,16,20));

        JLabel title = new JLabel("Game Over", SwingConstants.CENTER);
        title.setForeground(Colors.HEADER);
        title.setBorder(BorderFactory.createEmptyBorder(0,0,4,0));
        add(title, BorderLayout.NORTH);

        JPanel headerInfo = new JPanel(new GridLayout(2,1,0,2));
        headerInfo.setForeground(Colors.HEADER);
        headerInfo.setBackground(Colors.BUTTON);
        headerInfo.setBorder(BorderFactory.createLineBorder(Colors.BORDER, 2));
        headerInfo.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));

        winnerLabel = new JLabel("", SwingConstants.CENTER);
        winnerLabel.setForeground(Colors.HEADER);

        scoreLabel = new JLabel("", SwingConstants.CENTER);
        scoreLabel.setForeground(Colors.HEADER);

        headerInfo.add(winnerLabel);
        headerInfo.add(scoreLabel);

        JPanel statsPanel = new JPanel(new GridLayout(1,2,30,0));
        statsPanel.setBackground(Colors.BG);
        statsPanel.add(title, BorderLayout.NORTH);
        statsPanel.add(headerInfo, BorderLayout.CENTER);
        add(statsPanel, BorderLayout.NORTH);

        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        boardsPanel.setBackground(Colors.BG);
        boardsPanel.add(buildBoardPanel("Player 1", player1Board, null));
        boardsPanel.add(buildBoardPanel("Player 2", player2Board, null));
        add(boardsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Colors.BG);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.setBackground(Colors.BUTTON);
        playAgainButton.setForeground(Colors.HEADER);
        playAgainButton.setBorder(BorderFactory.createLineBorder(Colors.BORDER));
        playAgainButton.addActionListener(e -> {
            if (playAgainListener != null) playAgainListener.onPlayAgain();
        });

        buttonPanel.add(playAgainButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void renderBoards() {
        renderGrid(true, true);
        renderGrid(false,true);
    }
}
