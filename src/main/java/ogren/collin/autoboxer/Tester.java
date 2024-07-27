package ogren.collin.autoboxer;

import ogren.collin.autoboxer.pdf.FileType;
import ogren.collin.autoboxer.pdf.PDFManipulator;

import javax.swing.*;
import java.io.File;

public class Tester {

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        JFileChooser fc = new JFileChooser();
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fc.showOpenDialog(jframe);
        try {
            File file = fc.getSelectedFile();
            PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.IJS_JUDGE_SHEET);
            //PDDocument circledDocument = PDFManipulator.boxOfficial("Hope Wheeler", pdfManipulator.getDocument(), 1);
            //circledDocument.save(new File(file.getPath().split(file.getName())[0]+"Circled.pdf"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        jframe.dispose();
        System.exit(0);
    }
}
