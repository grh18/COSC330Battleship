import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PanelPreGame extends GamePanel {
    private JLabel statusLabel;

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

    public void setStatus(String msg) { statusLabel.setText(msg); }

    public void startUI() {
        setLayout(new BorderLayout(0,0));
        setBackground(Colors.BG);
        setBorder(BorderFactory.createEmptyBorder(16,20,16,20));

        JLabel title = new JLabel("Place Your Ships", SwingConstants.CENTER);
        title.setForeground(Colors.HEADER);
        title.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        add(title, BorderLayout.NORTH);

        JPanel centered = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centered.setBackground(Colors.BG);
        centered.add(buildBoardPanel("Player 1", player1Board, null));
        add(centered, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2,1,0,4));
        bottom.setBackground(Colors.BG);
        bottom.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));

        statusLabel = new JLabel("Randomize Your Ships or Ready Up!", SwingConstants.CENTER);
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
    }

    public void update() {
        renderGrid(true, true);
    }

    public void styleButton(JButton button) {
        button.setBackground(Colors.BUTTON);
        button.setForeground(Colors.HEADER);
        button.setBorder(BorderFactory.createLineBorder(Colors.BORDER));
    }
}
