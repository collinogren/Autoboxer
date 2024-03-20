# Autoboxer
This program is experimental. Always proof its outputs before using them.<br>
Autoboxer aims to make lengthy stretches of highlighting and sorting papers for the box a thing of the past.<br>
I would like to extend massive thank you to this wonderful community. I hope you find this software useful and I appreciate any and all feedback.<br>
And to my friend and mentor John Marasco, for his many years of excellent guidance and highly enjoyable weekends, this one's for you.
## Usage Instructions
1. Download the program from releases or build from source.
2. Download an automatic PDF printer to dramatically save your time and sanity (I like this open-source one called clawPDF https://github.com/clawsoftware/clawPDF/releases).
3. Create a directory in which you will begin building your box.
4. Inside this directory place schedule.txt.
5. Copy all event numbers in order from the 104 into schedule.txt.
6. Create directories as follows
   - 60
   - 60_sub
   - coversheets
   - judges
   - tech
7. Use the automatic PDF printer to print IJSCompanion coversheets into 'coversheets', IJSCompanion judges' sheets into 'judges', ISUCalc technical panel sheets into 'tech', Hal2 judges' sheets and worksheets (when used as the only sheet) into '60', and Hal2 worksheets into '60_sub' when also using a judges' sheet.
8. Run 'Autoboxer.bat' and select the box directory which stores the other newly created directories.
9. Print each judge's combined PDFs stored sub-directories in an automatically created directory called box.
10. Proof the box to ensure no unforeseen mistakes occurred. You could do this virtually before the printing step if you like.
11. Do something else with all the time you just saved.
### Usage Guidelines
- Make sure that officials’ names for Hal2 match those in ISUCalc because otherwise you will get a separated output PDF containing IJS in one and 6.0 in the other. Best practice is to copy and paste from the official's directory into Hal2 so as to avoid any discrepancies.
- Generally, his program works largely by matching strings so some amount of consistency is required for optimal results. For example, while the program reads 34a and 34A as the same, it will not read 34a the same as 34 a.
- Finally, proof the generated "box" against the 104 to ensure no unforeseen issues arose.
## TODO
- [x] Read a file derived from the 104 to serve as a guide for sorting paperwork.
- [x] Assign event numbers and file types for PDF print outputs from ISUCalc and IJSCompanion.
- [x] Assign event numbers and file types for PDF print outputs from Hal2.
- [ ] Create a "box" out of processed paperwork which includes the following:
- [ ] Circle or otherwise mark each official on the coversheet of each set.
- [ ] Copy and sort files for every official.
## Free and Open Source Software
This software is completely free and open source. Feel free to use, edit, and contribute to the project as much as you like. Remember that because this project uses the GNU GPL v3 license, any derivative works must also use the GNU GPL v3 license and therefore must also be free and open source software.
## Disclaimer
This is a tool designed to make your job easier but this is alpha software with little testing so please make sure you proof the outputs of the program and remember it is your responsibility to ensure the accuracy of the paperwork. Even after this program becomes more stable and tested I still highly recommend always double checking to make sure everything is accurate. Regardless, I greatly appreciate feedback, so if something does not work or could be expanded upon, please, do not hesitate to let me know or to attempt to fix it yourself as this is 100% free and open source software.
## License
Autoboxer to make creating "boxes" for Figure Skating competitions easier.<br>
Copyright (C) 2024 Collin Ogren<br>

This program is free software -> you can redistribute it and/or modify<br>
it under the terms of the GNU General Public License as published by<br>
the Free Software Foundation, either version 3 of the License, or<br>
(at your option) any later version.<br>
<br>
This program is distributed in the hope that it will be useful,<br>
but WITHOUT ANY WARRANTY; without even the implied warranty of<br>
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the<br>
GNU General Public License for more details.<br>
<br>
You should have received a copy of the GNU General Public License<br>
along with this program.  If not, see <https ->//www.gnu.org/licenses/>.<br>
## See Also
https://www.youtube.com/watch?v=l3LFML_pxlY<br>
https://medium.com/@kennethbridgham/the-100-greatest-boxers-of-all-time-the-full-list-9729c182542<br>
https://www.uhaul.com/MovingSupplies/Boxes/<br>
https://en.wikipedia.org/wiki/Boxing_Day
