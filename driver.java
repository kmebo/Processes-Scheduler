import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.Buffer;

class ArgsLengthException extends Exception{
    public ArgsLengthException(String s){
        super(s);
    }
}
class ListOfProcesses {
    private Queue<String> ProcesData = new LinkedList<>();//this is the data of the process; all command to process.
    private int timer;
    private int buffer;
    private int physicalRead;
    private int inMemoryRead;
    private int physicalWrite;
    private String procStatus = "No Defined";;
    private String reportProcStatus = "yes";
    
    public ListOfProcesses(){
        this.ProcesData = null;
        this.timer = 0;
        this.buffer = 0;
        this.physicalRead = 0;
        this.inMemoryRead = 0;
        this.physicalWrite = 0;
        this.procStatus = "No Defined";
        this.reportProcStatus = "yes";
    }
    public ListOfProcesses(int timer){
        this.timer = timer;
    }
    //setter
    public void addToQueue(String s, int myNumParameter){ //e.i.: command number
        ProcesData.add(s+" "+String.valueOf(myNumParameter));
    }
    public void updateBuffer(int buffer){
        this.buffer = buffer;
    }
    public void updatePhysicalRead(){
        this.physicalRead += 1;
    }
    public void updateInMemoryRead(){
        this.inMemoryRead += 1;
    }
    public void updatePhysicalWrite(){
        this.physicalWrite += 1;
    }
    public void ProcStatusUpdate(String procStatus){
        this.procStatus = procStatus;
    }
    public void ReportProcStatusUpdate(String s){
        this.reportProcStatus = s;
    }
    // getters
    public String peek(){
        return ProcesData.peek();
    }
    public void pop(){
        this.ProcesData.poll();
    }
    public int getTimer(){
        return this.timer;
    }
    public int ProcessBuffer(){
        return this.buffer;
    }
    public String PhysicalRead(){
        return Integer.toString(this.physicalRead);
    }
    public String InMeroyRead(){
        return Integer.toString(this.inMemoryRead);
    }
    public String PhysicalWrite(){
        return Integer.toString(this.physicalWrite);
    }
    public String ProcStatus(){
        return this.procStatus;
    }
    public String ReportProcStatus(){
        return this.reportProcStatus;
    }
}
public class driver{
    public static void main(String[] args) {
        System.out.println("\nScheduler Project\n");
        try {
            int numOfFiles = argsLength(args.length);
            if (numOfFiles != 1){
                System.exit(0);
            }
            File myFile = new File(args[0]);
            Scanner fileReader = new Scanner(myFile);
            int numOfProcesses = myNumOfProcesses(fileReader);
            ListOfProcesses[] myProcesses = new ListOfProcesses[numOfProcesses];
            
            Scanner fileReader1 = new Scanner(myFile);
            populateEachProcess(fileReader1, myProcesses);

            LinkedList<String> MyScheduler = new LinkedList<String>();
            
            String s = "";
            for (int i = 0; i < myProcesses.length; i++) {
                s = String.valueOf(i)+ " start " + String.valueOf(myProcesses[i].getTimer()*1.0); 
                MyScheduler.add(s);
            }

            LinkedList<String> READY_QUEUEList = new LinkedList<String>();
            LinkedList<String> SSD_QUEUEList = new LinkedList<String>();
            String[] CPU_RUNNING = {"off"};
            String[] SSD_RUNNING = {"off"};

            if(MyScheduler.peek() != null){//>
                int MinTimeIndex = -1;
                while (MyScheduler.peek() != null) {
                    String schedTimeTwice =  schedTimeTwice(MyScheduler);// Core instr. has priority in Scheduler when time is duplicate. Is time duplicate?
                    if (schedTimeTwice.equals("true")){
                        // In what index is core; because that index is the MinTimeIndex now.
                        int coreTimeTwice = CoreIsInIndex(MyScheduler);
                        if (coreTimeTwice != -1){
                            MinTimeIndex = coreTimeTwice;
                        }
                        else{
                            System.out.println("A problem occurred: Exist a time that is duplucated in Schedule List but can find the core");
                        }
                    }else{
                        MinTimeIndex = FirstInstructionIndex(MyScheduler);//this index has priority
                    }

                    String s1 = MyScheduler.get(MinTimeIndex);// Index orients what index contains the instruction to execute next when the lowest time; hence it has the highest priority.
                    String[] s2 = s1.split(" ");// [#procID nameOfInst. time] 
                    String instructionName = s2[1].toLowerCase();
                    
                    if (instructionName.equals("start")){
                        MyStart(s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler);
                        MyScheduler.remove(MinTimeIndex);
                    }
                    else if(instructionName.equals("core")){
                        MyCPU(s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler, SSD_QUEUEList, SSD_RUNNING);
                        MyScheduler.remove(MinTimeIndex);
                    }
                    else if(instructionName.equals("ssd")){
                        mySSD(s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler, SSD_QUEUEList, SSD_RUNNING);
                        MyScheduler.remove(MinTimeIndex);
                    }
                    else if(instructionName.equals("i/o")){
                        myIO(s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler);
                        MyScheduler.remove(MinTimeIndex);
                    }
                    else{
                        System.out.println("Program only executes start, cpu, ssd, or i/o instructions.");
                        MyScheduler.clear();
                    }
                }
            }
            else{
                System.out.println("MyScheduler.size() is <= 0.");
            }

        }catch(Exception ArgsLengthException){
            System.err.println(ArgsLengthException);
        }
    }
    private static int CoreIsInIndex(LinkedList<String> MyScheduler){
        int ind = -1;
        for (int i = 0; i < MyScheduler.size(); i++) {
            String instSet = MyScheduler.get(i);
            String[] instSetArr = instSet.split(" ");
            if (instSetArr[1].toLowerCase().equals("core")){
                return i;
            }
        }
        return ind;
    }
    private static String schedTimeTwice(LinkedList<String> MyScheduler){
        String bool = "false";
        String instrSet = MyScheduler.getFirst();
        String[] instSetArr = instrSet.split(" ");
        double value = Double.parseDouble(instSetArr[2]);
        // loop start at 1;
        for (int i = 1; i < MyScheduler.size(); i++) {
            instrSet = MyScheduler.get(i);
            instSetArr = instrSet.split(" ");
            if (Double.parseDouble(instSetArr[2]) == value){
                return "true";
            }
        }
        return bool;
    }
    private static void mySSD(String[] s2, ListOfProcesses[] myProcesses, LinkedList<String> READY_QUEUEList, String[] CPU_RUNNING, LinkedList<String> MyScheduler, LinkedList<String> SSD_QUEUEList, String[] SSD_RUNNING){
        String scheProcId = s2[0];
        double scheProcTime = Double.parseDouble(s2[2]);
        String nextProcInst = myProcesses[Integer.parseInt(scheProcId)].peek();
        String[] nextProcInstArr = nextProcInst.split(" "); // = [Instr.Name value]
        String nextProcInstName = nextProcInstArr[0].toLowerCase();
        String nextProcInstValue = nextProcInstArr[1];

        if ( nextProcInst != null){
            if (nextProcInstName.equals("core")){
                RQandCPUstatus(s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler);
                myProcesses[Integer.parseInt(scheProcId)].pop();
            }
            else{
                System.out.println("msg. from mySSD: "+ nextProcInstName + " (not a core).");
            }
        }
        else{
            System.out.println("Process "+ scheProcId + " is empty.");
        }
    }
    private static void myIO(String[] s2, ListOfProcesses[] myProcesses, LinkedList<String> READY_QUEUEList, String[] CPU_RUNNING, LinkedList<String> MyScheduler){
        String processIDSchedule = s2[0];
        double processTimeSchedule = Double.parseDouble(s2[2])*1.0;
    // What's the status of the process that gave the CPU? Is it empty or has another instruction?
        if (myProcesses[Integer.parseInt(processIDSchedule)].peek() != null){ // It means has at least one instr.
            String procNextInst = myProcesses[Integer.parseInt(processIDSchedule)].peek(); // "instName value"
            String[] splitProcNextInst = procNextInst.split(" ");
            String splitNextInstName = splitProcNextInst[0];
            String splitNextInstValue = splitProcNextInst[1];
            
            if (splitNextInstName.equals("core")){
                RQandCPUstatus(s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler);
                myProcesses[Integer.parseInt(processIDSchedule)].pop(); // remove that instrution from Proc. List.
            }
            else{
                System.out.println("msg. from myIO/splitNextInstName.equals(\"core\"). Will the IO request another inst. rather than core?");
            }
        }
        else{
            System.out.println("from CPU: "+myProcesses[Integer.parseInt(s2[0])]+ " does not have more instr.");
        }
    }
    private static void RQandCPUstatus(String[] s2, ListOfProcesses[] myProcesses, LinkedList<String> READY_QUEUEList, String[] CPU_RUNNING, LinkedList<String> MyScheduler){
        String processIDSchedule = s2[0];
        double processTimeSchedule = Double.parseDouble(s2[2])*1.0;

        String procNextInst = myProcesses[Integer.parseInt(processIDSchedule)].peek(); // "instName value"
        String[] splitProcNextInst = procNextInst.split(" ");
        double splitNextInstValue = Double.parseDouble(splitProcNextInst[1])*1.0;

        if (READY_QUEUEList.size() == 0){ // RQ is empty 
            if (CPU_RUNNING[0].equals("on")){ // Whichever process must wait and stay in the RQ.
                // add this Process's instr. to the RQ.
                READY_QUEUEList.add(processIDSchedule +" core "+ Double.toString(splitNextInstValue));
                //not update CPU_RUNNING because is runing.
                myProcesses[Integer.parseInt(s2[0])].ProcStatusUpdate("READY");
            }
            else if(CPU_RUNNING[0].equals("off")){ // Process requesting the CPU gets it.
                //update CPU_RUNNING because it got busy
                CPU_RUNNING[0] = "on";
                double x = processTimeSchedule + splitNextInstValue;
                MyScheduler.add(processIDSchedule +" core "+ Double.toString(x));
                //READY_QUEUE is not updated here
            }
            else{
                System.out.println("msg. from RQandCPUstatus(): CPU_RUNNING is not on/off.");
            }
        }
        else{ //It means RQ contains @ least 1 process's instr. Hence, CPU is on.
            // add this Process's instr. to the RQ.
            READY_QUEUEList.add(processIDSchedule +" core "+ Double.toString(splitNextInstValue));
            //not update CPU_RUNNING because is runing.
            myProcesses[Integer.parseInt(s2[0])].ProcStatusUpdate("READY");
        }
    }
    private static void MyCPU(String[] s2, ListOfProcesses[] myProcesses, LinkedList<String> READY_QUEUEList, String[] CPU_RUNNING, LinkedList<String> MyScheduler, LinkedList<String> SSD_QUEUEList, String[] SDD_RUNNING){
    // Check the status of READY_QUEUE and CPU_RUNNING  
        if (READY_QUEUEList.size() > 0){
            String sQueue = READY_QUEUEList.poll(); // peek and remove value String from RQ.
            String[] s1Queue = sQueue.split(" "); // [#procID instName time]

            myProcesses[Integer.parseInt(s1Queue[0])].ProcStatusUpdate("RUNNING");
            CPU_RUNNING[0] = "on"; // Update that the CPU is running a process's instr.

        // Since CPU was giving from one core to another, next action is to schedule that CPU completion.
            double x = Double.parseDouble(s2[2]) + Double.parseDouble(s1Queue[2]);

        // Adding new future core completion for a process to schedule list.
            MyScheduler.add(s1Queue[0] + " core "+ Double.toString(x));

        // What's the status of the process that gave the CPU? Is it empty or has another instruction?
            if (myProcesses[Integer.parseInt(s2[0])].peek() != null){ // It means has at least one instr.
                // Important: the string comes in this formatt "instrName Time"
                String sProc = myProcesses[Integer.parseInt(s2[0])].peek();
                myProcesses[Integer.parseInt(s2[0])].pop(); // remove that inst. from Proc. List.
                String[] s1Proc = sProc.split(" "); // [instrName Time]
                String ProcInstName = s1Proc[0].toLowerCase();
                double ProcInstTime = Double.parseDouble(s1Proc[1])*1.0;
                
                if (ProcInstName.equals("display") || ProcInstName.equals("input") ){ // Assuming that display/input are a category of I/O request.
                    double ioRequestTime = Double.parseDouble(s2[2])*1.0 + ProcInstTime;
                // Scheduling the i/o request, and adding it to the Schedule list.
                    MyScheduler.add(s2[0] + " i/o " + Double.toString(ioRequestTime));
                }
                else if(ProcInstName.equals("core")){
                    System.out.println("-- Process "+ s2[0] +" request the CPU at time "+ Double.parseDouble(s2[2])*1.0 +" ms for "+ ProcInstTime +" ms.");
                }
                else if(ProcInstName.equals("read") || ProcInstName.equals("write")){// Assuming that read/write are a category of SSD request.
                    myCPUhelper(s1Proc, s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler, SSD_QUEUEList, SDD_RUNNING);
                }
                else if(ProcInstName.equals("start")){
                    System.out.println("Is it possible to have an istruction having as its next instr. a start?");
                }
                else{
                    System.out.println("Am I missing a possible instruction name to execute?");
                }
            }
            else{// A process does not have more instructions; then, print its table.
                myProcesses[Integer.parseInt(s2[0])].ProcStatusUpdate("TERMINATED");
                TableMsg(s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler, SSD_QUEUEList,SDD_RUNNING);
            }
        }
        else{// NO process's instr. is waiting for the CPU. (A)
        // What's the status of the process that gave the CPU? Is it empty or has another instruction?
            CPU_RUNNING[0] = "off"; // means that (A) so must be set to "off".

            if (myProcesses[Integer.parseInt(s2[0])].peek() != null){ // It means has at least one more instructions.
                // Important: the string comes in this formatt "instrName Time"
                String sProc = myProcesses[Integer.parseInt(s2[0])].peek();
                myProcesses[Integer.parseInt(s2[0])].pop(); // remove that inst. from Proc. List.

                String[] s1Proc = sProc.split(" "); // [instrName Time]
                String ProcInstName = s1Proc[0].toLowerCase();
                double ProcInstTime = Double.parseDouble(s1Proc[1])*1.0;

                if (ProcInstName.equals("display") || ProcInstName.equals("input") ){ // Assuming that display/input are a category of I/O request.
                    double ioRequestTime = Double.parseDouble(s2[2])*1.0 + ProcInstTime;
                // Scheduling the i/o request, and adding it to the Schedule list.
                    MyScheduler.add(s2[0] + " i/o " + Double.toString(ioRequestTime));
                }
                else if(ProcInstName.equals("core")){
                    System.out.println("-- Process "+ s2[0] +" request the CPU at time "+ Double.parseDouble(s2[2])*1.0 +" ms for "+ ProcInstTime +" ms. what to do now?");
                }
                else if(ProcInstName.equals("read") || ProcInstName.equals("write")){// Assuming that read/write are a category of SSD request.
                    myCPUhelper(s1Proc, s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler, SSD_QUEUEList, SDD_RUNNING);
                }
                else if(ProcInstName.equals("start")){
                    System.out.println("Is it possible to have an istruction having as its next instr. a start?");
                }
                else{
                    System.out.println("Am I missing a possible instruction name to execute?");
                }
            }
            else{// A process does not have more instructions; then, print its table.
                myProcesses[Integer.parseInt(s2[0])].ProcStatusUpdate("TERMINATED");
                TableMsg(s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler, SSD_QUEUEList,SDD_RUNNING);
            }
        }
    }
    private static void TableMsg(String[] s2, ListOfProcesses[] myProcesses, LinkedList<String> READY_QUEUEList, String[] CPU_RUNNING, LinkedList<String> MyScheduler, LinkedList<String> SSD_QUEUEList, String[] SDD_RUNNING){
        String procId = s2[0];
        String scheProcTime = s2[2];
        String s = "\nProcess "+ procId +" terminates at time "+(Math.round(Double.parseDouble(scheProcTime) * 10.0) / 10.0 )+ " ms.\nit performed "+ myProcesses[Integer.parseInt(procId)].PhysicalRead() +" physical read(s), "+ myProcesses[Integer.parseInt(procId)].InMeroyRead() +" in-memory read(s), and "+ myProcesses[Integer.parseInt(procId)].PhysicalWrite() +" physical write(s).\nProcess Table:\n";

        for (int i = 0; i < myProcesses.length; i++) {
            // if you are the process finishin/TERMINATED, then report status
            // if you are not the process doing the call, then if your status is different than TERMINATED then print status
            if (!myProcesses[i].ProcStatus().equals("TERMINATED") || i == Integer.parseInt(procId)) {
                s += "Process "+ Integer.toString(i) + " is " + myProcesses[i].ProcStatus() +".\n";
            }
        }
        System.out.print(s);
    }
    
    private static void myCPUhelper(String[] s1Proc,String[] s2, ListOfProcesses[] myProcesses, LinkedList<String> READY_QUEUEList, String[] CPU_RUNNING, LinkedList<String> MyScheduler, LinkedList<String> SSD_QUEUEList, String[] SDD_RUNNING){
        // from myProcesses
        String ProcInstName = s1Proc[0].toLowerCase(); 
        int ProcInstValue = Integer.parseInt(s1Proc[1]);
         // from MyScheduler
        String schedProcessId = s2[0];
        double schedProcessTime = Double.parseDouble(s2[2])*1.0;
        
        if (ProcInstName.equals("read")){
            int ProcessBufferSize = myProcesses[Integer.parseInt(schedProcessId)].ProcessBuffer();
            if(ProcessBufferSize == 0){
                ProcessBufferSize = 4096 - ProcInstValue;
                //update that this processs request one physical read(s)
                myProcesses[Integer.parseInt(schedProcessId)].updatePhysicalRead();
                myProcesses[Integer.parseInt(schedProcessId)].updateBuffer(ProcessBufferSize);
                SSDQueueStatus(s1Proc, s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler, SSD_QUEUEList, SDD_RUNNING);
            }
            else if (ProcessBufferSize > ProcInstValue){
                int unreadBytes = ProcessBufferSize - ProcInstValue;
                // Update process buffer
                myProcesses[Integer.parseInt(schedProcessId)].updateBuffer(unreadBytes);
                // Update process in-memory read(s)
                myProcesses[Integer.parseInt(schedProcessId)].updateInMemoryRead();

             // What's Process next instruction?
                String procNextInstr = myProcesses[Integer.parseInt(schedProcessId)].peek();
                String[] procNextInstrArr = procNextInstr.split(" ");

                // check RQ & CPU_RUNNING status
                RQandCPUstatus(s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler);
                myProcesses[Integer.parseInt(schedProcessId)].pop();
            }
            else if (ProcessBufferSize < ProcInstValue){
                int bytesNeeded = ProcInstValue - ProcessBufferSize; // this not clear yet
                int unreadBytes = 4096 - bytesNeeded;
                // update process buffer
                myProcesses[Integer.parseInt(schedProcessId)].updateBuffer(unreadBytes);
                // update physical read(s)
                myProcesses[Integer.parseInt(schedProcessId)].updatePhysicalRead();
                SSDQueueStatus(s1Proc, s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler, SSD_QUEUEList, SDD_RUNNING);
            }
            else{
                System.out.println("Unhandle ProcessBufferSize");
            }
        }
        else if (ProcInstName.equals("write")){
            SSDQueueStatus(s1Proc, s2, myProcesses, READY_QUEUEList, CPU_RUNNING, MyScheduler, SSD_QUEUEList, SDD_RUNNING);
            myProcesses[Integer.parseInt(schedProcessId)].updatePhysicalWrite();
        }
        else{
            System.out.println("from myCPUhelper: instr. is not read/write.");
        }
    }
    private static void SSDQueueStatus(String[] s1Proc,String[] s2, ListOfProcesses[] myProcesses, LinkedList<String> READY_QUEUEList, String[] CPU_RUNNING, LinkedList<String> MyScheduler, LinkedList<String> SSD_QUEUEList, String[] SDD_RUNNING){
        String schedProcessId = s2[0];
        if (SSD_QUEUEList.size() == 0 && (SDD_RUNNING[0].equals("off") )){
            double x = Double.parseDouble(s2[2])*1.0 + 0.1;
            MyScheduler.add(schedProcessId + " ssd " + Double.toString(x));
        }
        else{
            System.out.println("-- Process "+  schedProcessId +" waits for SSD. Should it be stored in SSD_Queue_List?");
        }
    }
    private static void MyStart(String[] s2, ListOfProcesses[] myProcesses, LinkedList<String> READY_QUEUEList, String[] CPU_RUNNING, LinkedList<String> MyScheduler){
        // what's Proc. next instruction if it has one.
        if (myProcesses[Integer.parseInt(s2[0])] != null){
            String instAndValue = myProcesses[Integer.parseInt(s2[0])].peek();// when will I take this item out of the list of this process?
            myProcesses[Integer.parseInt(s2[0])].pop(); // remove that instruction for process list

            String[] instAndValueArr = instAndValue.split(" ");// [inst.Name Value] e.i.: [core 100]
            if (instAndValueArr[0].toLowerCase().equals("core")){
            // Check the status of READY_QUEUE and CPU_RUNNING
                if (READY_QUEUEList.size() == 0){ // it means queue is empty
                    if(CPU_RUNNING[0].equals("on")){
                        // update READY_QUEUE and CPU_RUNNING
                    // CPU_RUNNING is not update here becuase it's running an instr.
                        READY_QUEUEList.add(s2[0]+ " core "+ instAndValueArr[1]);
                        myProcesses[Integer.parseInt(s2[0])].ProcStatusUpdate("READY");
                    }
                    else if (CPU_RUNNING[0].equals("off")) { // means CPU is not running an inst.
                        //update CPU_RUNNING because it got busy
                        CPU_RUNNING[0] = "on";
                        double x = Double.parseDouble(s2[2])*1.0 + Integer.parseInt(instAndValueArr[1])*1.0;
                        MyScheduler.add(s2[0] +" core "+ Double.toString(x));
                        //READY_QUEUE is not updated here
                    }
                    else{
                        System.out.println("msg. from MyStart(): CPU_RUNNING is not on/off. Inspects problem.");
                    }
                }
                else{ // it means that RQ has an element and CPU_Running is on
                    // update READY_QUEUE and CPU_RUNNING
                    // CPU_RUNNING is not update here becuase it's running an instr.
                    READY_QUEUEList.add(s2[0]+ " core "+ instAndValueArr[1]);
                    myProcesses[Integer.parseInt(s2[0])].ProcStatusUpdate("READY");
                }
            }
            else{
                System.out.println("I got an instruction that is not core; what to do?");
            }
        }
        else{
            System.out.println("from MyStart(): myProcesses.length is <= 0 (for debug purpose).");
        }

    }
    private static int FirstInstructionIndex(LinkedList<String> MyScheduler){
        int minTimerInd = 0;
        String s = MyScheduler.get(minTimerInd);
        String[] s1 = s.split(" ");
        double minTimer = Double.parseDouble(s1[2])*1.0;
        
        for (int i = 1; i < MyScheduler.size(); i++){
            s = MyScheduler.get(i);
            s1 = s.split(" ");
            double x = Double.parseDouble(s1[2]); 
            if (minTimer > x){
                minTimer = x;
                minTimerInd = i;
            }
        }
        return minTimerInd;
    }
    private static void populateEachProcess(Scanner fileReader, ListOfProcesses[] myProcesses){
        int i = -1;
        while (fileReader.hasNextLine()) {
            String str = fileReader.nextLine().toLowerCase();
            String[] strSplit = str.split(" ", 2);
            String s = strSplit[0];
            int myNumParameter = myParameter(strSplit[1]);
            if(s.equals("start")){//creates a new process save its time
                i += 1;
                myProcesses[i] = new ListOfProcesses(myNumParameter);
            }
            else{//in case is not a start command, then send it to its queue 
                if (i > -1){
                    myProcesses[i].addToQueue(s, myNumParameter);
                }
            }
        }
        fileReader.close();
    }
    /*This method makes sure that the numbers from the right of each 
     * line in the file. First, it is a number. Second, it doesn't contain
     * a space or any other character.
     * The exception is not handle here because I am assuming only numbers.
     * returns an int
     * receive as parameter a String
    */
    private static int myParameter(String strSplit){
        String s = "";
        for (int i = 0; i < strSplit.length(); i++) {
            if(strSplit.charAt(i) != ' ' && 
                Character.isDigit(strSplit.charAt(i))){
                    s += strSplit.charAt(i);
            }
        }
        return Integer.parseInt(s);
    }
    /*This function returns the numbers of processes in the file provided.
     * parameter needed a Scanner file.
     */
    private static int myNumOfProcesses(Scanner fileReader){
        int countProcesses = 0;
        while (fileReader.hasNextLine()) {
            String s = fileReader.nextLine().toLowerCase();
            if (s.length() > 3){
                //START
                if (s.charAt(0)=='s' && s.charAt(1)=='t' && 
                    s.charAt(2)=='a' && s.charAt(3)=='r' && 
                    s.charAt(4)=='t'){
                        countProcesses += 1;
                }

            }
        }
        fileReader.close();
        return countProcesses;
    }
    private static int argsLength(int args) throws ArgsLengthException{
        if (args == 0){
            throw new ArgsLengthException("User has not provide a file file.");
        }
        else if(args > 1){
            throw new ArgsLengthException("User has not provide too many files.");
        }

        else{
            return args;
        }
    }
}