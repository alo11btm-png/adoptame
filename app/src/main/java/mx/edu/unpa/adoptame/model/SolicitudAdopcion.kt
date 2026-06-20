package mx.edu.unpa.adoptame.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SolicitudAdopcion(
    @SerializedName("idSolicitud")
    var idSolicitud: Int? = null,
    var idMascota: Int? = null,
    var nombreMascota: String? = null,
    var idUsuarioSolicitante: Int? = null,
    var nombreSolicitante: String? = null,
    var emailSolicitante: String? = null,
    var estado: String? = null,
    var fechaSolicitud: String? = null
) : Serializable
