# Autoboxer
This program is beta software. Always proof its outputs before using them.<br>
*Autoboxer* aims to make lengthy stretches of copying, highlighting, and sorting papers for the box a thing of the past.<br>
I would like to extend a massive thank you to this wonderful community. I hope you find this software useful and I appreciate any and all feedback.<br>
## Usage Instructions
1. Download and install the program from [releases](https://github.com/collinogren/Autoboxer/releases) or build from source.
2. Download an automatic PDF printer to dramatically save your time and sanity. I like this open-source one called [clawPDF](https://github.com/clawsoftware/clawPDF/releases). While I do not fully expect that using a different PDF printer will break the program, it could. So if you choose to use a different PDF printer and experience issues related to parsing the PDFs, please try clawPDF before reaching out for support since clawPDF is the only supported PDF printer.
3. Create a directory in which you will begin building your box.
4. Inside this directory place schedule.txt.
5. On the first line, type the day that the box is for (i.e. "Friday"). Then, on the next lines, copy all event numbers, start times, and end times in order from the 104 into schedule.txt. Copy all three columns at once.
6. Create directories as follows
   - 60
   - 60_sub
   - 60_starting_orders
   - coversheets
   - judges
   - tech
7. Use the automatic PDF printer to print IJSCompanion coversheets into 'coversheets,' IJSCompanion judges' sheets into 'judges,' ISUCalc technical panel sheets into 'tech,' Hal2 judges' sheets and worksheets (when used as the only sheet) into '60,' and Hal2 worksheets into '60_sub' when also using a judges' sheet.
8. Run *Autoboxer*, input the delimiter used to separate the event number from the event name,* and select the box directory which stores the other newly created directories. Wait until the program opens a window to alert you of its completion.
9. Print each judge's combined PDFs stored sub-directories in an automatically created directory called 'box.'
10. Proof the box to ensure no unforeseen mistakes occurred. You could do this virtually before the printing step if you like.
11. Do something else with all the time you just saved.

*A delimiter is a string of characters that is used to tell the computer where the separations between two or more sets of data are. An already common (and the default, preferred, and tested delimiter) is a space followed by a hyphen followed by a space (" - "). A good delimiter is a string that is not super common otherwise, though even a space should work, but be careful when naming combined events with a space as a delimiter.
Lastly, a delimiter also may not have any numbers or letters. To help visualize these rules, consider the examples below.<br>
### Favourable examples:
- "5 - Senior Women Short Program"
- "21 & 23 - Intermediate Women"
- "35_103 - Junior Men"
### Less favourable, but still completely functional examples using a space as a delimiter, notice the format for multiple event numbers is now limited:
- "5 Senior Women Short Program"
- "21_23 Intermediate Women"
### Examples that will result in a broken box:
- When using space as a delimiter: "21 & 23 Intermediate Women" results in only event number 21 being read and the event name being set to "& 23 Intermediate Women"
- When using space, hyphen, space (" - ") as a delimiter: "21 - 23 - Intermediate Women" results in only event number 21 being read with the event name being set to "23 - Intermediate Women"

To put it simply, never let your delimiter get mixed up with how you separate event numbers when the category contains two or more segments (and therefore event numbers).
Finally, **always be consistent**, you cannot use " - " as a delimiter most of the time and then expect an event with a space delimiter to be handled correctly.
### Competition Building Guidelines When Using Autoboxer
- **IMPORTANT make sure that officials’ names for Hal2 match those in ISUCalc** because otherwise you will get a separated output PDF containing IJS in one and 6.0 in the other. Best practice is to copy and paste from the official's directory into Hal2 so as to avoid any discrepancies.
- Generally, his program works largely by matching strings so some amount of consistency is required for optimal results. For example, while the program reads 34a and 34A as the same, it will not read 34a the same as 34 a.
- Finally, proof the generated "box" against the 104 to ensure no unforeseen issues arose.
## clawPDF Setup
After installing clawPDF, launch its utility application which you can do easily by searching clawPDF in the Windows search box. In the utility, go to 'profile settings,' then 'auto-save' check 'enable automatic saving' checkbox, then provide the path to any of the aforementioned directories depending on which sheet type you are going to print next into the target field.<br>
Keep the clawPDF settings utility open during the printing process because you are going to need to change output directories in the 'auto-save' tab as described in the 'usage instructions' section above.
Make sure you set clawPDF as your default printer so that Hal2 and IJSCompanion will use it and make sure that you select it, if it is not already, in ISUCalc when performing a batch print.
## TODO
- Multi-rink support for the schedule sheets. Multi-rink sorting works by sorting two separate boxes and then placing the relevant papers in order. Since different color paper is often used for multi-rink competitions, multi-rink sorting will likely never be implemented unless I hear that there are a lot of people who do not care about different color paper for different rinks.
- Lots of testing.
## Free and Open Source Software
This software is completely free and open source. Feel free to use, edit, and contribute to the project as much as you like. Remember that because this project uses the GNU GPL v3 license, any derivative works must also use the GNU GPL v3 license and therefore must also be free and open source software.
## Disclaimer
This is a tool designed to make your job easier but this is software with little testing so please make sure you proof the outputs of the program and remember it is your responsibility to ensure the accuracy of the paperwork. Even after this program becomes more stable and tested I still highly recommend always double checking to make sure everything is accurate. Regardless, I greatly appreciate feedback, so if something does not work or could be expanded upon, please, do not hesitate to let me know or to attempt to fix it yourself as this is 100% free and open source software.
## License
This program is licensed under the GNU General Public License v3.0.<br>
Copyright (C) 2024 Collin Ogren<br>
## See Also
https://www.youtube.com/watch?v=l3LFML_pxlY<br>
https://medium.com/@kennethbridgham/the-100-greatest-boxers-of-all-time-the-full-list-9729c182542<br>
https://www.uhaul.com/MovingSupplies/Boxes/<br>
https://en.wikipedia.org/wiki/Boxing_Day
