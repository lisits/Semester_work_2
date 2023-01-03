package ownservlet;

public class Car {
    //Attributes
    private int row;
    private int col;
    private int tailSize;
    private String dir;

    //Constructor
    public Car(int player) {
        tailSize = 0;
        if (player == 1) {
            row = 24;
            col = 0;
            dir = "r";
        } else {
            row = 24;
            col = 49;
            dir = "l";
        }
    }

    //Get & Set methods
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getTailSize() {
        return tailSize;
    }

    public void setTailSize(int tailSize) {
        this.tailSize = tailSize;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }
}	
