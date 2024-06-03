package DaLaw2.FinalProject.Manager.DataClass;

import java.io.Serializable;

public class FileBody implements Serializable {
    public final Long id;
    public final byte[] data;

    public FileBody(Long id, byte[] data) {
        this.id = id;
        this.data = data;
    }


}
