package edu.hpu.foot.msolver.scanner.impl;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;
import edu.hpu.foot.msolver.scanner.ScannerRobot;
import edu.hpu.foot.msolver.test.ImageScanner;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class ArbiterScannerRobot implements ScannerRobot {

    @Override
    public void start() {//开始
        if (!init()) {//如果初始化失败
            throw new RuntimeException("初始化失败");
        }
//        点击第一个位置
        moveMouse(reStartPoint.x, reStartPoint.y);

        robot.mousePress(InputEvent.BUTTON1_MASK);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        click(gameWidth / 2, gameHeight / 2);
    }

    BufferedImage temp = null;
    @Override
    public int[][] scan() {
        BufferedImage originImg = screenShotImage();
        boolean allOpend = true;
        for (int i = 0; i < gameHeight; i++) {
            for (int j = 0; j < gameWidth; j++) {
                if (board[i][j] == -1) {
                    allOpend = false;
                    board[i][j] = parseTile(j, i, originImg);
                }
            }
        }
        if (!gameOver&&allOpend) {
            win();
        }
        gameOver = gameOver || allOpend;
        temp = cropImage(originImg,boardStartPoint.x,boardStartPoint.y,
                boardStartPoint.x+gameWidth*boardWidth,boardStartPoint.y+gameHeight*boardWidth);
        return board;
    }
    private void win() {

    }
    private void lose() {
//        new ImageScanner(temp);
    }

    public void saveImage() {
        new ImageScanner(temp);
    }

    private void moveMouse(int mouseX, int mouseY) {
        //平滑的移动到点击的位置
        robot.mouseMove(mouseX, mouseY);
    }

    @Override
    public void click(int x, int y) {
        System.out.println("click: x:" + x + " y:" + y);
        Point p = getPoint(x, y);

        moveMouse(p.x, p.y);

        robot.mousePress(InputEvent.BUTTON1_MASK);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void flag(int x, int y) {
        Point p = getPoint(x, y);

        moveMouse(p.x, p.y);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        robot.mousePress(InputEvent.BUTTON3_MASK);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void chord(int x, int y) {
        Point p = getPoint(x, y);

        moveMouse(p.x, p.y);

        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mousePress(InputEvent.BUTTON3_MASK);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_MASK);
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getGameWidth() {
        return gameWidth;
    }

    @Override
    public int getGameHeight() {
        return gameHeight;
    }

    @Override
    public int getMimes() {
        return gameMimes;
    }

    @Override
    public boolean isGameOver() {
        return gameOver;
    }

    // https://github.com/java-native-access/jna
    private static class A<T> {
        private T t;

        private A(T t) {
            this.t = t;
        }
    }

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class);

        boolean EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, Pointer arg);

        boolean EnumChildWindows(WinDef.HWND hwnd, WinUser.WNDENUMPROC lpEnumFunc, int t);

        int GetWindowTextA(WinDef.HWND hWnd, byte[] lpString, int nMaxCount);

        WinDef.HWND GetLastActivePopup(WinDef.HWND hWnd);

        WinDef.HWND GetForegroundWindow();

        boolean SetForegroundWindow(WinDef.HWND hWnd);

        boolean GetClientRect(WinDef.HWND hWnd, WinDef.RECT lpRect);

        boolean GetWindowRect(WinDef.HWND hWnd, WinDef.RECT lpRect);

        WinDef.HWND GetTopWindow(WinDef.HWND hWnd);

        WinDef.HWND GetWindow(WinDef.HWND hWnd, WinDef.UINT nCmd);

        static WinDef.HWND findWindow(String s) {
            WinDef.HWND hWnd = null;
            User32 user32 = User32.INSTANCE;//Minesweeper Arbiter
            A<WinDef.HWND> a = new A<>(null);
            user32.EnumWindows(new WinUser.WNDENUMPROC() {
                int count = 0;

                @Override
                public boolean callback(WinDef.HWND thWnd, Pointer arg1) {
                    byte[] windowText = new byte[512];
                    user32.GetWindowTextA(thWnd, windowText, 512);
                    String wText = Native.toString(windowText);
                    if (wText.equals("Minesweeper Arbiter ")) {
                        System.out.println("find..............." + wText);
                        a.t = thWnd;

                        return false;
                    }

                    return true;
                }
            }, null);

            return a.t;
        }
    }


    public static void main(String[] args) {
        ArbiterScannerRobot arbiterScannerRobot = new ArbiterScannerRobot();
//        arbiterScannerRobot.start();
//        arbiterScannerRobot.show();
        arbiterScannerRobot.init();

    }


    private Robot robot;

    private Point reStartPoint;
    private Point boardStartPoint;
    private final static int boardWidth = 16;
    private int gameWidth, gameHeight;
    private int[][] board;
    private boolean gameOver = false;
    private boolean winGame = false;
    private int gameMimes = 999;

    public ArbiterScannerRobot() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public Point getPoint(int x, int y) {//获取点击点的位置
        return new Point(boardStartPoint.x + x * boardWidth + boardWidth / 2, boardStartPoint.y + y * boardWidth + boardWidth / 2);
    }

    public boolean init() {
        User32 user32 = User32.INSTANCE;//Minesweeper Arbiter
        WinDef.HWND hWnd = User32.findWindow("Minesweeper Arbiter ");
        if (hWnd == null) return false;
        user32.SetForegroundWindow(hWnd);
        final WinDef.RECT rect = new WinDef.RECT();
        user32.GetWindowRect(hWnd, rect);


        user32.EnumChildWindows(hWnd, new WinUser.WNDENUMPROC() {
                    int count = 0;
                    Color[] colors = new Color[]{Color.BLACK, Color.RED, Color.YELLOW, Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN, Color.PINK};

                    @Override
                    public boolean callback(WinDef.HWND thWnd, Pointer arg1) {
                        count++;
                        WinDef.RECT rect2 = new WinDef.RECT();
                        user32.GetWindowRect(thWnd, rect2);
//                        g2.drawRect(rect2.left - rect.left, rect2.top - rect.top, rect2.right - rect2.left, rect2.bottom - rect2.top); // 画一个矩形

                        if (count == 4) {
                            reStartPoint = new Point((rect2.right + rect2.left) / 2, (rect2.top + rect2.bottom) / 2);
                        } else if (count == 3) {
                            boardStartPoint = new Point(rect2.left + 3, rect2.top + 3);//神奇的偏移量
                            gameWidth = (rect2.right - rect2.left - 6) / boardWidth;
                            gameHeight = (rect2.bottom - rect2.top - 6) / boardWidth;
                            board = new int[gameHeight][gameWidth];
                            for (int i = 0; i < gameHeight; i++) {
                                for (int j = 0; j < gameWidth; j++) {
                                    board[i][j] = -1;
                                }
                            }

                        } else if (count==6) {
//                            数字框的识别
                            Rectangle captureSize = rect2.toRectangle();

                            BufferedImage bufferedImage = robot.createScreenCapture(captureSize);
                            gameMimes= parseNum(bufferedImage,new Rectangle(1,2,11,21))*100+
                                    parseNum(bufferedImage,new Rectangle(15,2,11,21))*10+
                                    parseNum(bufferedImage,new Rectangle(28,2,11,21));
                            System.out.println("mimes:"+gameMimes);

                        }

                        return true;
                    }
                }
                , 10);
        return board != null;
    }

    private static int parseNum(BufferedImage bufferedImage,Rectangle rectangle) {
        HashMap<Color,Integer> map = new HashMap<>();
        int value = 0;
        int x = rectangle.x;
        int y = rectangle.y;
        if (bufferedImage.getRGB(x+5,y+1)== 0xffff0000) value+=1;
        if (bufferedImage.getRGB(x+1,y+5)== 0xffff0000) value+=2;
        if (bufferedImage.getRGB(x+9,y+5)== 0xffff0000) value+=4;
        if (bufferedImage.getRGB(x+5,y+10)== 0xffff0000) value+=8;
        if (bufferedImage.getRGB(x+1,y+15)== 0xffff0000) value+=16;
        if (bufferedImage.getRGB(x+9,y+15)== 0xffff0000) value+=32;
        if (bufferedImage.getRGB(x+5,y+20)== 0xffff0000) value+=64;
        for (int i = 0; i < 10; i++) {
            if (parseNumDict[i]==value) return i;
        }
        return 999;
    }
    static int[] parseNumDict={119,36,93,109,46,107,123,37,127,111};

    static int[] colors = new int[]{
            new Color(192, 192, 192, 255).getRGB(),//empty
            new Color(0, 0, 255, 255).getRGB(),//1
            new Color(0, 128, 0, 255).getRGB(),//2
            new Color(255, 0, 0, 255).getRGB(),//3
            new Color(0, 0, 128, 255).getRGB(),//4
            new Color(128, 0, 0, 255).getRGB(),//5
            new Color(0, 128, 128, 255).getRGB(),//6
            new Color(0, 0, 0, 255).getRGB(),//7
            new Color(128, 128, 128, 255).getRGB(),//8

    };

    int parseTile(int x, int y, BufferedImage img) {
        int startX = boardStartPoint.x + boardWidth * x;
        int startY = boardStartPoint.y + boardWidth * y;
        if (img.getRGB(startX, startY) == Color.WHITE.getRGB()) {
            return -1;
        }
        if (img.getRGB(startX + 1, startY + 1) == colors[3]) {
            gameOver = true;
            lose();
            return -10;
        }
        int flag = 0;
        for (int j = startX + 1; j < startX + boardWidth; j++) {
            for (int i = startY + 1; i < startY + boardWidth; i++) {
                int col = img.getRGB(j, i);
                for (int k = 1; k < colors.length; k++) {
                    if (col == 0) {
                        return -10;
                    }
                    if (col == colors[k]) {
                        return k;
                    }
                }
                if (col == Color.BLACK.getRGB()) {
                    gameOver = true;
                    return -10;
                }
                if (col != colors[0]) {
                    System.out.println("unkonw:" + "x:" + x + " y:" + y + "color:" + new Color(col));
                    return -10;
                }
            }
        }
        return 0;
    }

    public void show() {
        BufferedImage originImg = screenShotImage();
        //绘制一个矩形
        Graphics2D g2 = (Graphics2D) originImg.getGraphics();
        g2.setColor(Color.WHITE);// 背景色
        g2.setStroke(new BasicStroke(2));


        for (int i = 0; i < gameHeight; i++) {
            for (int j = 0; j < gameWidth; j++) {
                if (board[i][j] == -1) {
                    board[i][j] = parseTile(j, i, originImg);
                    if (board[i][j] != -1) {
                        Point p = getPoint(j, i);
                        g2.drawOval(p.x, p.y, 2, 2);
                        g2.drawString(String.valueOf(board[i][j]), p.x, p.y);
                    }
                }
            }
        }
        g2.setColor(Color.GREEN);
        g2.drawOval(reStartPoint.x, reStartPoint.y, 2, 2);
        BufferedImage img = cropImage(originImg, boardStartPoint.x, boardStartPoint.y, boardStartPoint.x + boardWidth * gameWidth, boardStartPoint.y + boardWidth * gameHeight);
        new ImageScanner(img);
    }

    BufferedImage screenShotImage() {
        try {
            Rectangle captureSize = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage bufferedImage = robot.createScreenCapture(captureSize);
            return bufferedImage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedImage cropImage(BufferedImage bufferedImage, int startX, int startY, int endX, int endY) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        if (startX == -1) {
            startX = 0;
        }
        if (startY == -1) {
            startY = 0;
        }
        if (endX == -1) {
            endX = width - 1;
        }
        if (endY == -1) {
            endY = height - 1;
        }
        BufferedImage result = new BufferedImage(endX - startX, endY - startY, 4);
        for (int x = startX; x < endX; ++x) {
            for (int y = startY; y < endY; ++y) {
                int rgb = bufferedImage.getRGB(x, y);
                result.setRGB(x - startX, y - startY, rgb);
            }
        }
        return result;
    }
}
