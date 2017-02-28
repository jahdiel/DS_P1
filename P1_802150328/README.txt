README
CIIC 4020 - Data Structures
============================================
------------------
Project Author
------------------
+ Jahdiel Alvarez
+ 802-15-0328
+ Inst. Kelvin Roche

==============================================
------------------
Project Description
------------------
This programming project works with virtual disks systems, 
with the eventuality of using it in a system that will implement a simple virtual file system. 
In this phase, it implements the basis of the system, which is the virtual disk in which data is stored in blocks. 

==============================================
------------------
Program Details:
------------------
+ The project is based on implementing a DiskUnit based on RandomAccessFiles and 
  arrays of bytes as the virtual representation of the data in memory.
+ The project consists of three packages:
	* testers -> which include the scripts for testing the DiskUnit and VirtualDiskBlock classes.
	* diskUtilities -> which contains the DiskUnit, VirtualDiskBlock and DiskUnitInterface java files.
	* diskUnitExceptions -> which contains the necessary exceptions for the DiskUnit and VirtualDiskBlock classes.
+ The classes are based on reading and writing information on to  the RandomAccessFiles simulating a disk system.

===============================================
------------------------------
Instructions for Eclipse:
------------------------------
+ First unzip the file: P1_4035_802150328_162.zip
+ Open the project in Eclipse.
+ Make sure the Eclipse encoding is set to UTF-8.	
+ Run DiskUnitTester0.java in order to create the RandomAccessFiles.
  - The step above will not work if the files already exists.
+ Run DiskUnitTester1.java (the RandomAccessFiles are need to exist). 
  
================================================
-------------------------------
Instructions for Terminal 
-------------------------------
+ Open your terminal or command prompt.
+ Enter the file in which the project is stored.
+ On Windows use the dir command and MacOS or Linux use ls command to verify you are in the correct file.
+ Once inside the project file, compile the java files:
	>>>javac -d src -sourcepath src src/testers/DiskUnitTester0.java
or 
	>>>javac -d src -sourcepath src src/testers/DiskUnitTester1.java
	
+ Run the java class files using one of either commands, depending on the situation:
	>>>java -classpath src testers.DiskUnitTester0
or
	>>>java -classpath src testers.DiskUnitTester1
