package POJO;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultExpediente {

    @SerializedName("domicilio")
    @Expose
    private String domicilio;
    @SerializedName("fecha")
    @Expose
    private String fecha;
    @SerializedName("id_terapeuta")
    @Expose
    private Integer idTerapeuta;

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Integer getIdTerapeuta() {
        return idTerapeuta;
    }

    public void setIdTerapeuta(Integer idTerapeuta) {
        this.idTerapeuta = idTerapeuta;
    }

}
