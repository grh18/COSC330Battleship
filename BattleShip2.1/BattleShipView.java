import javax.swing.*;
import java.awt.*;


public class BattleShipView extends JFrame{
    public final static int CELL_SIZE = 50;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    private PanelActiveGame panelActiveGame;
    private PanelPreGame panelPreGame;
    private PanelPostGame panelPostGame;

    public BattleShipView(BattleShipModel model) {
        setTitle("BattleShip");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        panelActiveGame = new PanelActiveGame(model);
        panelPreGame = new PanelPreGame(model);
        panelPostGame = new PanelPostGame(model);

        cardPanel.add(panelPreGame, "pregame");
        cardPanel.add(panelActiveGame, "active");
        cardPanel.add(panelPostGame, "post");

        add(cardPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setScene(String name) {
        cardLayout.show(cardPanel, name);
    }

    public PanelActiveGame getPanelActiveGame() { return panelActiveGame; }
    public PanelPreGame getPanelPreGame() { return panelPreGame; }
    public PanelPostGame getPanelPostGame() { return panelPostGame; }
}
