import java.util.ArrayList;

public class Ship {
    private int length;
    private char ID;
    private boolean orientation; // True means verticle
    private ArrayList<int[]> positions;
    private boolean sunk;

    public Ship(int length, char ID) {
        this.length = length;
        this.ID = ID;
        positions = new ArrayList<>();
        this.orientation = true;
        this.sunk = false;
    }

    public boolean checkIfSunk(char[][] board) {
        for (int[] pos : positions) {
            if (!Character.isLowerCase(board[pos[1]][pos[0]])) return false;
        }
        sunk = true;
        return true;
    }

    public boolean getOrientation() { return orientation; }
    public char getID() { return ID; }
    public int getLength() { return length; }
    public int[][] getPositions() { return positions.toArray(int[][]::new);}
    public boolean isSunk() { return sunk; }

    public void setOrientation(boolean orientation) { this.orientation = orientation; }
    public void addPosition(int[] position) { positions.add(position); }
    public void sink(boolean sunk) { this.sunk = sunk; }

    public void removePositions() { positions = new ArrayList<>();}
}
