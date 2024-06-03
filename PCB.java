import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;

public class PCB {
    // this class manages the process from the kernel's perspective and is not visible from userland.
    // that makes it secure

    private static int nextpid = 0; // static variable for generating unique process IDs

    private static int pid; // holds a PCB's process ID

    private UserlandProcess userlandProcess; // Reference to the associated UserlandProcess

    private Priority priority;

    private int sleepCounter = 0; // tracks consecutive sleeps
    private Instant wakeUpTime;
    private Kernel kernel;

    // change the int[] in the PCB to an array of 100 of these classes.
    //private int[] pageTable = new int[100]; // 100 1kb pages
    private VirtualToPhysicalMapping[] pageTable = new VirtualToPhysicalMapping[100]; // 100 1kb pages
    private int allocatedMemoryStartAddress = -1; // Initialize to an invalid value
    private int allocatedMemorySize = 0; // Initialize to zero indicating no memory allocated


    /**
     * Constructor for the PCB class. It assigns a unique process ID to this PCB
     * and sets the associated UserlandProcess.
     * creates the thread, sets pid
     * @param up the UserlandProcess associated with this PCB.
     */
    public PCB (UserlandProcess up) {
        this.pid = nextpid++; // Assigns a unique process ID to this PCB and increments the next available ID
        this.userlandProcess = up; // Sets the associated UserlandProcess
        Arrays.fill(pageTable, -1);
    }


    /**
     *  Stops the associated userlandProcess. It calls the stop method
     *  on the UserlandProcess and then loops, sleeping until the process reports
     *  it has stopped.
     */
    public void stop () throws InterruptedException {
        userlandProcess.stop();
        while (!userlandProcess.isStopped()){
            try {  
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println("the thread was interrupted, in PCB.stop()");
                Thread.currentThread().interrupt(); // preserve interrupt status
                break; // exit the loop
            }
        }
        // Once the process has stopped, proceed with termination and cleanup
        terminateProcess(); // Cleanup resources, free memory, 
    }

    private void terminateProcess() {
        if (allocatedMemoryStartAddress != -1) { // Check if memory was allocated
            kernel.FreeMemory(allocatedMemoryStartAddress, allocatedMemorySize);
            System.out.println("Process terminated and resources freed.");
        }
    }
    public void allocateMemoryForProcess(int size) {
        // This method simulates the allocation logic
        // It should ultimately set allocatedMemoryStartAddress and allocatedMemorySize based on the allocation
        int startAddress = kernel.AllocateMemory(size);
        if (startAddress != -1) { // Check if allocation was successful
            this.allocatedMemoryStartAddress = startAddress;
            this.allocatedMemorySize = size;
        } else {
            // Handle allocation failure
        }
    }


    /**
     *  Checks if the associated UserlandProcess is done.
     *  calls userlandProcess's isDone()
     *  @return true if the process is done, false otherwise.
     */
    public boolean isDone () {
        return userlandProcess.isDone();
    }


    /**
     * Starts the associated UserlandProcess.
     * calls userlandProcess' start()
     */
    public void run() {
        this.userlandProcess.start();
    }


    /**
     * returns the priority
     */
    public Priority getPriority() {
        return this.priority;
    }

    /**
     * set the priority
     * @param priority
     */
    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    /**
     * set userland processes
     * @param up
     */
    public void setUserlandProcess(UserlandProcess up){
        this.userlandProcess = up;
    }


    /**
     * Sleep() methods, timeout, demotion related
     */
    public void incrementSleepCounter(){
        sleepCounter++;
    }

    public void resetSleepCounter(){
        sleepCounter = 0;
    }

    public int getSleepCounter(){
        return sleepCounter;
    }


    public void setWakeUpTime(Instant wakeUpTime) {
        this.wakeUpTime = wakeUpTime;
    }

    public Instant getWakeUpTime() {
        return this.wakeUpTime;
    }

    public int getPid(){
        return this.pid;
    }

    public void requestStop() {
        userlandProcess.requestStop();
    }

    public void start() {
        userlandProcess.start();
    }
    public UserlandProcess getUserlandProcess(){
        return userlandProcess;
    }

    public String getName() {
        return kernel.getName();
    }




    /**
     * Method to wait for a message
     * @return
     */
    public synchronized KernelMessage waitForMessage() {
        LinkedList<KernelMessage> messageQueue = kernel.getMessageQueue();
        while (messageQueue.isEmpty()){
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return messageQueue.poll();
    }

    /**
     * Method to receive a message
     * @param message
     */
    public synchronized void receiveMessage(KernelMessage message){
        LinkedList<KernelMessage> messageQueue = kernel.getMessageQueue();
        messageQueue.add(message);
        notifyAll();
    }


    public int getPhysicalPageNumber (int virtualPageNumber){
        return pageTable[virtualPageNumber].getPhysicalPageNumber();
    }
    public void updatePageTable(int virtualPage, int physicalPage){
        pageTable[virtualPage].setPhysicalPageNumber(physicalPage);
    }

    public void GetMapping(int virtualPageNumber){

        int TLB_mapping = UserlandProcess.getTLBMapping(virtualPageNumber);
        if (TLB_mapping != -1) {
            //TLB hit
            System.out.println("TLB hit for virtual page: " + virtualPageNumber);
            // Use TLB_mapping as the physical page number
        } else {
            // TLB miss, check page table
            int physical_page_number = pageTable[virtualPageNumber].getPhysicalPageNumber();
            if (physical_page_number != -1){
                // Page table hit
                System.out.println("Page table hit for virtual page: " + virtualPageNumber);
                UserlandProcess.updateTLB(virtualPageNumber, physical_page_number);
            } else {
                // handles page fault (load the page into memory, updating page table and TLB)
                // Page fault: No physical page assigned
                handlePageFault(virtualPageNumber);
            }
        }


        // update (randomly) one of the two TLB entries.
        // back in user space, we should then try again to find a match.
        // TLB is in UserlandProcess
    }

    private void handlePageFault(int virtualPageNumber) {
        // Try to find a free physical page or evict one if needed
        int freePhysicalPage = findFreePhysicalPage();
        if (freePhysicalPage == -1) {
            // No free physical page, need to perform a page swap
            freePhysicalPage = swapPage();
        }

        if (pageTable[virtualPageNumber].getDiskPageNumber() != -1) {
            // Load page from disk into the freed-up physical page
            loadPageFromDisk(virtualPageNumber, freePhysicalPage);
        } else {
            // Initialize memory with zeros if it was never written to disk
            initializeMemory(freePhysicalPage);
        }
        // Update the mapping and TLB
        pageTable[virtualPageNumber].setPhysicalPageNumber(freePhysicalPage);
        UserlandProcess.updateTLB(virtualPageNumber, freePhysicalPage);
    }

    private void initializeMemory(int freePhysicalPage) {
    }

    private void loadPageFromDisk(int virtualPageNumber, int freePhysicalPage) {
    }

    private int findFreePhysicalPage() {
        return 1;
    }
    private int swapPage() {
        Scheduler scheduler = new Scheduler();
        PCB victimProcess = scheduler.getRandomProcess();
        int victimPage = findPageToEvict(victimProcess);
        writePageToDisk(victimPage);
        return releasePhysicalPage(victimPage);
    }

    private int releasePhysicalPage(int victimPage) {
        return 1;
    }

    private void writePageToDisk(int victimPage) {
    }

    private int findPageToEvict(PCB victimProcess) {
        return 1;
    }


    // Method to clear the TLB
    public static void clearTLB() {
        UserlandProcess.clearTLB();
    }

    public boolean hasPhysicalPages() {
        return true;
    }
}
