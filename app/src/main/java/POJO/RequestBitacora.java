package POJO;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestBitacora {
    @SerializedName("2023-06-26")
    @Expose
    private List<NotasBitacora> NotasBitacora;

    public List<NotasBitacora> getNotasBitacora() {
        return NotasBitacora;
    }

    public void set20230626(List<NotasBitacora> NotasBitacora) {
        this.NotasBitacora = NotasBitacora;
    }
}
