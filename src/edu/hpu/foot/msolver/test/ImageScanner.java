package edu.hpu.foot.msolver.test;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class ImageScanner extends JFrame {

    public ImageScanner(BufferedImage img) {
        super("img");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel label = new JLabel();
        add(label);
        label.setIcon(new ImageIcon(img));
//        setSize(img.getWidth(),img.getHeight());
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }
}
