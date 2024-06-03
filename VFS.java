public class VFS implements Device {

    /**
     * Virtual File System (VFS)
     */
    private Device[] devices;
    private int[] ids;
    private int deviceCount;

    public VFS (int size) {
        devices = new Device[size];
        ids = new int [size];
        deviceCount = 0;
    }

    public void addDevice (Device device, int id) {
        if (deviceCount < devices.length){
            devices[deviceCount] = device;
            ids[deviceCount] = id;
            deviceCount++;
        }
    }

    @Override
    public int Open(String s) {
        for (Device device : devices){
            if (device != null)
                device.Open(s);
        }
        return 1; // necessary? change to void?
    }

    @Override
    public void Close(int id) {
        for (Device device : devices)
            if (device != null)
                device.Close(id);
    }

    @Override
    public byte[] Read(int id, int size) {
        return new byte[0];
    }

    @Override
    public void Seek(int id, int to) {

    }

    @Override
    public int Write(int id, byte[] data) {
        return 0;
    }
}
