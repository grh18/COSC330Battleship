import java.io.*;
import java.net.*;
import javax.swing.*;

public class BattleShipController {
    static int player1Score = 0;
    static int player2Score = 0;

    static BattleShipModel model;
    static BattleShipView view;
    static PrintWriter out;
    static BufferedReader in;
    static boolean isHost;
    static int pendingCol, pendingRow;
    static boolean boardSent = false;
    static String opponentBoardStr = null;

    public static void main(String[] args) {
        model = new BattleShipModel();

        String[] options = {"Host Game", "Join Game"};
        int choice = JOptionPane.showOptionDialog(null, "Choose your role:", "Battleship",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice < 0) System.exit(0);
        isHost = (choice == 0);

        try {
            if (isHost) {
                String portStr = JOptionPane.showInputDialog(null, "Port to host on:", "6000");
                if (portStr == null) System.exit(0);
                int port = Integer.parseInt(portStr.trim());
                ServerSocket ss = new ServerSocket(port);
                JOptionPane.showMessageDialog(null,
                        "Hosting on port " + port + ".\nClick OK, then wait for your opponent to connect.\nThe game will appear automatically.");
                Socket socket = ss.accept();
                ss.close();
                out = new PrintWriter(socket.getOutputStream(), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } else {
                String ip = JOptionPane.showInputDialog(null, "Enter host IP address:");
                if (ip == null) System.exit(0);
                String portStr = JOptionPane.showInputDialog(null, "Port:", "6000");
                if (portStr == null) System.exit(0);
                Socket socket = new Socket(ip.trim(), Integer.parseInt(portStr.trim()));
                out = new PrintWriter(socket.getOutputStream(), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                model.setTurn(false);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Connection error: " + e.getMessage());
            System.exit(1);
        }

        SwingUtilities.invokeLater(() -> {
            view = new BattleShipView(model);
            startListening();
            startPreGame();
        });
    }

    static void startListening() {
        new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    final String msg = line;
                    SwingUtilities.invokeLater(() -> handleMessage(msg));
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(() ->
                        JOptionPane.showMessageDialog(view, "Connection lost: " + e.getMessage()));
            }
        }).start();
    }

    static void handleMessage(String msg) {
        if (msg.startsWith("BOARD:")) {
            opponentBoardStr = msg.substring(6);
            if (boardSent) startActiveGame();

        } else if (msg.startsWith("MOVE:")) {
            String[] parts = msg.substring(5).split(",");
            int col = Integer.parseInt(parts[0]);
            int row = Integer.parseInt(parts[1]);
            boolean hit = model.fire(col, row);
            out.println(hit ? "HIT" : "MISS");
            view.getPanelActiveGame().updateNetworked(model.getPlayerTurn());

        } else if (msg.equals("HIT")) {
            model.setTurn(true);
            model.fire(pendingCol, pendingRow);
            view.getPanelActiveGame().setStatus("Hit!");
            view.getPanelActiveGame().updateNetworked(true);
            if (model.checkForPlayer1Win()) {
                player1Score++;
                out.println("WIN");
                endGame("You");
            }

        } else if (msg.equals("MISS")) {
            model.setTurn(true);
            model.fire(pendingCol, pendingRow);
            view.getPanelActiveGame().setStatus("Miss...");
            view.getPanelActiveGame().updateNetworked(false);

        } else if (msg.equals("WIN")) {
            player2Score++;
            endGame("Opponent");

        } else if (msg.equals("AGAIN")) {
            startPreGame();
        }
    }

    static void endGame(String winner) {
        view.getPanelPostGame().setPlayAgainListener(() -> {
            out.println("AGAIN");
            startPreGame();
        });
        view.getPanelPostGame().update(winner, player1Score, player2Score);
        view.setScene("post");
    }

    static void startPreGame() {
        model.resetGame();
        if (!isHost) model.setTurn(false);
        boardSent = false;
        opponentBoardStr = null;
        view.setScene("pregame");
        view.getPanelPreGame().update();
        view.getPanelPreGame().setReadyListener(() -> {
            out.println("BOARD:" + model.getBoard1String());
            boardSent = true;
            view.getPanelPreGame().setStatus("Waiting for opponent's board...");
            if (opponentBoardStr != null) startActiveGame();
        });
    }

    static void startActiveGame() {
        model.setPlayer2Board(opponentBoardStr);
        model.setPlayer2Ships();
        PanelActiveGame gamePanel = view.getPanelActiveGame();
        gamePanel.setMoveListener((col, row) -> {
            if (model.alreadyPlayed(col, row)) {
                gamePanel.setStatus("Already played that move!");
                return;
            }
            pendingCol = col;
            pendingRow = row;
            out.println("MOVE:" + col + "," + row);
            gamePanel.setWaiting();
        });
        view.setScene("active");
        gamePanel.updateNetworked(model.getPlayerTurn());
    }
}
