import java.util.ArrayList;
import java.util.Random;

public class BattleShipModel {
    public static final int GRID_SIZE = 10;
    private ArrayList<Ship> player1Ships;
    private ArrayList<Ship> player2Ships;
    private char[][] player1Board;
    private char[][] player2Board;
    private Random rand = new Random();
    private boolean turn = true;                // True means player 1


    public BattleShipModel() {
        player1Board = new char[GRID_SIZE][GRID_SIZE];
        player2Board = new char[GRID_SIZE][GRID_SIZE];
        player1Ships = new ArrayList<Ship>();
        player2Ships = new ArrayList<Ship>();
        clearBoards();
        generateShips();
    }

    public void resetGame() {
        clearBoards();
        generateShips(); // regenerate ship list fresh
        turn = true;
    }


    public void setPlayer2Board(String str) {
        int counter = 0;
        for (int i = 0; i < player2Board.length; i++) {
            for (int j = 0; j < player2Board[i].length; j++) {
                player2Board[i][j] = str.charAt(counter);
                counter++;
            }
        }
    }

    public void setPlayer2Ships() {
        for (Ship ship : player2Ships) {
            ship.removePositions();

            ArrayList<int[]> positions = new ArrayList<>();
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (player2Board[i][j] == ship.getID()) {
                        positions.add(new int[]{j,i});
                    }
                }
            }

            boolean verticle = positions.get(0)[0] == positions.get(1)[0];
            ship.setOrientation(verticle);

            positions.sort((a, b) -> verticle ? a[1] - b[1] : a[0] - b[0]);

            for (int[] pos : positions) {
                ship.addPosition(pos);
            }
        }
    }


    public boolean checkForPlayer1Win() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (Character.isUpperCase(player2Board[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }


    public boolean checkForPlayer2Win() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (Character.isUpperCase(player1Board[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }


    public boolean fire(int col, int row) throws IndexOutOfBoundsException{
        char[][] board = (turn ? player2Board : player1Board);
        if (board[row][col] == ' ') {
            board[row][col] = 'x';
            turn = !turn;
            return false;
        } else {
            board[row][col] = Character.toLowerCase(board[row][col]);
            return true;
        }
        
    }


    public boolean placeShip(int col, int row, Ship ship) {
        boolean verticle = ship.getOrientation();
        ship.removePositions();
        try {
            for (int i = 0; i < ship.getLength(); i++) {
                int c = verticle ? col : col + i;
                int r = verticle ? row + i : row;
                if (player1Board[r][c] != ' ') {
                    return false;
                }
            }
            for (int i = 0; i < ship.getLength(); i++) {
                int c = verticle ? col : col + i;
                int r = verticle ? row + i : row;
                player1Board[r][c] = ship.getID();
                ship.addPosition(new int[]{c,r});
            }
        } catch (IndexOutOfBoundsException oob) {
            return false;
        }
        return true;
    }


    public void clearBoards() {
        for (int i = 0; i < player1Board.length; i++) {
            for (int j = 0; j < player1Board[i].length; j++) {
                player1Board[i][j] = ' ';
                player2Board[i][j] = ' ';
            }
        }
    }


    public void randomizeBoard() {
        clearBoards();
        for (Ship ship : player1Ships) {
            ship.setOrientation(rand.nextInt(2) != 0);
            while (!placeShip(rand.nextInt(10), rand.nextInt(10), ship)) {}
        }
    }


    public void generateShips() {
        player1Ships.clear();
        player2Ships.clear();
        int[][] shipGen = {{2,'C'}, {3,'S'}, {3,'D'}, {4,'A'}, {5,'B'}};
        for (int[] s : shipGen) {
            player1Ships.add(new Ship(s[0], (char)s[1]));
        }
        for (int[] s : shipGen) {
            player2Ships.add(new Ship(s[0], (char)s[1]));
        }
    }

    
    public boolean alreadyPlayed(int x, int y) { 
        char cell = (turn ? player2Board[y][x] : player1Board[y][x]);
        return Character.isLowerCase(cell);
    }


    public char[][] getPlayer1Board() { return player1Board; }
    public char[][] getPlayer2Board() { return player2Board; }
    public boolean getPlayerTurn() { return turn; }
    public ArrayList<Ship> getPlayer1Ships() { return player1Ships; }
    public ArrayList<Ship> getPlayer2Ships() { return player2Ships; }
    public void setTurn(boolean turn) { this.turn = turn; }

    public String getBoard1String() {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                str.append(player1Board[i][j]);
            }
        }
        return str.toString();
    }

}
