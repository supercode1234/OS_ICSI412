import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class Kernel implements Runnable, Device {

    private Scheduler scheduler;
    private Thread thread;
    private Semaphore semaphore;
    //private int pid;
    private String name;
    private LinkedList<KernelMessage> messageQueue = new LinkedList<>();

    // array to store VFS device IDs
    private int[] deviceIDs = new int[10];


    // total pages = 1024
    private static final int TOTAL_PAGES = UserlandProcess.getMemorySize()  / UserlandProcess.getPageSize();


    // array of 1024 indices
    private static boolean[] freeList = new boolean[TOTAL_PAGES];


    // array of 1024 indices
    //private static int[] virtualToPhysicalMapping = new int[TOTAL_PAGES];
    private static VirtualToPhysicalMapping[] virtualToPhysicalMapping = new VirtualToPhysicalMapping[TOTAL_PAGES];




    public Kernel() {
        this.scheduler = new Scheduler();
        this.semaphore = new Semaphore(0); // 0 permits
        this.thread = new Thread (this);


        // Initialize the array with -1, indicating no device is open
        for (int i = 0; i < deviceIDs.length; i++){
            deviceIDs[i] = -1;
        }
        //Arrays.fill(deviceIDs, -1);
        this.thread.start();

        Arrays.fill(freeList, false); // physical pages are free initially
        //Arrays.fill(virtualToPhysicalMapping, -1);

        // Initialize the array
//        for (int i = 0; i < TOTAL_PAGES; i++) {
//            virtualToPhysicalMapping[i] = new VirtualToPhysicalMapping();
//        }

    }

    @Override
    public int Open(String s) {
        //UserlandProcess currentProcess = scheduler.getCurrentlyRunning();
//        if (currentProcess == null) {
//            return -1; // No process is currently running
//        }
        return 0;

    }

    @Override
    public void Close(int id) {

    }

    @Override
    public byte[] Read(int id, int size) {
        return new byte[0];
    }

    @Override
    public void Seek(int id, int to) {

    }

    @Override
    public int Write(int id, byte[] data) {
        return 0;
    }



    public static void Sleep(int milliseconds) {
        System.out.println("This is sleep in Kernel");
        Scheduler.Sleep(milliseconds);
    }

    // parameter 1: a userlandProcess
    // parameter 2: an enum for priority
    public void CreateProcess (UserlandProcess user, OS.CallType priority) {

    }


    @Override
    public void run() {
        while (true){
            try {
                semaphore.acquire(); // to see if I should be running
                switch(OS.currentCall){
                    case CREATE_PROCESS:
                        System.out.println("Kernel.createProcess");
                        // not done
                        // call the function that implements them
                        Scheduler.CreateProcess((UserlandProcess) OS.getParametersItem());
                        System.out.println("Kernel.createProcess: changing return value");
                        OS.setReturnValue();
                        //Scheduler.CreateProcess(OS.currentCall);
                        //run(Scheduler.currentlyRunning);
                        //Scheduler.currentlyRunning.run();
                        //OS.CreateProcess(new HelloWorld());
//                        System.out.println("This is Kernel while (true) method, switch on CreateProcess, end");
                        //Scheduler.

                        break;

                    case SWITCH_PROCESS:
                        // not done
                        System.out.println("Kernel.switchProcess");
                        Scheduler.SwitchProcess();
                        break;


                    /**
                     * call sleep() in Scheduler, pass in the sleeping time (in milliseconds)
                     * from the parameter in OS
                     */
                    case SLEEP:
                        Scheduler.Sleep((int)OS.getParametersItem());
                        break;

                    default: break;
                }
                // write code here?


            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
        // write something
//        if (Scheduler.currentlyRunning != null)
//            Scheduler.currentlyRunning.run();
        //new Thread(Scheduler.currentlyRunning).start();

    }


    public void start() {
        semaphore.release();// need to know why
    }


    public void runScheduledProcesses() {
        Scheduler.runScheduledProcesses();
    }



    /**
     * HW4 - Messages
     */

    /**
     * returns the current process' pid
     * @return
     */

    public static int GetPid(){
        return Scheduler.GetPid();
    }

    /**
     *     returns the pid of process with that name
     */
    public int GetPidByName(String name){
        return scheduler.GetPidByName(name);
    }

    public String getName () {
        return this.name;
    }


    public void SendMessage(KernelMessage km){
        PCB senderProcess = scheduler.getProcessByPid(km.getSenderPid());
        PCB targetProcess = scheduler.getProcessByPid(km.getTargetPid());

        KernelMessage copiedMessage = new KernelMessage(km);

        // populate the sender's PID for security and efficiency
        copiedMessage.setSenderPid(scheduler.getPid());
    }



    public KernelMessage WaitForMessage(){
        int currentPid = scheduler.getCurrentProcessPid();
        PCB currentProcess = scheduler.getProcessByPid(currentPid);
        if (currentProcess != null){
            return currentProcess.waitForMessage();
        }
        return null;
    }

    public LinkedList<KernelMessage> getMessageQueue() {
        return messageQueue;
    }







    public void GetMapping(int virtualPageNumber){
        scheduler.GetMapping(virtualPageNumber);
    }


    /**
     * ALlocates memory
     * @param size
     * @return start virtual address
     */
    public int AllocateMemory (int size) {
        // need an efficient mechanism to track if pages are in use or not.
        // an array of boolean will work.

        // (size + 1024 -1) / 1024
        int pagesNeeded = (size + UserlandProcess.getPageSize() - 1) / UserlandProcess.getPageSize();
        //VirtualToPhysicalMapping hi = new VirtualToPhysicalMapping();

        // Find the first hole in the virtual space that fits pagesNeeded
        // This might involve a more complex data structure in practice

        // i < = 1024-pages_needed
        for (int i = 0; i <= virtualToPhysicalMapping.length - pagesNeeded; i++) {
            if (checkIfHoleFits(i, pagesNeeded)) {
                int startVirtualPage = i;
                for (int j = 0; j < pagesNeeded; j++) {

                    /* HW5
                    virtualToPhysicalMapping[startVirtualPage + j] = findFreePhysicalPage(); // Find and assign a free physical page
                    if (virtualToPhysicalMapping[startVirtualPage + j] == -1) return -1; // If no free physical page is found
                    freeList[virtualToPhysicalMapping[startVirtualPage + j]] = true; // Mark physical page as in use
                    */

                    // Initialize the mapping without assigning a physical page
                    if (virtualToPhysicalMapping[startVirtualPage + j] == null){
                        virtualToPhysicalMapping[startVirtualPage] = new VirtualToPhysicalMapping();
                    }
                }
                return startVirtualPage * UserlandProcess.getPageSize(); // Return start virtual address
            }
        }
        return -1; // Failure to allocate
    }

    private int findFreePhysicalPage() {
        for (int i = 0; i < freeList.length; i++) {
            if (!freeList[i]) { // This page is not in use
                return i; // Return the index of the free page
            }
        }
        return -1; // No free page found
    }

    private boolean checkIfHoleFits(int i, int pagesNeeded) {
        //int startVirtualPage = 1;
        int end = i + pagesNeeded;
        // Ensure the block doesn't exceed memory bounds
        if (end > virtualToPhysicalMapping.length) return false;

        for (int j = i; j < end; j++) {
            // If the virtual page is already mapped (not -1), the hole doesn't fit
            if (virtualToPhysicalMapping[j].getPhysicalPageNumber() != -1) return false;
        }
        return true; // The hole fits
    }


    /**
     * Frees memory
     * @param pointer
     * @param size
     * @return
     */
    public boolean FreeMemory(int pointer, int size) {
        // Calculate the starting virtual page and how many pages need to be freed
        int startVirtualPage = pointer / UserlandProcess.getPageSize();
        int pagesToFree = (size + UserlandProcess.getPageSize() - 1) / UserlandProcess.getPageSize();

        boolean freedAny = false; // Track if any memory was actually freed

        for (int i = 0; i < pagesToFree; i++) {
            int virtualPage = startVirtualPage + i;

            // ensure index is within bounds and mapped
            if (virtualPage < virtualToPhysicalMapping.length && virtualToPhysicalMapping[virtualPage] != null) {
                // Mark the corresponding physical page as not in use
                int physicalPage = virtualToPhysicalMapping[virtualPage].getPhysicalPageNumber();


                // check to see if the physical page is -1
                // before updating the physical memory in use
                if (physicalPage != -1) {
                    if (physicalPage < freeList.length) {
                        freeList[physicalPage] = true; // mark physical page as free
                        freedAny = true;
                    }
                    // clear the physical page mapping
                    virtualToPhysicalMapping[virtualPage].setPhysicalPageNumber(-1);
                }

                //  Set the array for each block back to null
                //  (freeing the VirtualToPhysicalmapping)
                virtualToPhysicalMapping[virtualPage] = null;
            }
        }

        return freedAny; // Return true if any memory was freed, false otherwise
    }

}
