package edu.hpu.foot.msolver.test;

import edu.hpu.foot.msolver.solver.ClickResult;
import edu.hpu.foot.msolver.solver.impl.MSolverImpl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Random;

public class Game {

    private final int width;
    private final int height;
    private final int mines;

    private final boolean[][] flags;
    private final int[][] board;

    private int openCount = 0;

    public Game(int width, int height, int mines) {
        this.width = width;
        this.height = height;
        this.mines = mines;
        flags = new boolean[height][width];
        board = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                board[i][j] = TileState.T_NONE;
            }
        }
        initGames();
    }

    private void initGames() {
        int count = mines;
        Random random = new Random();
        while (count > 0) {
            int x, y;
            do {
                x = random.nextInt(height);
                y = random.nextInt(width);
            } while (flags[x][y]);
            flags[x][y] = true;
            count--;
        }
    }

    public int cal(int x, int y) {
        if (flags[x][y]) {
            return TileState.T_MIME;
        }
        int count = 0;
        if (x > 0) {
            if (y > 0 && flags[x - 1][y - 1]) {
                count++;
            }
            if (flags[x - 1][y]) {
                count++;
            }
            if (y < width - 1 && flags[x - 1][y + 1]) {
                count++;
            }
        }

        if (y > 0 && flags[x][y - 1]) {
            count++;
        }
        if (y < width - 1 && flags[x][y + 1]) {
            count++;
        }

        if (x < height - 1) {
            if (y > 0 && flags[x + 1][y - 1]) {
                count++;
            }
            if (flags[x + 1][y]) {
                count++;
            }
            if (y < width - 1 && flags[x + 1][y + 1]) {
                count++;
            }
        }
        return count;
    }

    public String print() {
        StringBuffer sb = new StringBuffer();
//        //add head ╔═══════╤═══════╤═══════╦═══════╤═══════╤═══════╦═══════╤═══════╤═══════╗
//        sb.append("╔");
//        for (int i = 0; i < width; i++) {
//            sb.append("═╤");
//        }
//        sb.replace(sb.length() - 1, sb.length(), "╗\n");
//
//        for (int i = 0; i < height; i++) {
//            sb.append("║");
//            for (int j = 0; j < width; j++) {
//                sb.append(TileState.toString(board[i][j])).append("│");
//
//
//            }
//            sb.replace(sb.length() - 1, sb.length(), "║\n");
//        }
//
//        sb.append("╚");
//        for (int i = 0; i < width; i++) {
//            sb.append("═╧");
//        }
//        sb.replace(sb.length() - 1, sb.length(), "╝\n");

        for (int i = 0; i < height; i++) {
            sb.append("|");
            for (int j = 0; j < width; j++) {
                sb.append(TileState.toString(board[i][j]));
            }
            sb.append("|\n");
        }
        return sb.toString();

    }

    public void openAll() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {

                int t = cal(i, j);
                if (board[i][j] == TileState.T_NONE) {
                    board[i][j] = t;
                } else if (board[i][j] == TileState.T_FLAG) {
                    if (t != TileState.T_MIME) {
                        board[i][j] = TileState.T_FLAG_ERROR;
                    }
                }
            }
        }
    }

    public boolean gameOver() {
        return openCount < 0 || openCount == width * height - mines;
    }

    public boolean isWin() {
        return openCount > 0 && gameOver();
    }

    public boolean clickFlag(int x,int y) {
        if (board[x][y]==TileState.T_NONE) {
            board[x][y] = TileState.T_FLAG;
        }
        return true;
    }
    public boolean chord(int x,int y) {
            if (x > 0) {
                if (y > 0) {
                    click(x - 1, y - 1);
                }
                click(x - 1, y);
                if (y < width - 1) {
                    click(x - 1, y + 1);
                }
            }

            if (y > 0) {
                click(x, y - 1);
            }

            if (y < width - 1) {
                click(x, y + 1);
            }

            if (x < height - 1) {
                if (y > 0) {
                    click(x + 1, y - 1);
                }

                click(x + 1, y);
                if (y < width - 1) {
                    click(x + 1, y + 1);
                }
            }
            return true;
    }

    public boolean click(int x, int y) {
        if (board[x][y] != TileState.T_NONE || board[x][y] == TileState.T_FLAG) {
            return true;
        }
        if (x == 4 && y == 12)
        {
            System.out.println(print());
        }
        int t = cal(x, y);
        if (t == TileState.T_MIME) {
            board[x][y] = TileState.T_MIME_CLICK;
//            openAll();
            openCount = -1;
            return false;
        }
        board[x][y] = t;
        openCount++;
        if (t == 0) {
            if (x > 0) {
                if (y > 0) {
                    click(x - 1, y - 1);
                }
                click(x - 1, y);
                if (y < width - 1) {
                    click(x - 1, y + 1);
                }
            }

            if (y > 0) {
                click(x, y - 1);
            }

            if (y < width - 1) {
                click(x, y + 1);
            }

            if (x < height - 1) {
                if (y > 0) {
                    click(x + 1, y - 1);
                }

                click(x + 1, y);
                if (y < width - 1) {
                    click(x + 1, y + 1);
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public static void main(String[] args) {

        Game game = create();
//        game.openAll();
        System.out.println(game.print());
        MSolverImpl solver = new MSolverImpl(game.width, game.height, game.mines);
//        clickOn(BoardHeight/2-1, BoardWidth/2-1);
        game.click(game.height / 2 - 1, game.width / 2 - 1);
        while (!game.gameOver()) {
            System.out.println(game.print());
            List<ClickResult> results = solver.solve(game.board);
            System.out.println("find results:" + results);
            if (results.size()==0) {
                System.out.println("游戏bug，记录游戏");
                break;
            }

            for (ClickResult click : results) {
                if (click.t == ClickResult.T_CLICK) {
                    game.click(click.x, click.y);
                } if (click.t == ClickResult.T_CHRON) {
                    game.chord(click.x,click.y);
                } if (click.t == ClickResult.T_FLAG) {
                    game.clickFlag(click.x,click.y);
                }
            }
        }
        System.out.println(game.print());
        System.out.println("GAME OVER!"+ (game.isWin()?"win":"failed"));

    }

    public static Game create() {
//        boolean[][] flags;
//        ObjectInputStream oin = null;
//        try {
//            oin = new ObjectInputStream(new FileInputStream(
//                  "C:\\Users\\zack\\Desktop\\abc.obj"));
//            flags = (boolean[][]) oin.readObject();
//            oin.close();
//            Game game = new Game(flags[0].length,flags.length,0);
//            for (int i = 0; i < game.height; i++) {
//                for (int j = 0; j < game.width; j++) {
//                    game.flags[i][j] = flags[i][j];
//                }
//            }
//            return game;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        return new Game(20, 10, 30);
    }
    private void save() {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("C:\\Users\\zack\\Desktop\\abc.obj"));
            outputStream.writeObject(this.flags);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
