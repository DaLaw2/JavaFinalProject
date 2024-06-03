package DaLaw2.FinalProject.Manager.DataClass;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class FileBody implements Serializable {
    public final Long id;
    public final byte[] data;

    public FileBody(Long id, byte[] data) {
        this.id = id;
        this.data = data;
    }


    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(stream);
        out.writeObject(this);
        return stream.toByteArray();
    }
}
