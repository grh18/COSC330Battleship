import java.util.ArrayList;

public class Ship {
    private int length;
    private char ID;
    private boolean orientation; // True means verticle
    private ArrayList<int[]> positions;

    public Ship(int length, char ID) {
        this.length = length;
        this.ID = ID;
        positions = new ArrayList<>();
        this.orientation = true;
    }

    public boolean getOrientation() { return orientation; }
    public char getID() { return ID; }
    public int getLength() { return length; }
    public int[][] getPositions() { return positions.toArray(int[][]::new);}

    public void setOrientation(boolean orientation) { this.orientation = orientation; }
    public void addPosition(int[] position) { positions.add(position); }

    public void removePositions() { positions = new ArrayList<>();}
}
