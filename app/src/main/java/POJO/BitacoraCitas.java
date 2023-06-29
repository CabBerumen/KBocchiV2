package POJO;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BitacoraCitas {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("id_terapeuta")
    @Expose
    private Integer idTerapeuta;
    @SerializedName("fecha")
    @Expose
    private String fecha;
    @SerializedName("id_paciente")
    @Expose
    private Integer idPaciente;
    @SerializedName("terapeuta_datos")
    @Expose
    private TerapeutaDatos terapeutaDatos;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdTerapeuta() {
        return idTerapeuta;
    }

    public void setIdTerapeuta(Integer idTerapeuta) {
        this.idTerapeuta = idTerapeuta;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Integer getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Integer idPaciente) {
        this.idPaciente = idPaciente;
    }

    public TerapeutaDatos getTerapeutaDatos() {
        return terapeutaDatos;
    }

    public void setTerapeutaDatos(TerapeutaDatos terapeutaDatos) {
        this.terapeutaDatos = terapeutaDatos;
    }
}
