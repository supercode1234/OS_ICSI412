import java.util.concurrent.Semaphore;
import java.util.Random;

/**
 * This class will be the base class for every test program that we write
 */
public abstract class UserlandProcess implements Runnable {
    private Thread thread; //the thread on which the process will run
    private Semaphore semaphore; //a semaphore to manage execution control of the process

    private int PID = 0;

    private Priority priority;


    //Flag to indicate if the process's quantum (execution time slice) has expired
    private boolean quantumExpire; //volatile?




    private static final int PAGE_SIZE = 1024; // 1KB pages
    private static final int MEMORY_SIZE = 1024 * PAGE_SIZE; // 1MB total memory
    private static byte [] memory = new byte [MEMORY_SIZE]; // physical memory
    private static int[][]TLB = new int [2][2]; // TLB holding 2 mappings



    // constructor initializes the process with its own thread and a semaphore
    // for synchronization.
    public UserlandProcess(){
        this.thread = new Thread(this); // initialize the thread with the current instance

        // initialize the semaphore with 0 permits to block execution initially
        this.semaphore = new Semaphore(0);

        this.quantumExpire = false; //  initially, the quantum has not expired

        this.PID++;

        for (int i = 0; i < TLB.length; i++){
            TLB[i][0] = -1; // virtual page numbers to -1
            TLB[i][1] = -1; // physical page numbers to -1
        }
    }

    public int getPID (){
        return this.PID;
    }

    // request to stop the process by setting the quantum expire flag to true
    public void requestStop() {
        this.quantumExpire = true;
    }


    // abstract method that must be implemented by subclasses to define the process's main behavior
    public abstract void main();


    // checks if the process is stopped based on the availability of permits in the semaphore
    public boolean isStopped() {
        return semaphore.availablePermits() == 0;
    }


    // checks if the process's thread has completed execution
    public boolean isDone() {
        return !thread.isAlive();
    }


    // starts the process by releasing a semaphore permit and
    // starting the thread if it's not already alive
    public void start() {
        semaphore.release();
        if (!thread.isAlive()) {
            thread.start();
        }
    }



    // stops the process by acquiring a semaphore permit, effectively blocking
    // further execution until released again.
    void stop() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            // properly handle the InterruptedException
            Thread.currentThread().interrupt();
        }
    }


    // The main run method that waits for a permit, then executes the process's
    // main method.
    @Override
    public void run() {
        try {
            semaphore.acquire(); // wait for the semaphore to be released before proceeding
            main(); // execute the process's main behavior
        } catch (InterruptedException e) {
            // properly handle the InterruptedException
            Thread.currentThread().interrupt();
        }
    }


    // temporarily yields control to allow other processes to execute, used when quantum expires.
    public void cooperate() {
        if (quantumExpire){
            quantumExpire = false; // reset the quantum expire flag
            System.out.println("hihihi-----. this is Userland process, Cooperate(), debugging");
            OS.switchProcess();
        }
    }

    public Priority getPriority(){
        return this.priority;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }


    static {
        for (int i = 0; i < TLB.length; i++){
            TLB[i][0] = -1; // Virtual
            TLB[i][1] = -1; // Physical
        }
    }


    /**
     * To check TLB for a given virtual page number
     * returns physical page number if found, else -1
     * @param virtualPageNumber
     * @return
     */
    public static int getTLBMapping(int virtualPageNumber){
        for (int i = 0; i < TLB.length; i++){
            if (TLB[i][0] == virtualPageNumber){
                // returns physical page number if found
                return TLB[i][1];
            }
        }
        return -1; // not found
    }

    public static void updateTLB(int virtualPageNumber, int physicalPageNumber){
        // Randomly choose a set of TLB entries to update
        int randomIndex = new Random().nextInt(TLB.length);


        // Update the TLB entry with the new mapping
        TLB[randomIndex][0] = virtualPageNumber;
        TLB[randomIndex][1] = physicalPageNumber;
    }


    // Method to clear the TLB
    public static void clearTLB() {
        for (int i = 0; i < TLB.length; i++) {
            TLB[i][0] = -1; // Invalidate the virtual page number
            TLB[i][1] = -1; // Invalidate the physical page number
        }
    }


    public static int getPageSize(){
        return PAGE_SIZE;
    }

    public static int getMemorySize(){
        return MEMORY_SIZE;
    }




    /**
     * For paging. To simulate accessing memory.
     * @param address
     * @return
     */
    public byte Read (int address){
        // first thing: find the page number. page number = address /page size.
        int virtualPageNumber = address / PAGE_SIZE;
        int offset = address % PAGE_SIZE;
        int physicalPageNumber = getTLBMapping(virtualPageNumber);

        if (physicalPageNumber == -1) {
            physicalPageNumber = getTLBMapping (virtualPageNumber);
            if (physicalPageNumber == -1) {
                throw new RuntimeException("Memory access violation");
            }
        }
        int physicalAddress = physicalPageNumber * PAGE_SIZE + offset;
        return memory[physicalAddress];
    }


    /**
     * To write on memory
     * @param address
     * @param value
     */
    public void Write (int address, byte value){
        // first thing: find the page number. page number = address /page size.
        int virtualPageNumber = address / PAGE_SIZE;
        int offset = address % PAGE_SIZE;
        int physicalPageNumber = getTLBMapping(virtualPageNumber);

        if (physicalPageNumber == -1) {
            physicalPageNumber = getTLBMapping (virtualPageNumber);
            if (physicalPageNumber == -1) {
                throw new RuntimeException("Memory access violation");
            }
        }
        int physicalAddress = physicalPageNumber * PAGE_SIZE + offset;
        memory[physicalAddress] = value;
    }




}
