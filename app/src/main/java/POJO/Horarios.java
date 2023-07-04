package POJO;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Horarios {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("id_terapeuta")
    @Expose
    private Integer idTerapeuta;
    @SerializedName("dia")
    @Expose
    private String dia;
    @SerializedName("hora_inicio")
    @Expose
    private String horaInicio;
    @SerializedName("hora_fin")
    @Expose
    private String horaFin;

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

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }
}
