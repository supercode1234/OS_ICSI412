public class Ping extends UserlandProcess {
    private Kernel kernel;
    private int targetPid;
    private OS os;

    public Ping(int targetPid) {
        //this.kernel = kernel;
        this.targetPid = targetPid;
    }

    /**
     * Method to wait for a message
     * @return
     */
    private KernelMessage waitForMessage() {
        return os.WaitForMessage();
    }

    @Override
    public void main() {
        int messageType = 1;
        String messageContent = "ping";
        byte[] messageData = messageContent.getBytes();

        System.out.println("Ping: Sending ping to pong.");
        KernelMessage pingMessage = new KernelMessage(this.getPID(), targetPid, messageType, messageData);
        kernel.SendMessage(pingMessage);


        // Wait for a pong response
        KernelMessage response = waitForMessage();
        if (response != null && new String(response.getData()).equals("pong")) {
            System.out.println("Ping: Received pong from Pong.");
        }
    }


    public void setTargetPid(int pongPid) {
        targetPid = pongPid;
    }
}
