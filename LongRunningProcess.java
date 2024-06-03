public class LongRunningProcess extends UserlandProcess {
    /**
     * Simulates a process that would trigger a demotion
     * due to excessive run time
     */

    @Override
    public void main() {
        System.out.println("Long running process started");
        // sleep to simulate a long running time
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Long running process completed.");

    }
}
