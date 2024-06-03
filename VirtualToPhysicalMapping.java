public class VirtualToPhysicalMapping {
    // with public members for physical page number
    // and on disk page number.
    public int physicalPageNumber;
    public int diskPageNumber;



    // create a constructor (no parameters) that sets physical
    // page number and disk page number to -1.
    public VirtualToPhysicalMapping(){
        this.physicalPageNumber = -1;
        this.diskPageNumber = -1;
    }

    public int getPhysicalPageNumber() {
        return physicalPageNumber;
    }

    public void setPhysicalPageNumber(int physicalPageNumber) {
        this.physicalPageNumber = physicalPageNumber;
    }

    public int getDiskPageNumber() {
        return diskPageNumber;
    }

    public void setDiskPageNumber(int diskPageNumber) {
        this.diskPageNumber = diskPageNumber;
    }

}
