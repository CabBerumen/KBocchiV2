package com.example.kbocchiv2.Interfaces;

import com.example.kbocchiv2.Request.LoginRequest;

import java.util.List;

import POJO.NotasBitacora;
import POJO.RequestBitacora;
import POJO.RequestCitas;
import POJO.RequestExpediente;
import POJO.RequestPacientes;
import POJO.ResultCita;
import POJO.ResultExpediente;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @FormUrlEncoded
    @POST("usuarios/fisioterapeutas/login")
    Call<LoginRequest> login_call(@Field("email") String email, @Field("contrasena") String contrasena);


    @GET("usuarios/fisioterapeutas/pacientes/{id_terapeuta}")
    Call<List<RequestPacientes>> obtenerPacientes(@Path("id_terapeuta") String idTerapeuta);

    @GET("citas/obtenerCitas/{id_terapeuta}")
    Call<ResultCita> obtenerCitas(@Path("id_terapeuta") String idTerapeuta);

    @GET("/usuarios/fisioterapeutas/bitacora/pacientes/{id_terapeuta}")
    Call<List<RequestExpediente>> obtenerExpediente (@Path("id_terapeuta") String idTerapeuta);

    @GET("notas/terapeuta/{id_terapeuta}")
    Call<List<RequestBitacora>> obtenerListaNotas(@Path("id_terapeuta") String idTerapeuta, @Query("id_paciente") String id_Paciente );


}
