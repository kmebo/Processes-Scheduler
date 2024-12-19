# Processes-Scheduler
This project simulates scheduling several processes.

It is mandatory that the user have already set up the Java environment locally on the machine to run this project. Then, the use can type:
1. javac driver.javar
2. java driver X
   * where X could be input10.txt, . . ., input14.txt

Input
The program reads its input from .txt file which looks like (omitting the comments):
* BSIZE 4096  //  block size is 4,096 KB
* START    0  //  new process starts at t = 0 ms 
* CORE   200  // request 200 ms of CPU time
* READ   256  // read 256B from SSD 
* CORE   30   // request 30 ms of CPU time 
* DISPLAY 100 // write to display for 100 ms
. . .


The program processes several different operation including:
a. SSD Accesses which will take 0.1 milliseconds.

b. SSD Allocation: As the SSD can only process one request at  a  time, the program maintain a  single FCFS queue for processes waiting for the SSD.

c. SSD  Reads:  All  SSD  reads  are both  sequential  and buffered.  Whenever  a  process  issues  its  first  SSD  read request, the  kernel similutes bringing as many blocks in main 
memory as needed to satisfy the read request. So, if the read request  specified  128  bytes,  the  kernel  will  bring  BSIZE bytes into an in-memory I/O buffer memory.  The following reads will not require any further SSD access as long as they can satisfied with the data already in the I/O. As a result, the requesting process will immediately return to the end of the READY queue without any I/O delay.

d. SSD  Writes:  All  SSD  writes are blocking.  As  a  result, any  process  issuing  than  SSD  write  request  will  have  to access the SSD .

e. Memory Allocation: It is assumed that memory is large enough to contain all processes. 

f. CPU  Scheduling:  The program  maintains  a  single FCFS queue for all processes in the READY state.

g. Input and Display Access: It is assumed that each process runs in its own window so there will never be any queuing delay.


Output
Each time a process terminates, the program write to the Terminal/Command Prompt a short report with:
1. The total simulated time elapsed, 
2. The  number  of  read  operations  performed  by  the terminating process that required accessing the SSD, the number of those that did not, as well as the number of write operations performed by the same process. 
3. For each process in main memory and the process that has just terminated, one line with the sequence number of the process, and its current status (READY, RUNNING, BLOCKED, or TERMINATED)

For any question, please send me a LinkedIn message!

