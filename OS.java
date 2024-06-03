import java.util.ArrayList;

public class OS {

    private static Kernel kernel = new Kernel();
    public static CallType currentCall;
    private static ArrayList<Object> parameters = new ArrayList<>();
    private static Object returnValue; // maybe should not be 1, just debugging



    public enum CallType {
        CREATE_PROCESS,
        SWITCH_PROCESS,
        STARTUP,
        SLEEP,


    }




    public static void switchProcess(){
        // what should it be?
        System.out.println("OS.switchProcess()");
        currentCall = CallType.SWITCH_PROCESS;
        kernel.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //Kernel.;
    }

    public static int CreateProcess (UserlandProcess up){

        System.out.println("OS.createProcess()");
        int i = 0;
        //resetParameter();
        parameters.clear();
        parameters.add(up);
        currentCall = CallType.CREATE_PROCESS;
        up.setPriority(Priority.INTERACTIVE);

        //currentCall = CallType.SwitchProcess;
        //switch to Kernel, on back to... section
        kernel.start();
//        System.out.println("This is createProcess() in OS class, debugging");
        //up.stop();
        //System.out.println("This is createProcess() in OS class, debugging, testing, right after stop()");
        if (Scheduler.hasCurrentlyRunning()) {
            System.out.println("OS.CreateProcess(): has currently running process");
            up.stop();

//            Scheduler.currentlyRunning.stop();
        }
         else {
            System.out.println("OS.CreateProcess(): has no currently running process");
            try {
                while (returnValue == null) {
                    System.out.println("returnValue is null");
                    Thread.sleep(2000);

                    i++;
                    System.out.println(i);
                    //up.cooperate();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
//        System.out.println("This is create process in OS, right before returning, debugging");
        return (Integer) returnValue;
        //return 0;
    }



    // parameter 1: a userlandProcess
    // parameter 2: an enum for priority
    public static int CreateProcess (UserlandProcess up, Priority priority) {
        int i = 0;

        parameters.clear(); // resetParameter();
        parameters.add(up); // add userlandProcess to parameters
        parameters.add(priority);

        currentCall = CallType.CREATE_PROCESS;

        up.setPriority(priority);

        //currentCall = CallType.SwitchProcess;
        //switch to Kernel, on back to... section
        kernel.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("This is createProcess() in OS class, debugging");
        //up.stop();
        //System.out.println("This is createProcess() in OS class, debugging, testing, right after stop()");
        if (Scheduler.hasCurrentlyRunning()) {
            System.out.println("This is create process in OS, debugging1, inside if");
            up.stop();
        }
        else {
            System.out.println("This is create process in OS, debugging, inside else");
            try {
                while (returnValue == null) {
                    System.out.print("This is create process in OS, debugging, inside else and while true loop  ");
                    Thread.sleep(2000);

                    i++;
                    System.out.println(i);
                    //up.cooperate();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("This is create process in OS, right before returning, debugging");
        return (Integer) returnValue;
    }

    /**
     * calling sleep() in the kernel which will call sleep() in Scheduler
     * @param milliseconds
     */
    public static void sleep (int milliseconds) {
        currentCall = CallType.SLEEP;
        parameters.clear();
        parameters.add(milliseconds);

        System.out.println("This is sleep() in OS, debugging");
        //Kernel.Sleep(milliseconds);
    }




    public static void Startup (UserlandProcess init) {
        kernel = new Kernel(); // initialize the kernel
//        System.out.println("this is OS class");
        CreateProcess(init); // create initial process
        System.out.println("OS.startUp(): Process Initialized");
//        CreateProcess(new IdleProcess()); //create idle process
    }

    public static Object getParametersItem () {
        return parameters.get(0);
    }



    public static void setReturnValue (){

        returnValue = 1;
        System.out.println("returnVal: " + returnValue);
    }

    public static void runScheduledProcesses(){
        kernel.runScheduledProcesses();
    }


    /**
     * HW4 - Messages
     */


    /**
     *  returns the current process' pid
     */
    public int GetPid(){
        return Kernel.GetPid();
    }
    public int GetPidByName(String name){
        return kernel.GetPidByName(name);
    }


    public void SendMessage(KernelMessage km){
        // should use the copy constructor to make a copy of the orginal message
        // Use the copy constructor to ensure the original message is untouched
        KernelMessage copiedMessage = new KernelMessage(km);


        int senderPid = GetPid();

    }
    public KernelMessage WaitForMessage(){
        return kernel.WaitForMessage();
    }

    public void GetMapping (int virtualPageNumber){
        kernel.GetMapping(virtualPageNumber);
    }

    /**
     * returns the start virtual address
     *
     * @param size
     * @return
     */
    public int AllocateMemory (int size) {
        // in OS. Ensures that size and pointer are multiples of 1024
        // return failure if not
        if (size % 1024 != 0){
            System.out.println("Allocation size must be a multiple of 1024.");
            return -1; // indicate failure
        }
        return kernel.AllocateMemory(size);
    }



    /**
     * takes the virtual address and the amount to free
     * @param pointer
     * @param size
     * @return
     */
    public boolean FreeMemory (int pointer, int size) {
        return kernel.FreeMemory(pointer, size);
    }



}
