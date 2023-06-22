package com.example.kbocchiv2.Interfaces;

import com.example.kbocchiv2.Request.LoginRequest;

import java.util.List;

import POJO.RequestPacientes;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @FormUrlEncoded
    @POST("usuarios/fisioterapeutas/login")
    Call<LoginRequest> login_call(@Field("email") String email, @Field("contrasena") String contrasena);

    @GET("usuarios/fisioterapeutas/pacientes/{id_terapeutas}")
    Call<List<RequestPacientes>> obtenerPacientes(@Path("id_terapeutas") int idTerapeuta);


}
