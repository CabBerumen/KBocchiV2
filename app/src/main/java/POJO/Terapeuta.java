package POJO;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Terapeuta {
    @SerializedName("domicilio")
    @Expose
    private String domicilio;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("id_usuario")
    @Expose
    private String idUsuario;
    @SerializedName("imagen_de_cedula")
    @Expose
    private Object imagenDeCedula;
    @SerializedName("imagen_de_INE")
    @Expose
    private Object imagenDeINE;
    @SerializedName("imagen_de_rostro")
    @Expose
    private Object imagenDeRostro;
    @SerializedName("lat")
    @Expose
    private Double lat;
    @SerializedName("lng")
    @Expose
    private Double lng;
    @SerializedName("nombre_del_consultorio")
    @Expose
    private String nombreDelConsultorio;
    @SerializedName("numero_cedula")
    @Expose
    private String numeroCedula;
    @SerializedName("pago_maximo")
    @Expose
    private Integer pagoMaximo;
    @SerializedName("pago_minimo")
    @Expose
    private Integer pagoMinimo;
    @SerializedName("rango_servicio")
    @Expose
    private Integer rangoServicio;
    @SerializedName("servicio_domicilio")
    @Expose
    private Integer servicioDomicilio;

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Object getImagenDeCedula() {
        return imagenDeCedula;
    }

    public void setImagenDeCedula(Object imagenDeCedula) {
        this.imagenDeCedula = imagenDeCedula;
    }

    public Object getImagenDeINE() {
        return imagenDeINE;
    }

    public void setImagenDeINE(Object imagenDeINE) {
        this.imagenDeINE = imagenDeINE;
    }

    public Object getImagenDeRostro() {
        return imagenDeRostro;
    }

    public void setImagenDeRostro(Object imagenDeRostro) {
        this.imagenDeRostro = imagenDeRostro;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getNombreDelConsultorio() {
        return nombreDelConsultorio;
    }

    public void setNombreDelConsultorio(String nombreDelConsultorio) {
        this.nombreDelConsultorio = nombreDelConsultorio;
    }

    public String getNumeroCedula() {
        return numeroCedula;
    }

    public void setNumeroCedula(String numeroCedula) {
        this.numeroCedula = numeroCedula;
    }

    public Integer getPagoMaximo() {
        return pagoMaximo;
    }

    public void setPagoMaximo(Integer pagoMaximo) {
        this.pagoMaximo = pagoMaximo;
    }

    public Integer getPagoMinimo() {
        return pagoMinimo;
    }

    public void setPagoMinimo(Integer pagoMinimo) {
        this.pagoMinimo = pagoMinimo;
    }

    public Integer getRangoServicio() {
        return rangoServicio;
    }

    public void setRangoServicio(Integer rangoServicio) {
        this.rangoServicio = rangoServicio;
    }

    public Integer getServicioDomicilio() {
        return servicioDomicilio;
    }

    public void setServicioDomicilio(Integer servicioDomicilio) {
        this.servicioDomicilio = servicioDomicilio;
    }
}
