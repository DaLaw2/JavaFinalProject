package DaLaw2.FinalProject.Utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Convertor {
    public static byte[] arrayListToByteArray(ArrayList<Long> list) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * list.size());
        for (long value : list)
            buffer.putLong(value);
        return buffer.array();
    }

    public static ArrayList<Long> arrayListFromByteArray(byte[] bytes) {
        ArrayList<Long> list = new ArrayList<>();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        while (buffer.hasRemaining())
            list.add(buffer.getLong());
        return list;
    }

    public static long bytesToLong(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        return buffer.getLong();
    }
}
