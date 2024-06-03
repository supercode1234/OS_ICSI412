import java.util.Arrays;
import java.util.Random;

public class RandomDevice implements Device {
    private Random[] randoms= new Random[10];

    private boolean isOpen = false;

    /**
     * initialize each Random instance
     */
    public RandomDevice() {
        for (int i = 0; i < randoms.length; i++){
            randoms[i] = new Random();
        }
    }


    /**
     * open device
     * will create a new random device
     * and put it in an empty spot in the array
     * @param s
     * @return
     */
    @Override
    public int Open(String s) {

        for (int i = 0; i < randoms.length; i++) {

            // Doing operations on the empty index of the array
            if (randoms[i] == null) {
                // if the supplied string in the parameter for open() is not null or empty
                if (s != null && !s.isEmpty()){

                    // assume the (String s) is the seed for the Random class
                    try {
                        // (convert the string to an integer).
                        randoms[i] = new Random (Integer.parseInt(s));
                    }
                    // if s (the seed) is not a valid integer, default to no seed
                    catch (NumberFormatException e){
                        randoms[i] = new Random();// no seed
                    }
                } else {
                    randoms[i] = new Random(); // no seed
                }
                break;
            }
        }
        isOpen = true;
        return 0;
    }


    /**
     * nulls the device entry, close device
     * @param id the random device's ID
     */
    @Override
    public void Close(int id) {
        isOpen = false;

        if (id >= 0 && id < randoms.length) {
            randoms[id] = null;
        }
    }

    /**
     * Read () will create/fill an array with random devices
     * @param id
     * @param size
     * @return
     */
    @Override
    public byte[] Read(int id, int size) {
        // write
        return new byte[0];
    }


    /**
     * Seek() will read random bytes but not return them
     * @param id
     * @param to
     */
    @Override
    public void Seek(int id, int to) {
        // seek
    }


    /**
     * writee() will return 0 length and do nothing
     * since writing to a random device doesn't make sense,
     * this method does nothing.
     * @param id
     * @param data
     * @return
     */
    @Override
    public int Write(int id, byte[] data) {
        return 0;
    }

}
