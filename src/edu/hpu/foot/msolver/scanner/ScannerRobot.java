package edu.hpu.foot.msolver.scanner;

public interface ScannerRobot {
    void start();
    int[][] scan();
    void click(int x,int y);
    void flag(int x,int y);
    void chord(int x,int y);
    int getGameWidth();
    int getGameHeight();
    int getMimes();
    boolean isGameOver();
//    boolean isWin();
}
