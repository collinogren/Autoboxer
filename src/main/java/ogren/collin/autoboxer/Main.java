/*
    Autoboxer to make creating "boxes" for figure skating competitions easier.
    Copyright (C) 2024 Collin Ogren

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ogren.collin.autoboxer;

import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.pdf.FileType;
import ogren.collin.autoboxer.pdf.PDFManipulator;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.swing.*;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static double progress = 0.0;
    private static boolean isDone = false;

    public static void setProgress(double d) {
        progress = d;
    }

    public static void addProgress(double d) {
        progress += d;
    }

    public static void setDone(boolean b) {
        isDone = b;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        //test();
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fc.showOpenDialog(jframe);
        try {
            File file = fc.getSelectedFile();
            MasterController mc = new MasterController(file.getPath());
            showProgressScreen();
            mc.begin();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to generate the box.", "Error", JOptionPane.ERROR_MESSAGE);
            jframe.dispose();
            e.printStackTrace();
            return;
        }
        jframe.dispose();
        JOptionPane.showMessageDialog(null, "Successfully generated the box.", "Success", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private static void test() {
        JFileChooser fc = new JFileChooser();
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fc.showOpenDialog(jframe);
        try {
            File file = fc.getSelectedFile();
            PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.SIX0_PRIMARY_JUDGE_SHEET);
            PDDocument circledDocument = PDFManipulator.boxOfficial("Hope Wheeler", pdfManipulator.getDocument(), 1);
            circledDocument.save(new File(file.getPath().split(file.getName())[0]+"Circled.pdf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        jframe.dispose();
        System.exit(0);
    }

    private static void showProgressScreen() {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(() -> {
            JFrame progressFrame = new JFrame("Box Progress");
            JProgressBar progressBar = new JProgressBar();
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            progressFrame.add(progressBar);
            progressFrame.setSize(275, 125);
            progressFrame.setVisible(true);

            while (!isDone) {
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                progressBar.setValue((int) (progress * 100.0));
            }
        });

        executor.shutdown();
    }
}