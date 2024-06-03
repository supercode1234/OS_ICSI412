public class SleepingRealTimeProcess extends UserlandProcess{
    /**
     * a real-time process that goes to sleep, avoiding demotion
     */
    @Override
    public void main() {
        System.out.println("SleepRealTimeProcess started and going to sleep");
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("SleepRealTimeProcess woke up and completed");
    }
}
