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

import ogren.collin.autoboxer.pdf.FileType;
import ogren.collin.autoboxer.pdf.PDFManipulator;
import ogren.collin.autoboxer.utilities.remote_utilities.auto_update.RemoteAutoUpdateTextBundle;
import ogren.collin.autoboxer.utilities.remote_utilities.auto_update.RemoteTextParser;

import javax.swing.*;
import java.io.File;

public class Tester {

    public static void main(String[] args) {
        test();
    }

    private static void test() {
        RemoteAutoUpdateTextBundle e = RemoteTextParser.getRemoteText();
        System.out.println(e.version());
        System.out.println(e.numericVersion());
        System.out.println(e.url());
        JFileChooser fc = new JFileChooser();
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fc.showOpenDialog(jframe);
        try {
            File file = fc.getSelectedFile();
            PDFManipulator pdfManipulator = new PDFManipulator(file, FileType.IJS_JUDGE_SHEET);
            System.out.println(pdfManipulator.parseToString(false));

            //PDDocument circledDocument = PDFManipulator.boxOfficial("Hope Wheeler", pdfManipulator.getDocument(), 1);
            //circledDocument.save(new File(file.getPath().split(file.getName())[0]+"Circled.pdf"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        jframe.dispose();
        System.exit(0);
    }
}
