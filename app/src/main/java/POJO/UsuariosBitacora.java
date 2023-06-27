package POJO;
import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UsuariosBitacora {
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("rol")
    @Expose
    private String rol;
    @SerializedName("nombre")
    @Expose
    private String nombre;
    @SerializedName("foto_perfil")
    @Expose
    private String fotoPerfil;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
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
