package ogren.collin.autoboxer;

import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.pdf.FileType;
import ogren.collin.autoboxer.pdf.PDFManipulator;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UI {


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

    private static URL icon;

    public static void begin() {
        icon = UI.class.getResource("/Autoboxer.png");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        //test();
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        JFrame jframe = new JFrame();
        jframe.setIconImage(new ImageIcon(icon).getImage());
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
            progressFrame.setIconImage(new ImageIcon(icon).getImage());
            JProgressBar progressBar = new JProgressBar();
            progressBar.setValue(0);
            progressBar.setStringPainted(true);
            progressFrame.add(progressBar);
            progressFrame.setSize(275, 125);
            progressFrame.setLocationRelativeTo(null);
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
