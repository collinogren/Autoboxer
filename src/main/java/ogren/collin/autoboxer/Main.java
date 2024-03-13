/*
    Autoboxer to make creating "boxes" for Figure Skating competitions easier.
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

// See also: https://www.gnu.org/fun/jokes/gospel.en.html

package ogren.collin.autoboxer;

import ogren.collin.autoboxer.pdf.FileType;
import ogren.collin.autoboxer.pdf.PDFManipulator;
import ogren.collin.autoboxer.process.Schedule;

import javax.swing.*;
import java.io.File;

public class Main {

    private static String competitionName = "2023 Pony Express Championships";

    public static String getCompetitionName() {
        return competitionName;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        JFileChooser fc = new JFileChooser();
        JFrame jframe = new JFrame();
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fc.showOpenDialog(jframe);
        JFileChooser fc2 = new JFileChooser();
        JFrame jframe2 = new JFrame();
        jframe2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fc.showOpenDialog(jframe2);
        try {
            File scheduleFile = fc.getSelectedFile();
            Schedule schedule = new Schedule(scheduleFile);
            File file = fc2.getSelectedFile();
            PDFManipulator pdfM = new PDFManipulator(file, FileType.IJS_TS2_SHEET, schedule);
            System.out.println(pdfM.getEventName());
        } catch (Exception e) {}

        jframe.dispose();
        System.exit(0);
    }
}