package hitech.example.admin.volleyrequests;

/**
 * Created by Admin on 2016-11-23.
 */

public class ImgDataHolder {

    public static String name;
    byte[] dataStored = null;
    public ImgDataHolder(byte[] data) {
        dataStored = data;
    }

    public byte[] getDataStored() {
        return dataStored;
    }
}
