package mx.edu.unpa.adoptame.client

import mx.edu.unpa.adoptame.service.ImagenMascotaService
import mx.edu.unpa.adoptame.service.MascotaService
import mx.edu.unpa.adoptame.service.SolicitudAdopcionService
import mx.edu.unpa.adoptame.service.UploadService
import mx.edu.unpa.adoptame.service.UsuarioService
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val BASE_URL = "http://192.168.1.105:8181/"

    private val gson by lazy {
        GsonBuilder()
            .serializeNulls()
            .create()
    }


    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val usuarioService: UsuarioService by lazy {
        retrofit.create(UsuarioService::class.java)
    }

    val mascotaService: MascotaService by lazy {
        retrofit.create(MascotaService::class.java)
    }

    val uploadService: UploadService by lazy {
        retrofit.create(UploadService::class.java)
    }

    val imagenMascotaService: ImagenMascotaService by lazy {
        retrofit.create(ImagenMascotaService::class.java)
    }

    val solicitudAdopcionService: SolicitudAdopcionService by lazy {
        retrofit.create(SolicitudAdopcionService::class.java)
    }

    // Compatibilidad con código existente
    val instance: UsuarioService
        get() = usuarioService
}
