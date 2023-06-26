package POJO;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestCitas {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("lng")
    @Expose
    private Double lng;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("id_paciente")
    @Expose
    private Integer idPaciente;
    @SerializedName("id_terapeuta")
    @Expose
    private Integer idTerapeuta;
    @SerializedName("fecha")
    @Expose
    private String fecha;
    @SerializedName("modalidad")
    @Expose
    private String modalidad;
    @SerializedName("domicilio")
    @Expose
    private String domicilio;
    @SerializedName("nombre")
    @Expose
    private String nombre;
    @SerializedName("foto_perfil")
    @Expose
    private String fotoPerfil;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Integer getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(Integer idPaciente) {
        this.idPaciente = idPaciente;
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

    public String getModalidad() {
        return modalidad;
    }

    public void setModalidad(String modalidad) {
        this.modalidad = modalidad;
    }

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }


}
