public class KernelMessage {
    private int senderPid;
    private int targetPid;
    private int what;
    private byte[] data;

    // Constructor
    public KernelMessage (int senderPid, int targetPid, int what, byte[]data){
        this.senderPid = senderPid;
        this.targetPid = targetPid;
        this.what = what;
        this.data = data.clone();
    }

    // Copy Constructor
    public KernelMessage(KernelMessage other){
        this.senderPid = other.senderPid;
        this.targetPid = other.targetPid;
        this.what = other.what;
        this.data = other.data.clone();
    }

    public int getSenderPid(){
        return senderPid;
    }
    public void setSenderPid(int senderPid){
        this.senderPid = senderPid;
    }

    public int getTargetPid(){
        return targetPid;
    }
    public void setTargetPid(int targetPid){
        this.targetPid = targetPid;
    }

    @Override
    public String toString() {
        return String.format("From: %d, To: %d, What: %d", senderPid, targetPid, what);
    }


    public byte[] getData() {
        return data;
    }
}
