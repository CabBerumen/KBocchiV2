package POJO;
import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RequestExpediente {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("id_usuario")
    @Expose
    private String idUsuario;
    @SerializedName("has_cita_hoy")
    @Expose
    private Object hasCitaHoy;
    @SerializedName("ultima_cita")
    @Expose
    private List<ResultExpediente> ultimaCita;
    @SerializedName("nombre")
    @Expose
    private String nombre;
    @SerializedName("foto_perfil")
    @Expose
    private String fotoPerfil;
    @SerializedName("telefono")
    @Expose
    private String telefono;

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

    public Object getHasCitaHoy() {
        return hasCitaHoy;
    }

    public void setHasCitaHoy(Object hasCitaHoy) {
        this.hasCitaHoy = hasCitaHoy;
    }

    public List<ResultExpediente> getUltimaCita() {
        return ultimaCita;
    }

    public void setUltimaCita(List<ResultExpediente> ultimaCita) {
        this.ultimaCita = ultimaCita;
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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
