# Autoboxer
This program is beta software. Always proof its outputs before using them.<br>
*Autoboxer* aims to make lengthy stretches of copying, highlighting, and sorting papers for the box a thing of the past.<br>
I would like to extend a massive thank you to this wonderful community. I hope you find this software useful and I appreciate any and all feedback.<br>
## Usage Instructions
1. Download and install the program from [releases](https://github.com/collinogren/Autoboxer/releases) or build from source.
2. Download and install [clawPDF](https://github.com/clawsoftware/clawPDF/releases). No other PDF printer is supported.
3. Create a directory to build the box.
4. Launch Autoboxer and use the browse button to select the box directory that was created.
5. Press the plus (+) button next to 'control panel' near the top of the screen to create a new rink schedule, use the top, single line text field to input the name of the rink you are inputting the schedule for.
6. Open the 104 in a spreadsheet editor and hide other columns as necessary so that event number, start time, and end time columns are next to each other (you may have to copy start and end times up or down to fill in empty spaces for events that warm-up together depending on how the referee has chosen to format the 104).
7. Highlight and copy all the values from the three columns for the desired rink and paste them into the larger text area in the rink schedule editor in Autoboxer. Repeat steps 5, 6 (as needed), and 7 (this) for every rink at the competition. Use CTRL+S or 'file->save' to save your work.
8. On the 'clawPDF print directory' panel there are buttons to set clawPDF to print to the correct location. Use as follows:
- ### Coversheets
  + Press 'IJS Coversheets' in Autoboxer then print from IJScompanion.
- ### IJS Judges' Sheets
  + Press 'IJS Judge' in Autoboxer then print from IJScompanion.
- ### Technical Panel Sheets
  + Press 'IJS Tech' in Autoboxer then batch print from ISUCalc.
- ### 6.0 Sheets
  + Press '6.0' and print judges' sheets and worksheets. **Not** worksheets that go underneeth a judges' sheet such as a generic free skating worksheet, that comes next.
- ### 6.0 Generic Free Skating Worksheets
  + Press '6.0 sub' and print worksheets for any events that require a generic free skating worksheet.
- ### 6.0 Starting Orders
  + Press '6.0 Starting Orders and print all 6.0 starting orders for the day.
9. Ensure the correct number delimiter* is set in Autoboxer using the corresponding text field, uncheck any unwanted settings, and press 'generate.'
10. Digitally proof the box or alternatively print then proof on paper. Using this software lends itself well to digital proofing which saves paper and frustration.
11. Print each judge's combined PDFs sorted by rink in an automatically created directory called 'box.' Do the same for starting orders and TA sheets if generated.
12. Do something else with all the time you just saved.
    
*A delimiter is a string of characters that is used to tell the computer where the separations between two or more sets of data are. An already common (and the default, preferred, and tested delimiter for *Autoboxer*) is a space followed by a hyphen followed by a space (" - "). A good delimiter is a string that is not super common otherwise. However, even a space should work but be careful when naming combined events with a space as a delimiter.
Lastly, it may be risky for a delimiter to have numbers or letters. To help visualize these rules, consider the examples below.<br>
### Favourable examples:
- "5 - Senior Women Short Program"
- "21 & 23 - Intermediate Women"
- "35_103 - Junior Men"
### Less favourable, but still completely functional examples using a space as a delimiter, notice the format for multiple event numbers is now more limited:
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
After installing clawPDF, launch its utility application which you can do easily by searching clawPDF in the Windows search box. In the utility, go to 'profile settings,' then 'auto-save' check 'enable automatic saving' checkbox.
Next, go to 'save' then uncheck 'open with standard viewer after conversion.'<br>
Make sure you set clawPDF as your default printer so that Hal2 and IJSCompanion will use it and make sure that you select it, if it is not already, in ISUCalc when performing a batch print.<br>
Use the buttons in Autoboxer to direct clawPDF to print to the necessary directories.
## TODO
- Make schedule sheets more obvious for accounting purposes.
## Disclaimer
It is your responsibility to ensure the accuracy of your paperwork. Even with this software, proof the paperwork before sending it out.
## Free and Open Source Software
This software is completely free and open source. Feel free to use, edit, and contribute to the project as much as you like. Remember that because this project uses the GNU GPL v3.0 license, any derivative works must also use the GNU GPL v3.0 license and therefore must also be free and open source software. Any redistribution of this software must also provide access to the source code. 
## License
This program is licensed under the GNU General Public License v3.0.<br>
Copyright (C) 2024 Collin Ogren<br>
## See Also
https://www.youtube.com/watch?v=l3LFML_pxlY<br>
https://medium.com/@kennethbridgham/the-100-greatest-boxers-of-all-time-the-full-list-9729c182542<br>
https://www.uhaul.com/MovingSupplies/Boxes/<br>
https://en.wikipedia.org/wiki/Boxing_Day
