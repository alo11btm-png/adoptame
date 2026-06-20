package mx.edu.unpa.adoptame.service

import mx.edu.unpa.adoptame.model.Mascota
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface MascotaService {
    @GET("mascota")
    fun getMascotas(): Call<List<Mascota>>

    @GET("mascota/{idMascota}")
    fun getMascota(@Path("idMascota") idMascota: Int): Call<Mascota>

    @GET("mascota/name")
    fun getMascotaByName(@Query("name") name: String): Call<Mascota>

    @POST("mascota")
    fun crearMascota(@Body mascota: Mascota): Call<Mascota>

    @GET("mascota/usuario/{idUsuario}")
    fun getMascotasByUsuario(@Path("idUsuario") idUsuario: Int): Call<List<Mascota>>

    @PATCH("mascota/{idMascota}/estado")
    fun actualizarEstado(
        @Path("idMascota") idMascota: Int,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<Mascota>
}
