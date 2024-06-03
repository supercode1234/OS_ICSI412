public class Pong extends UserlandProcess{
    private Kernel kernel;

    public Pong() {
        //this.kernel = kernel;
    }

    @Override
    public void main() {
        while (true) { // Keep listening for messages indefinitely
            System.out.println("Pong: Waiting for ping message...");
            KernelMessage message = waitForMessage();

            // Check if the received message is a "ping"
            if (message != null && new String(message.getData()).equals("ping")) {
                System.out.println("Pong: Received ping. Sending pong back.");
                sendPongMessage(message.getSenderPid()); // Respond back to the sender of the ping
            }
        }
    }

    private KernelMessage waitForMessage() {
        return kernel.WaitForMessage();
    }

    private void sendPongMessage(int targetPid) {
        // Assuming a 'pong' message doesn't need additional data besides the acknowledgment
        byte[] data = "pong".getBytes();
        KernelMessage pongMessage = new KernelMessage(this.getPID(), targetPid, 2, data); // '2' could signify a pong message type
        kernel.SendMessage(pongMessage);
    }
}
