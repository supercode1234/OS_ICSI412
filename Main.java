
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {


    public static void main(String[] args) {
//        System.out.println("hi, this is main class, debugging");

        // starts the OS with the HelloWorld program
        OS.Startup(new HelloWorld());

        // commented the below line for debugging
        OS.CreateProcess(new GoodbyeWorld());
        //OS os;


        testReadWrite();
        testMemoryExtension();
        testInvalidMemoryAccess();
        testProcessIsolation();
        

        /*
        test for assignment 4
        Ping ping = new Ping ( -1);
        Pong pong = new Pong();

        int pingPid = OS.CreateProcess(ping);
        int pongPid = OS.CreateProcess(pong);
        ping.setTargetPid(pongPid);
        OS.runScheduledProcesses();

        */

        /**
         * Testing assignment 2
         */
        /*
        Scheduler scheduler = new Scheduler();
        UserlandProcess longRunningProcess = new LongRunningProcess();
        UserlandProcess sleepingProcess = new SleepingRealTimeProcess();
        OS.CreateProcess(longRunningProcess, Priority.REAL_TIME);
        OS.CreateProcess(sleepingProcess, Priority.REAL_TIME);
        OS.runScheduledProcesses();
         */


    }

    private static void testProcessIsolation() {
        System.out.println("Starting testProcessIsolation...");

        OS os = new OS();
        // Simulate two separate allocations, mimicking two processes
        int address1 = os.AllocateMemory(UserlandProcess.getPageSize());
        int address2 = os.AllocateMemory(UserlandProcess.getPageSize());

        UserlandProcess userlandProcess = new UserlandProcess() {
            @Override
            public void main() {

            }
        };

        userlandProcess.Write(address1, (byte)50); // Write to first block
        userlandProcess.Write(address2, (byte)60); // Write to second block

        byte value1 = userlandProcess.Read(address1);
        byte value2 = userlandProcess.Read(address2);

        // Check if the writes and reads are isolated and correct
        if (value1 == 50 && value2 == 60) {
            System.out.println("testProcessIsolation Passed: Processes are isolated.");
        } else {
            System.out.println("testProcessIsolation Failed: Memory isolation between processes is compromised.");
        }

        // Consider deallocating the allocated memory here to clean up
    }


    private static void testInvalidMemoryAccess() {
        System.out.println("Starting testInvalidMemoryAccess...");

        // Attempt to read from and write to an invalid memory address
        int invalidAddress = UserlandProcess.getMemorySize() + 1024; // Beyond allocated memory
        boolean caughtException = false;
        UserlandProcess userlandProcess = new UserlandProcess() {
            @Override
            public void main() {

            }
        };

        try {
            // Assuming readMemory and writeMemory methods exist
            userlandProcess.Write(invalidAddress, (byte)123); // Attempt invalid write
            byte value = userlandProcess.Read(invalidAddress); // Attempt invalid read
        } catch (SecurityException | IllegalArgumentException e) {
            caughtException = true;
        }

        if (caughtException) {
            System.out.println("testInvalidMemoryAccess Passed: Invalid memory access correctly prevented.");
        } else {
            System.out.println("testInvalidMemoryAccess Failed: Invalid memory access not detected.");
        }
    }


    private static void testMemoryExtension() {
        System.out.println("Starting testMemoryExtension...");

        // Assuming PAGE_SIZE and MEMORY_SIZE are accessible
        int allocationSize = UserlandProcess.getPageSize(); // Allocate one page at a time
        int totalAllocations = 0;
        boolean success = true;
        OS os = new OS();

        // Try allocating memory until it fails or exceeds a reasonable limit
        while (success && totalAllocations < (UserlandProcess.getMemorySize() / allocationSize * 2)) { // Example limit: 2x memory size
            int address = os.AllocateMemory(allocationSize);
            if (address == -1) {
                success = false; // Allocation failed
            } else {
                totalAllocations++;
            }
        }

        if (success) {
            System.out.println("testMemoryExtension Passed: Memory successfully extended.");
        } else {
            System.out.println("testMemoryExtension Failed: Could not allocate more memory.");
        }

        // Consider deallocating the allocated memory here to clean up
    }


    private static void testReadWrite() {
        System.out.println("Testing Read/Write...");
         //Allocate memory and write a value
        OS os = new OS();
        PCB pcb = null;
        UserlandProcess userlandProcess = new UserlandProcess() {
            @Override
            public void main() {

            }
        };
        int address = os.AllocateMemory(1024); // Assuming allocateMemory returns a starting address
        userlandProcess.Write(address, (byte) 42); // Assuming writeMemory method

         //Read the value back
         byte value = userlandProcess.Read(address); // Assuming readMemory method

         //Check if the written value matches the read value
         if (value == 42) {
             System.out.println("Read/Write Test Passed");
         } else {
             System.out.println("Read/Write Test Failed");
         }
    }
}