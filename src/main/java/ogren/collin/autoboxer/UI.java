package ogren.collin.autoboxer;

import ogren.collin.autoboxer.control.MasterController;
import ogren.collin.autoboxer.pdf.FileType;
import ogren.collin.autoboxer.pdf.PDFManipulator;
import org.apache.pdfbox.pdmodel.PDDocument;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

    private static boolean generateSchedule = true;
    private static boolean generateStartingOrders = true;
    private static boolean generateTASheets = true;

    public static void begin() {
        icon = UI.class.getResource("/Autoboxer.png");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        //test();

        boxOptions();

    }

    private static void test() {
        JFileChooser fc = new JFileChooser();
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fc.showOpenDialog(jframe);
        try {
            File file = fc.getSelectedFile();
            PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.SIX0_PRIMARY_JUDGE_SHEET);
            System.out.println(pdfManipulator.parseToString(true));
            //PDDocument circledDocument = PDFManipulator.boxOfficial("Hope Wheeler", pdfManipulator.getDocument(), 1);
            //circledDocument.save(new File(file.getPath().split(file.getName())[0]+"Circled.pdf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        jframe.dispose();
        System.exit(0);
    }

    private static void boxOptions() {
        JFrame jframe = new JFrame();
        jframe.setTitle("Autoboxer");
        jframe.setSize(300, 150);
        jframe.setLocationRelativeTo(null);
        jframe.setResizable(false);
        jframe.setIconImage(new ImageIcon(icon).getImage());
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setLayout(new GridLayout(4, 1));
        JCheckBox generateSchedule = new JCheckBox("Generate Schedule Sheets");
        generateSchedule.setSelected(true);
        generateSchedule.addActionListener(e -> {
            setGenerateSchedule(generateSchedule.isSelected());
            System.out.println(getGenerateSchedule());
        });
        JCheckBox generateStartingOrders = new JCheckBox("Generate Starting Orders");
        generateStartingOrders.setSelected(true);
        generateStartingOrders.addActionListener(e -> {
            setGenerateStartingOrders(generateStartingOrders.isSelected());
            System.out.println(getGenerateStartingOrders());
        });
        JCheckBox generateTASheets = new JCheckBox("Generate TA Sheets");
        generateTASheets.setSelected(true);
        generateTASheets.addActionListener(e -> {
            setGenerateTASheets(generateTASheets.isSelected());
            System.out.println(getGenerateTASheets());
        });

        JPanel buttonPanel = getButtonPanel(jframe);
                panel.add(generateSchedule);
        panel.add(generateStartingOrders);
        panel.add(generateTASheets);
        panel.add(buttonPanel);
        jframe.add(panel);
        jframe.setVisible(true);
    }

    private static JPanel getButtonPanel(JFrame jframe) {
        JButton generateBox = new JButton("Generate Box");
        jframe.addWindowFocusListener(new WindowAdapter() {
            public void windowGainedFocus(WindowEvent e) {
                generateBox.requestFocusInWindow();
            }
        });

        generateBox.addActionListener(e -> {
            jframe.dispose();
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.execute(UI::generate);
        });

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(e -> {
            jframe.dispose();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        buttonPanel.add(cancel);
        buttonPanel.add(generateBox);
        return buttonPanel;
    }

    private static void generate() {
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

    public static void setGenerateSchedule(boolean b) {
        generateSchedule = b;
    }

    public static boolean getGenerateSchedule() {
        return generateSchedule;
    }

    public static void setGenerateStartingOrders(boolean b) {
        generateStartingOrders = b;
    }

    public static boolean getGenerateStartingOrders() {
        return generateStartingOrders;
    }

    public static void setGenerateTASheets(boolean b) {
        generateTASheets = b;
    }

    public static boolean getGenerateTASheets() {
        return generateTASheets;
    }

    private static void showProgressScreen() {
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(() -> {
            JFrame progressFrame = new JFrame("Box Progress");
            progressFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
