package mx.edu.unpa.adoptame.service

import mx.edu.unpa.adoptame.model.SolicitudAdopcion
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SolicitudAdopcionService {
    @POST("solicitudAdopcion")
    fun crear(@Body body: Map<String, @JvmSuppressWildcards Any>): Call<SolicitudAdopcion>

    @GET("solicitudAdopcion/recibidas/{idUsuario}")
    fun getRecibidas(@Path("idUsuario") idUsuario: Int): Call<List<SolicitudAdopcion>>

    @GET("solicitudAdopcion/enviadas/{idUsuario}")
    fun getEnviadas(@Path("idUsuario") idUsuario: Int): Call<List<SolicitudAdopcion>>

    @PATCH("solicitudAdopcion/{idSolicitud}/aprobar")
    fun aprobar(
        @Path("idSolicitud") idSolicitud: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<SolicitudAdopcion>
}
