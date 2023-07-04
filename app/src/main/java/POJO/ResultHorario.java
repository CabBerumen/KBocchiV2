package POJO;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ResultHorario {
    @SerializedName("horario")
    @Expose
    private List<Horarios> horario;

    public List<Horarios> getHorario() {
        return horario;
    }

    public void setHorario(List<Horarios> horario) {
        this.horario = horario;
    }
}
