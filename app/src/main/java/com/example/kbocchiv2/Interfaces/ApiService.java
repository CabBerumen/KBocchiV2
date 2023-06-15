package com.example.kbocchiv2.Interfaces;

import com.example.kbocchiv2.Request.LoginRequest;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("usuarios/fisioterapeutas/login")
    Call<LoginRequest> login_call(@Field("email") String email, @Field("contrasena") String contrasena);

}
