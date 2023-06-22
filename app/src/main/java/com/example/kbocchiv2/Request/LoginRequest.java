package com.example.kbocchiv2.Request;

public class LoginRequest {

    private String email;
    private String contrasena;

    private String token;


    public LoginRequest(String email, String contrasena) {
        this.email = email;
        this.contrasena = contrasena;
    }
    public String getUsername() {
        return email;
    }

    public void setUsername(String username) {
        this.email = username;
    }

    public String getPassword() {
        return contrasena;
    }

    public void setPassword(String password) {
        this.contrasena = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


}
