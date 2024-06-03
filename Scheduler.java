import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.time.Duration;


public class Scheduler {
    private static LinkedList<UserlandProcess> userProcessList;//remove

    private static Queue<PCB> AllQueue = new LinkedList<>();
    private static Queue<PCB> RealTimeQueue = new LinkedList<>();
    private static Queue<PCB> BackgroundQueue = new LinkedList<>();
    private static Queue<PCB> InteractiveQueue = new LinkedList<>();
    private static PriorityQueue<PCB> sleepingQueue = new PriorityQueue<PCB>();

    private Timer timer;
    private Kernel currentProcess;
    private static Random random = new Random();


    // get the current instant in the default time zone
    private Clock clock = Clock.systemDefaultZone();
    Instant now = clock.instant();

    // 1000 milliseconds + current time
    Instant wakeuptime = now.plus(Duration.ofMillis(1000));

    public static UserlandProcess currentlyRunningU; // assignment 1
    public static PCB currentlyRunning;


    private static PCB pcb;
    private int timeout = 0;
    private static int pid = 0;
    private Map <String, Integer> nameToPidMap = new HashMap<>();
    private Map <Integer, PCB> processMap = new HashMap<>();



    public Scheduler(){
        userProcessList = new LinkedList<>();
        timer = new Timer(true);

        interrupt();
    }


    public Kernel getCurrentlyRunning(){
        return currentProcess;
    }


    private void interrupt(){
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentlyRunning != null)
                    currentlyRunning.requestStop();
            }
        }, 250, 250);

    }


    public static int CreateProcess(UserlandProcess up){
        System.out.println("Scheduler.createProcess(), single parameter");

         userProcessList.add(up); // assignment 1



        /**
         * single parameter, default to be an INTERACTIVE process
         */
        if (pcb != null){
            pcb.setUserlandProcess(up);
            InteractiveQueue.add(pcb);
        }



        /**
         * if there's no currently running process, then switch to next process
         */
        if (currentlyRunning == null) {
            System.out.println("Scheduler: no currently running process");
            //currentlyRunning = up;
            SwitchProcess();
        }
        else
            pid++; // increment Process ID


        return pid;
    }


    /**
     * parameter 1: a userlandProcess
     * parameter 2: an enum for priority
     */
    public int CreateProcess (UserlandProcess up, Priority priority) {
        System.out.println("Scheduler.createProcess(), double parameter");

        userProcessList.add(up); // assignment 1



        /**
         * double parameter, set the priority from parameter
         */
        pcb.setUserlandProcess(up);
        pcb.setPriority(priority);



        addProcessToAppropriateQueue(priority);




        /**
         * if there's no currently running process, then switch to next process
         */
        if (currentlyRunning == null) {
            System.out.println("Scheduler: no currently running process");
            //currentlyRunning = up;
            SwitchProcess();
        }
        else
            pid++; // increment Process ID


        return pid;
    }


    public static void SwitchProcess(){
        System.out.println("Scheduler.switchProcess()");
    // take currently running process and put it at the end of the list
        if (currentlyRunning != null && !currentlyRunning.isDone()){
            System.out.println("Current process is alive and not done");
            try {
                currentlyRunning.stop();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            //if (currentlyRunning)

            System.out.println("Stopping current process");
            userProcessList.addLast(currentlyRunning.getUserlandProcess());

        }
        System.out.println("Before switch Process list size: " + userProcessList.size());
        System.out.println("Changing process and running");
        currentlyRunningU = userProcessList.poll();
        System.out.println("After switch Process list size: " + userProcessList.size());
        currentlyRunning.start();
        currentlyRunning.clearTLB();
//        if (currentlyRunning != null){
//
//        }

    }

    public static boolean hasCurrentlyRunning(){
        if (currentlyRunning == null)
            return false;
        else
            return true;
    }


    /**
     * Determines the priority of the process,
     * then add it to the corresponding queue
     */
    private void addProcessToAppropriateQueue(Priority priority) {
        switch (priority) {
            case REAL_TIME:
                RealTimeQueue.add(pcb);
                break;
            case INTERACTIVE:
                InteractiveQueue.add(pcb);
                break;
            case BACKGROUND:
                BackgroundQueue.add(pcb);
                break;
            default:
                break;
        }
    }
    /**
     * updates the storage, put the process in the separate queue
     * what storage? what process? what separate queue?
     * @param milliseconds
     */
    public static void Sleep(int milliseconds) {
        System.out.println("This is sleep in scheduler");
        pcb.incrementSleepCounter();

        Instant wakeUpTime = Instant.now().plusMillis(milliseconds);
        pcb.setWakeUpTime(wakeUpTime);
        sleepingQueue.add(pcb);



        if (pcb.getSleepCounter() >= 5){
            demotion(pcb);
            pcb.resetSleepCounter();
        }

        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }



    public void wakeUpSleepingProcesses() {
        Instant now = Instant.now();
        Queue<PCB> toWakeUp = new LinkedList<>();

        // Identify processes to wake up
        for (PCB pcb : sleepingQueue) {
            if (now.isAfter(pcb.getWakeUpTime())) {
                toWakeUp.add(pcb);
            }
        }

        // Wake up identified processes
        for (PCB pcb : toWakeUp) {
            sleepingQueue.remove(pcb);
            addProcessToAppropriateQueue(pcb.getPriority());
            pcb.setWakeUpTime(null); // Reset wake up time
        }
    }

    /**
     * Determines the chance of process' run time based on their priorities
     */
    public static void Probabilistic_Model() {
        Random random = new Random();
        /**
         * if all three queue/processes present
         * check to see if real time, interactive, background queues are empty
         */
        if (!RealTimeQueue.isEmpty() && !InteractiveQueue.isEmpty()
            && !BackgroundQueue.isEmpty()) {

            // generate a random number between 1 and 10
            int randomNum = random.nextInt(10) + 1;



            /**
             * Chance of run time-->
             * Background : 1/10
             * Interactive: 3/10
             * Real time: 6/10
             */
            if (randomNum == 1){
                // run background process
                BackgroundQueue.poll().run();
            }
            else if (randomNum >=2 && randomNum <=4){ // 3/10 chance
                // run interactive process
                InteractiveQueue.poll().run();
            }
            else if (randomNum >= 5 && randomNum <= 10) { // 6/10 chance
                // run real time process
                RealTimeQueue.poll().run();
            }
        }



        /**
         * if there's only interactive and background queue/processes
         */
        else if (RealTimeQueue.isEmpty() && !InteractiveQueue.isEmpty()
            && !BackgroundQueue.isEmpty()) {
            int randomNum = random.nextInt(4) + 1;
            if (randomNum == 1) {
                // run background process
                BackgroundQueue.poll().run();
            }
            else if (randomNum >=2 && randomNum <=4){
                // run intective queue
                InteractiveQueue.poll().run();
            }
        }



        /**
         * if only background, use the first of those
         */
        else {
            BackgroundQueue.poll().run();
        }

        }


    /**
     * if timeout more than 5 times,
     * how to check timeout?
     * If it sleeps for more than 5 times in a row
     * why would a process go to sleep? Required?
     */




    /**
     * if a process becomes sleeps 5 times consecutively, it will get demoted,
     * @param pcb
     */
    public static void demotion(PCB pcb){
        if (pcb.getPriority() == Priority.REAL_TIME){
            pcb.setPriority(Priority.INTERACTIVE);
        }
        else if (pcb.getPriority() == Priority.INTERACTIVE) {
            pcb.setPriority(Priority.BACKGROUND);
        }
    }


    public void addProcess(UserlandProcess longRunningProcess, Priority realTime) {
    }

    public static void runScheduledProcesses() {
        while (!allQueuesAreEmpty()){
            Probabilistic_Model();
        }
    }

    private static boolean allQueuesAreEmpty() {
        return RealTimeQueue.isEmpty() && InteractiveQueue.isEmpty() &&
                BackgroundQueue.isEmpty();
    }

    public int getPid(){
        return pid;
    }


    /**
     * HW4 - Messages
     */


    /**
     *  returns the current process' pid
     */
    public static int GetPid(){
        if (currentlyRunning != null)
            return currentlyRunning.getPid();
        else
            return -1;
    }

    /**
     *     returns the pid of process with that name
     */
    public int GetPidByName(String name){
        return nameToPidMap.getOrDefault(name, -1);
    }

    /**
     * Adds PCB to the hashmap
     * @param pcb
     */
    public void addProcess(PCB pcb){
        nameToPidMap.put(pcb.getName(), pcb.getPid());
    }
    public void removeProcess(int pid) {
        PCB pcb = processMap.remove(pid);
        if (pcb != null){

        }
    }

    public PCB getProcessByPid(int pid){
        return processMap.get(pid);
    }

    public Queue<PCB> getAllQueue() {
        return AllQueue;
    }

    public int getCurrentProcessPid() {
        return pid;
    }

    public void GetMapping(int virtualPageNumber){
        // check the currently running PCB
        if (currentlyRunning == null) {
            System.out.println("No current PCB running.");
            return;
        }
        currentlyRunning.GetMapping(virtualPageNumber);
    }

    public PCB getRandomProcess(){
        // List of all queues ordered by priority
        List<Queue<PCB>> queues = Arrays.asList(RealTimeQueue, InteractiveQueue, BackgroundQueue);

        // Temporary list to hold all processes with physical memory
        List<PCB> candidates = new ArrayList<>();


        // Iterate over all queues and add candidates with physical memory
        for (Queue<PCB>queue : queues){
            for (PCB process : queue) {
                if (process.hasPhysicalPages()) {
                    candidates.add(process);
                }
            }
        }

        // Check if we have any candidates
        if (!candidates.isEmpty()) {
            // Return a random process from the candidates list
            return candidates.get(random.nextInt(candidates.size()));
        }

        // No process available with physical pages, return null or handle otherwise
        return null;
    }


}
