package edu.hpu.foot.msolver.solver.impl;

import edu.hpu.foot.msolver.solver.ClickResult;
import edu.hpu.foot.msolver.solver.MSolver;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/*
  todo -
    read / write calibration settings

  Minor defects / bugs:
    1) Calibration routine kind of sucks, can't calibrate at
         small resolutions and can't calibrate non-empty board
    2) Death / win detection kind of sucks, can't distinguish
         win from loss and sometimes fails to detect either
    3) Clicking order is highly non-human
    4) Endgame solver is inefficient: we could make it kick in earlier if
         it was more efficient

  Known but non-fixable defects:
    1) Cannot automatically detect number of mines

    翻译 zack
    读/写 标准设置
    缺陷、bug
      1） 不能校准屏幕，小分辨率下不准
      2）胜利或失败的检测很烂
      3）点击顺序是非人类的
      4）Endgame解算器效率低下：如果效率更高，我们可以在之前启动它
    已知但不可修复的缺陷：
      1）不能自动检测地雷的数量
*/
public class MSolverImpl implements MSolver {


    private final List<ClickResult> results;
    private boolean noFlag = false;

    public MSolverImpl(int width, int height, int mimes) {
        BoardWidth = width;
        BoardHeight = height;
        TOT_MINES = mimes;
        onScreen = new int[height][width];
        flags = new boolean[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                onScreen[i][j] = -1;
                flags[i][j] = false;
            }
        }
        results = new ArrayList<>(10);
    }

    public void setNoFlag(boolean noFlag) {
        this.noFlag = noFlag;
    }

    //增加设置，设置noflag

    public List<ClickResult> solve(int[][] board) {
//        if (!checkInput(board)) {//清除缓存数据
//
//        }
        //赋值数据
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {
                onScreen[i][j] = board[i][j];
            }
        }
        results.clear();
        attemptFlagMine();
        if (!attemptMove()) {
            for (int i = 0; i < BoardHeight; i++) {
                for (int j = 0; j < BoardWidth; j++) {
                    onScreen[i][j] = board[i][j];
                }
            }
            tankSolver();
        }
        return results;
    }


    // Internal representation of the board state as displayed on the screen.
    // 1-8 means that the square there is that number
    // 0 means that it's actually empty
    // -1 means it's not opened yet
    // -2 means it's outside the boundries of the board
    // -3 means a mine
    // -10 means that something went wrong and we should exit the program
    int[][] onScreen = null;
    int BoardHeight, BoardWidth;

    // List of squares in which we know there are mines
    boolean[][] flags = null;

    int numMines = 0;
    int TOT_MINES = 99;


    void clickOn(int x, int y) {
        ClickResult result = new ClickResult(x, y, ClickResult.T_CLICK);
        if (!results.contains(result))
            results.add(result);
    }

    void chordOn(int x, int y) {
        if (noFlag) {
            if (onScreen(y + 1, x + 1) != -10 && !flags[y + 1][x + 1]) {
                clickOn(x + 1, y + 1);
            }
            if (onScreen(y, x + 1) != -10 && !flags[y][x + 1]) {
                clickOn(x + 1, y);
            }
            if (onScreen(y - 1, x + 1) != -10 && !flags[y - 1][x + 1]) {
                clickOn(x + 1, y - 1);
            }

            if (onScreen(y + 1, x) != -10 && !flags[y + 1][x]) {
                clickOn(x, y + 1);
            }
            if (onScreen(y - 1, x) != -10 && !flags[y - 1][x]) {
                clickOn(x, y - 1);
            }

            if (onScreen(y + 1, x - 1) != -10 && !flags[y + 1][x - 1]) {
                clickOn(x - 1, y + 1);
            }
            if (onScreen(y, x - 1) != -10 && !flags[y][x - 1]) {
                clickOn(x - 1, y);
            }
            if (onScreen(y - 1, x - 1) != -10 && !flags[y - 1][x - 1]) {
                clickOn(x - 1, y - 1);
            }
            return;
        }
        results.add(new ClickResult(x, y, ClickResult.T_CHRON));
    }

    void flagOn(int x, int y) {
        if (noFlag) return;
        results.add(new ClickResult(x, y, ClickResult.T_FLAG));
    }

    // Attempt to deduce squares that we know have mines
    // More specifically if number of squares around it = its number
    void attemptFlagMine() {

        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {

                if (onScreen(i, j) >= 1) {
                    int curNum = onScreen(i, j);

                    // Flag necessary squares
                    if (curNum == countFreeSquaresAround(onScreen, i, j)) {
                        for (int ii = 0; ii < BoardHeight; ii++) {
                            for (int jj = 0; jj < BoardWidth; jj++) {
                                if (Math.abs(ii - i) <= 1 && Math.abs(jj - j) <= 1) {
                                    if (onScreen(ii, jj) == -1 && !flags[ii][jj]) {
                                        flags[ii][jj] = true;
                                        flagOn(jj, ii);
                                    }
                                }
                            }
                        }
                    }


                }
            }
        }

    }


    // Attempt to deduce a spot that should be free and click it
    // More specifically:
    // Find a square where the number of flags around it is the same as it
    // Then click every empty square around it
    boolean attemptMove() {

        boolean success = false;
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {

                if (onScreen(i, j) >= 1) {

                    // Count how many mines around it
                    int curNum = onScreen[i][j];
                    int mines = countFlagsAround(flags, i, j);
                    int freeSquares = countFreeSquaresAround(onScreen, i, j);

                    // Click on the deduced non-mine squares
                    if (curNum == mines && freeSquares > mines) {
                        success = true;

                        // Use the chord or the classical algorithm
                        if (freeSquares - mines > 1) {
                            chordOn(j, i);
                            onScreen[i][j] = 0; // hack to make it not overclick a square
                            continue;
                        }

                        // Old algorithm: don't chord
                        for (int ii = 0; ii < BoardHeight; ii++) {
                            for (int jj = 0; jj < BoardWidth; jj++) {
                                if (Math.abs(ii - i) <= 1 && Math.abs(jj - j) <= 1) {
                                    if (onScreen(ii, jj) == -1 && !flags[ii][jj]) {
                                        clickOn(jj, ii);
                                        System.out.println("chlick...............................");
                                        onScreen[ii][jj] = 0;
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }

//        if (success) {
//            return;
//        }
        return success;

        // Bring in the big guns

    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // A boundry square is an unopened square with opened squares near it.
    boolean isBoundry(int[][] board, int i, int j) {
        if (board[i][j] != -1) {
            return false;
        }

        boolean oU = false, oD = false, oL = false, oR = false;
        if (i == 0) {
            oU = true;
        }
        if (j == 0) {
            oL = true;
        }
        if (i == BoardHeight - 1) {
            oD = true;
        }
        if (j == BoardWidth - 1) {
            oR = true;
        }
        boolean isBoundry = false;

        if (!oU && board[i - 1][j] >= 0) {
            isBoundry = true;
        }
        if (!oL && board[i][j - 1] >= 0) {
            isBoundry = true;
        }
        if (!oD && board[i + 1][j] >= 0) {
            isBoundry = true;
        }
        if (!oR && board[i][j + 1] >= 0) {
            isBoundry = true;
        }
        if (!oU && !oL && board[i - 1][j - 1] >= 0) {
            isBoundry = true;
        }
        if (!oU && !oR && board[i - 1][j + 1] >= 0) {
            isBoundry = true;
        }
        if (!oD && !oL && board[i + 1][j - 1] >= 0) {
            isBoundry = true;
        }
        if (!oD && !oR && board[i + 1][j + 1] >= 0) {
            isBoundry = true;
        }

        return isBoundry;
    }

    // TANK solver: slow and heavyweight backtrack solver designed to
    // solve any conceivable position! (in development)
    void tankSolver() {
//重新检测数据，可能上面的方法修改了数据，去确认一下 TODO 这里只是简单注释掉了
//        // Be extra sure it's consistent
//        Thread.sleep(100);
//        robot.mouseMove(0,0);
//        Thread.sleep(20);
//        updateOnScreen();
//        robot.mouseMove(mouseLocX,mouseLocY);
//        //dumpPosition();
//        if(!checkConsistency()) return;

        // Timing
        long tankTime = System.currentTimeMillis();

        ArrayList<Pair> borderTiles = new ArrayList<Pair>();
        ArrayList<Pair> allEmptyTiles = new ArrayList<Pair>();

        // Endgame case: if there are few enough tiles, don't bother with border tiles.
        borderOptimization = false;
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {
                if (onScreen(i, j) == -1 && !flags[i][j]) {
                    allEmptyTiles.add(new Pair(i, j));
                }
            }
        }

        // Determine all border tiles
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {
                if (isBoundry(onScreen, i, j) && !flags[i][j]) {
                    borderTiles.add(new Pair(i, j));
                }
            }
        }

        // Count how many squares outside the knowable range
        int numOutSquares = allEmptyTiles.size() - borderTiles.size();
        if (numOutSquares > BF_LIMIT) {
            borderOptimization = true;
        } else {
            borderTiles = allEmptyTiles;
        }

        // Something went wrong
        if (borderTiles.size() == 0) {
            return;
        }

        // Run the segregation routine before recursing one by one
        // Don't bother if it's endgame as doing so might make it miss some cases
        ArrayList<ArrayList<Pair>> segregated;
        if (!borderOptimization) {
            segregated = new ArrayList<ArrayList<Pair>>();
            segregated.add(borderTiles);
        } else {
            segregated = tankSegregate(borderTiles);
        }

        int totalMultCases = 1;
        boolean success = false;
        double prob_best = 0; // Store information about the best probability
        int prob_besttile = -1;
        int prob_best_s = -1;
        for (int s = 0; s < segregated.size(); s++) {

            // Copy everything into temporary constructs
            tank_solutions = new ArrayList<boolean[]>();
            tank_board = onScreen.clone();
            knownMine = flags.clone();

            knownEmpty = new boolean[BoardHeight][BoardWidth];
            for (int i = 0; i < BoardHeight; i++) {
                for (int j = 0; j < BoardWidth; j++) {
                    if (tank_board[i][j] >= 0) {
                        knownEmpty[i][j] = true;
                    } else {
                        knownEmpty[i][j] = false;
                    }
                }
            }

            // Compute solutions -- here's the time consuming step
            tankRecurse(segregated.get(s), 0);

            // Something screwed up
            if (tank_solutions.size() == 0) {
                return;
            }

            // Check for solved squares
            for (int i = 0; i < segregated.get(s).size(); i++) {
                boolean allMine = true;
                boolean allEmpty = true;
                for (boolean[] sln : tank_solutions) {
                    if (!sln[i]) {
                        allMine = false;
                    }
                    if (sln[i]) {
                        allEmpty = false;
                    }
                }

                Pair<Integer, Integer> q = segregated.get(s).get(i);
                int qi = q.getFirst();
                int qj = q.getSecond();

                // Muahaha
                if (allMine) {
                    flags[qi][qj] = true;
                    flagOn(qj, qi);
                }
                if (allEmpty) {
                    success = true;
                    clickOn(qj, qi);
                }
            }

            totalMultCases *= tank_solutions.size();

            // Calculate probabilities, in case we need it
            if (success) {
                continue;
            }
            int maxEmpty = -10000;
            int iEmpty = -1;
            for (int i = 0; i < segregated.get(s).size(); i++) {
                int nEmpty = 0;
                for (boolean[] sln : tank_solutions) {
                    if (!sln[i]) {
                        nEmpty++;
                    }
                }
                if (nEmpty > maxEmpty) {
                    maxEmpty = nEmpty;
                    iEmpty = i;
                }
            }
            double probability = (double) maxEmpty / (double) tank_solutions.size();

            if (probability > prob_best) {
                prob_best = probability;
                prob_besttile = iEmpty;
                prob_best_s = s;
            }

        }

        // But wait! If there's any hope, bruteforce harder (by a factor of 32x)!
        if (BF_LIMIT == 8 && numOutSquares > 8 && numOutSquares <= 13) {
            System.out.println("Extending bruteforce horizon...");
            BF_LIMIT = 13;
            tankSolver();
            BF_LIMIT = 8;
            return;
        }

        tankTime = System.currentTimeMillis() - tankTime;
        if (success) {
            System.out.printf(
                    "TANK Solver successfully invoked at step %d (%dms, %d cases)%s\n",
                    numMines, tankTime, totalMultCases, (borderOptimization ? "" : "*"));
            return;
        }

        // Take the guess, since we can't deduce anything useful
        System.out.printf(
                "TANK Solver guessing with probability %1.2f at step %d (%dms, %d cases)%s\n",
                prob_best, numMines, tankTime, totalMultCases,
                (borderOptimization ? "" : "*"));
        Pair<Integer, Integer> q = segregated.get(prob_best_s).get(prob_besttile);
        int qi = q.getFirst();
        int qj = q.getSecond();
        clickOn(qj, qi);

    }


    // Segregation routine: if two regions are independant then consider
    // them as separate regions
    ArrayList<ArrayList<Pair>>
    tankSegregate(ArrayList<Pair> borderTiles) {

        ArrayList<ArrayList<Pair>> allRegions = new ArrayList<ArrayList<Pair>>();
        ArrayList<Pair> covered = new ArrayList<Pair>();

        while (true) {

            LinkedList<Pair> queue = new LinkedList<Pair>();
            ArrayList<Pair> finishedRegion = new ArrayList<Pair>();

            // Find a suitable starting point
            for (Pair firstT : borderTiles) {
                if (!covered.contains(firstT)) {
                    queue.add(firstT);
                    break;
                }
            }

            if (queue.isEmpty()) {
                break;
            }

            while (!queue.isEmpty()) {

                Pair<Integer, Integer> curTile = queue.poll();
                int ci = curTile.getFirst();
                int cj = curTile.getSecond();

                finishedRegion.add(curTile);
                covered.add(curTile);

                // Find all connecting tiles
                for (Pair<Integer, Integer> tile : borderTiles) {
                    int ti = tile.getFirst();
                    int tj = tile.getSecond();

                    boolean isConnected = false;

                    if (finishedRegion.contains(tile)) {
                        continue;
                    }

                    if (Math.abs(ci - ti) > 2 || Math.abs(cj - tj) > 2) {
                        isConnected = false;
                    } else {
                        // Perform a search on all the tiles
                        tilesearch:
                        for (int i = 0; i < BoardHeight; i++) {
                            for (int j = 0; j < BoardWidth; j++) {
                                if (onScreen(i, j) > 0) {
                                    if (Math.abs(ci - i) <= 1 && Math.abs(cj - j) <= 1 &&
                                            Math.abs(ti - i) <= 1 && Math.abs(tj - j) <= 1) {
                                        isConnected = true;
                                        break tilesearch;
                                    }
                                }
                            }
                        }
                    }

                    if (!isConnected) {
                        continue;
                    }

                    if (!queue.contains(tile)) {
                        queue.add(tile);
                    }

                }
            }

            allRegions.add(finishedRegion);

        }

        return allRegions;

    }

    int[][] tank_board = null;
    boolean[][] knownMine = null;
    boolean[][] knownEmpty = null;
    ArrayList<boolean[]> tank_solutions;

    // Should be true -- if false, we're bruteforcing the endgame
    boolean borderOptimization;
    int BF_LIMIT = 8;

    // Recurse from depth k (0 is root)
    // Assumes the tank variables are already set; puts solutions in
    // the static arraylist.
    void tankRecurse(ArrayList<Pair> borderTiles, int k) {

        // Return if at this point, it's already inconsistent
        int flagCount = 0;
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {

                // Count flags for endgame cases
                if (knownMine[i][j]) {
                    flagCount++;
                }

                int num = tank_board[i][j];
                if (num < 0) {
                    continue;
                }

                // Total bordering squares
                int surround = 0;
                if ((i == 0 && j == 0) || (i == BoardHeight - 1 && j == BoardWidth - 1)) {
                    surround = 3;
                } else if (i == 0 || j == 0 || i == BoardHeight - 1 || j == BoardWidth - 1) {
                    surround = 5;
                } else {
                    surround = 8;
                }

                int numFlags = countFlagsAround(knownMine, i, j);
                int numFree = countFlagsAround(knownEmpty, i, j);

                // Scenario 1: too many mines
                if (numFlags > num) {
                    return;
                }

                // Scenario 2: too many empty
                if (surround - numFree < num) {
                    return;
                }
            }
        }

        // We have too many flags
        if (flagCount > TOT_MINES) {
            return;
        }

        // Solution found!
        if (k == borderTiles.size()) {

            // We don't have the exact mine count, so no
            if (!borderOptimization && flagCount < TOT_MINES) {
                return;
            }

            boolean[] solution = new boolean[borderTiles.size()];
            for (int i = 0; i < borderTiles.size(); i++) {
                Pair<Integer, Integer> s = borderTiles.get(i);
                int si = s.getFirst();
                int sj = s.getSecond();
                solution[i] = knownMine[si][sj];
            }
            tank_solutions.add(solution);
            return;
        }

        Pair<Integer, Integer> q = borderTiles.get(k);
        int qi = q.getFirst();
        int qj = q.getSecond();

        // Recurse two positions: mine and no mine
        knownMine[qi][qj] = true;
        tankRecurse(borderTiles, k + 1);
        knownMine[qi][qj] = false;

        knownEmpty[qi][qj] = true;
        tankRecurse(borderTiles, k + 1);
        knownEmpty[qi][qj] = false;

    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    boolean checkConsistency() {
        for (int i = 0; i < BoardHeight; i++) {
            for (int j = 0; j < BoardWidth; j++) {

                int freeSquares = countFreeSquaresAround(onScreen, i, j);
                int numFlags = countFlagsAround(flags, i, j);

                if (onScreen(i, j) == 0 && freeSquares > 0) {
                    return false;
                }
                if ((onScreen(i, j) - numFlags) > 0 && freeSquares == 0) {
                    return false;
                }

            }
        }

        return true;
    }

    // How many unopened squares around this square?
    int countFreeSquaresAround(int[][] board, int i, int j) {
        int freeSquares = 0;

        if (onScreen(i - 1, j) == -1) {
            freeSquares++;
        }
        if (onScreen(i + 1, j) == -1) {
            freeSquares++;
        }
        if (onScreen(i, j - 1) == -1) {
            freeSquares++;
        }
        if (onScreen(i, j + 1) == -1) {
            freeSquares++;
        }
        if (onScreen(i - 1, j - 1) == -1) {
            freeSquares++;
        }
        if (onScreen(i - 1, j + 1) == -1) {
            freeSquares++;
        }
        if (onScreen(i + 1, j - 1) == -1) {
            freeSquares++;
        }
        if (onScreen(i + 1, j + 1) == -1) {
            freeSquares++;
        }

        return freeSquares;
    }

    // Remove the need for edge detection every fricking time
    int onScreen(int i, int j) {
        if (i < 0 || j < 0 || i > BoardHeight - 1 || j > BoardWidth - 1) {
            return -10;
        }
        return onScreen[i][j];
    }


    // How many flags exist around this square?
    int countFlagsAround(boolean[][] array, int i, int j) {
        int mines = 0;

        // See if we're on the edge of the board
        boolean oU = false, oD = false, oL = false, oR = false;
        if (i == 0) {
            oU = true;
        }
        if (j == 0) {
            oL = true;
        }
        if (i == BoardHeight - 1) {
            oD = true;
        }
        if (j == BoardWidth - 1) {
            oR = true;
        }

        if (!oU && array[i - 1][j]) {
            mines++;
        }
        if (!oL && array[i][j - 1]) {
            mines++;
        }
        if (!oD && array[i + 1][j]) {
            mines++;
        }
        if (!oR && array[i][j + 1]) {
            mines++;
        }
        if (!oU && !oL && array[i - 1][j - 1]) {
            mines++;
        }
        if (!oU && !oR && array[i - 1][j + 1]) {
            mines++;
        }
        if (!oD && !oL && array[i + 1][j - 1]) {
            mines++;
        }
        if (!oD && !oR && array[i + 1][j + 1]) {
            mines++;
        }

        return mines;
    }


    // Copied from http://stackoverflow.com/questions/156275/
    static class Pair<A, B> {

        private A first;
        private B second;

        public Pair(A first, B second) {
            super();
            this.first = first;
            this.second = second;
        }

        public int hashCode() {
            int hashFirst = first != null ? first.hashCode() : 0;
            int hashSecond = second != null ? second.hashCode() : 0;

            return (hashFirst + hashSecond) * hashSecond + hashFirst;
        }

        public boolean equals(Object other) {
            if (other instanceof Pair) {
                Pair otherPair = (Pair) other;
                return
                        ((this.first == otherPair.first ||
                                (this.first != null && otherPair.first != null &&
                                        this.first.equals(otherPair.first))) &&
                                (this.second == otherPair.second ||
                                        (this.second != null && otherPair.second != null &&
                                                this.second.equals(otherPair.second))));
            }

            return false;
        }

        public String toString() {
            return "(" + first + ", " + second + ")";
        }

        public A getFirst() {
            return first;
        }

        public void setFirst(A first) {
            this.first = first;
        }

        public B getSecond() {
            return second;
        }

        public void setSecond(B second) {
            this.second = second;
        }
    }
}