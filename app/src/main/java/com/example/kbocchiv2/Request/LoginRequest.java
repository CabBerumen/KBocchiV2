package com.example.kbocchiv2.Request;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import POJO.Terapeuta;

public class LoginRequest implements Serializable {

    @SerializedName("cambioContrasena")
    @Expose
    private Integer cambioContrasena;
    @SerializedName("contrasena")
    @Expose
    private String contrasena;
    @SerializedName("cuenta_bloqueada")
    @Expose
    private Integer cuentaBloqueada;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("foto_perfil")
    @Expose
    private String fotoPerfil;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("intentos_fallidos")
    @Expose
    private Integer intentosFallidos;
    @SerializedName("nombre")
    @Expose
    private String nombre;
    @SerializedName("rol")
    @Expose
    private String rol;
    @SerializedName("telefono")
    @Expose
    private String telefono;
    @SerializedName("terapeuta")
    @Expose
    private Terapeuta terapeuta;

    public Integer getCambioContrasena() {
        return cambioContrasena;
    }

    public void setCambioContrasena(Integer cambioContrasena) {
        this.cambioContrasena = cambioContrasena;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Integer getCuentaBloqueada() {
        return cuentaBloqueada;
    }

    public void setCuentaBloqueada(Integer cuentaBloqueada) {
        this.cuentaBloqueada = cuentaBloqueada;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Terapeuta getTerapeuta() {
        return terapeuta;
    }

    public void setTerapeuta(Terapeuta terapeuta) {
        this.terapeuta = terapeuta;
    }

}
