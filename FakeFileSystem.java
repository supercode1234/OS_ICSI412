import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FakeFileSystem implements Device{

    private RandomAccessFile[] files = new RandomAccessFile[10];
    private boolean isOpen = false;


    /**
     * Open() will create and record a RandomAccessFile in the array.
     *
     */

    @Override
    public int Open(String filename) {
        /* if the filename is null or if the filename is empty
            throw an illegal argument exception
         */
        if (filename == null || filename.isEmpty()){
            throw new IllegalArgumentException("Filename cannot be null or empty");
        }


        /* if an index in files[] is not null, create and record a
           RandomAccessFile in that index, with the file name given in the open()'s parameter.
           Permission is read and write.
        */
        for (int i = 0; i < files.length; i++) {
            if (files[i] == null) {
                try {
                    files[i] = new RandomAccessFile(filename, "rw");
                    // return the index/id of the opened file
                    return i;
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("File not found: " + filename, e);
                }
            }
        }
        throw new RuntimeException("No available slot for new file");
    }


    /**
     * close the RandomAccessFile and clear out all the internal array
     * @param id
     */
    @Override
    public void Close(int id) {
        if(id >= 0 && id < files.length && files[id] != null){
            try {
                files[id].close();
                files[id] = null;
            } catch (IOException e) {
                throw new RuntimeException("Error closing file with id: " + id, e);
            }
        }
        else {
            throw new IllegalArgumentException("Invalid file id: " + id);
        }
    }



    @Override
    public byte[] Read(int id, int size) {
        if (id < 0 || id >= files.length || files[id] == null){
            throw new IllegalArgumentException("Invalid file id: " + id);
        }
        byte[] buffer = new byte[size];
        try {
            int bytesRead = files[id].read(buffer);
            if (bytesRead < size) {
                // if fewer bytes were read than requested, return the smaller array
                // it will return the number of elements the byteRead specifies
                return java.util.Arrays.copyOf(buffer, bytesRead);
            }
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Error reading from file with id: " + id, e);
        }
    }

    @Override
    public void Seek(int id, int to) {
        if (id < 0 || id >= files.length || files[id] == null){
            throw new IllegalArgumentException("Invalid file id" + id);
        }
        try {
            files[id].seek(to);
        } catch (IOException e) {
            throw new RuntimeException("Error seeking in file with id: " +  id, e);
        }

    }

    @Override
    public int Write(int id, byte[] data) {
        if (id < 0 || id >= files.length || files[id] == null){
            throw new IllegalArgumentException("Invalid file id" + id);
        }
        try {
            files[id].write(data);
            return data.length;
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file with id: " + id, e);
        }
    }
}
