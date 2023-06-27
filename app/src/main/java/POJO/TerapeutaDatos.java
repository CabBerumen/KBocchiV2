package POJO;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TerapeutaDatos {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("id_usuario")
    @Expose
    private String idUsuario;
    @SerializedName("usuario")
    @Expose
    private UsuariosBitacora usuario;

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

    public UsuariosBitacora getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuariosBitacora usuario) {
        this.usuario = usuario;
    }
}
