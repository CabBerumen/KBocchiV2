package POJO;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotasBitacora {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("diagnostico")
    @Expose
    private String diagnostico;
    @SerializedName("observaciones")
    @Expose
    private String observaciones;
    @SerializedName("tratamiento")
    @Expose
    private String tratamiento;
    @SerializedName("evolucion")
    @Expose
    private String evolucion;
    @SerializedName("id_cita")
    @Expose
    private Integer idCita;
    @SerializedName("titulo")
    @Expose
    private String titulo;
    @SerializedName("fecha_edicion")
    @Expose
    private String fechaEdicion;
    @SerializedName("fecha_creacion")
    @Expose
    private String fechaCreacion;
    @SerializedName("cita")
    @Expose
    private BitacoraCitas cita;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getEvolucion() {
        return evolucion;
    }

    public void setEvolucion(String evolucion) {
        this.evolucion = evolucion;
    }

    public Integer getIdCita() {
        return idCita;
    }

    public void setIdCita(Integer idCita) {
        this.idCita = idCita;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFechaEdicion() {
        return fechaEdicion;
    }

    public void setFechaEdicion(String fechaEdicion) {
        this.fechaEdicion = fechaEdicion;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public BitacoraCitas getCita() {
        return cita;
    }

    public void setCita(BitacoraCitas cita) {
        this.cita = cita;
    }

    @Override
    public String toString() {
        // replace with your actual properties
        return "NotasBitacora(title=" + titulo + ", date=" + fechaCreacion + ")";
    }

}
