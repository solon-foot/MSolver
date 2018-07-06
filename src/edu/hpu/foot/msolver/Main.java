package edu.hpu.foot.msolver;

import edu.hpu.foot.msolver.scanner.impl.ArbiterScannerRobot;
import edu.hpu.foot.msolver.scanner.ScannerRobot;
import edu.hpu.foot.msolver.solver.ClickResult;
import edu.hpu.foot.msolver.solver.MSolver;
import edu.hpu.foot.msolver.solver.impl.MSolverImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Main extends JFrame {

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        super("自动扫雷demo");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JButton start = new JButton("开始");
        Checkbox checkbox = new Checkbox("NoFlag");
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                process(checkbox.getState());
            }
        });
        add(checkbox);
        add(start);

        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        pack();
        setVisible(true);
    }

    public void process( boolean noFlag) {
        ScannerRobot scannerRobot = new ArbiterScannerRobot();
        scannerRobot.start();
        MSolver solver = new MSolverImpl(scannerRobot.getGameWidth(), scannerRobot.getGameHeight(), scannerRobot.getMimes());
        solver.setNoFlag(noFlag);
        int[][]board = scannerRobot.scan();
        int times = scannerRobot.getGameHeight()*scannerRobot.getGameWidth()*2;
        while (!scannerRobot.isGameOver() && times>0) {

            List<ClickResult> results = solver.solve(board);
            System.out.println("find results:" + results);
            if (results.size()==0) {
//                System.out.println("游戏bug，记录游戏");
//                ((ArbiterScannerRobot)scannerRobot).saveImage();
                break;
            }
            int size = results.size();
            times -= size;
            for (int i = 0; i < size; i++) {
                ClickResult click = results.get(i);

                if (click.t == ClickResult.T_CLICK) {
                    scannerRobot.click(click.x, click.y);
                } if (click.t == ClickResult.T_CHRON) {
                    scannerRobot.chord(click.x,click.y);
                } if (click.t == ClickResult.T_FLAG) {
                    scannerRobot.flag(click.x,click.y);
                }
            }
            board = scannerRobot.scan();
        }
    }

}
