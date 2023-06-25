package POJO;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultCita {
    @SerializedName("citas")
    @Expose
    private List<RequestCitas> citas;

    public List<RequestCitas> getCitas() {
        return citas;
    }
    public void setCitas(List<RequestCitas> citas) {
        this.citas = citas;
    }
}

