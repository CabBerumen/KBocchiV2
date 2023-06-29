package POJO;
import java.util.List;
import javax.annotation.Generated;

import com.example.kbocchiv2.Notas;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestBitacora {

    @SerializedName("header")
    @Expose
    private String header;
    @SerializedName("notas")
    @Expose
    private List<NotasBitacora> notas;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public List<NotasBitacora> getNotas() {
        return notas;
    }

    public void setNotas(List<NotasBitacora> notas) {
        this.notas = notas;
    }
}
