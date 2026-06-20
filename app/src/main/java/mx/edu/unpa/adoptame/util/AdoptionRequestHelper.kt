package mx.edu.unpa.adoptame.util

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import mx.edu.unpa.adoptame.MainActivity
import mx.edu.unpa.adoptame.R
import mx.edu.unpa.adoptame.client.RetrofitClient
import mx.edu.unpa.adoptame.model.Mascota
import mx.edu.unpa.adoptame.model.SolicitudAdopcion
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object AdoptionRequestHelper {

    fun canRequestAdoption(
        pet: Mascota,
        currentUserId: Int,
        cachedApiPets: List<Mascota> = emptyList()
    ): Boolean {
        if (currentUserId <= 0) return false
        if (PetListMerger.isOwner(pet, currentUserId, cachedApiPets)) return false
        return !pet.estadoAdopcion.equals("Adoptado", ignoreCase = true)
    }

    fun submit(
        activity: AppCompatActivity,
        pet: Mascota,
        cachedApiPets: List<Mascota> = emptyList(),
        onSuccess: (() -> Unit)? = null
    ) {
        val userId = currentUserId(activity)
        if (userId <= 0) {
            Toast.makeText(activity, R.string.msg_session_expired, Toast.LENGTH_SHORT).show()
            return
        }
        if (!canRequestAdoption(pet, userId, cachedApiPets)) {
            if (PetListMerger.isOwner(pet, userId, cachedApiPets)) {
                Toast.makeText(activity, R.string.msg_adoption_own_pet, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, R.string.msg_adoption_not_available, Toast.LENGTH_SHORT).show()
            }
            return
        }

        val idMascota = pet.idMascota?.takeIf { it > 0 }
            ?: PetListMerger.findIdInApi(pet, cachedApiPets)

        if (idMascota != null) {
            sendRequest(activity, idMascota, userId, onSuccess)
            return
        }

        val nombre = pet.nombre?.trim().orEmpty()
        if (nombre.isEmpty()) {
            Toast.makeText(activity, R.string.msg_status_offline, Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.mascotaService.getMascotaByName(nombre).enqueue(object : Callback<Mascota> {
            override fun onResponse(call: Call<Mascota>, response: Response<Mascota>) {
                val id = response.body()?.idMascota
                if (response.isSuccessful && id != null && id > 0) {
                    sendRequest(activity, id, userId, onSuccess)
                } else {
                    Toast.makeText(activity, R.string.msg_status_offline, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Mascota>, t: Throwable) {
                Toast.makeText(activity, R.string.msg_connection_error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendRequest(
        activity: AppCompatActivity,
        idMascota: Int,
        userId: Int,
        onSuccess: (() -> Unit)?
    ) {
        RetrofitClient.solicitudAdopcionService.crear(
            mapOf(
                "idMascota" to idMascota,
                "idUsuarioSolicitante" to userId
            )
        ).enqueue(object : Callback<SolicitudAdopcion> {
            override fun onResponse(call: Call<SolicitudAdopcion>, response: Response<SolicitudAdopcion>) {
                when {
                    response.isSuccessful -> {
                        Toast.makeText(activity, R.string.msg_adoption_requested, Toast.LENGTH_SHORT).show()
                        onSuccess?.invoke()
                    }
                    response.code() == 409 -> {
                        Toast.makeText(activity, R.string.msg_adoption_already_pending, Toast.LENGTH_SHORT).show()
                    }
                    response.code() == 404 -> {
                        Toast.makeText(activity, R.string.msg_adoption_endpoint_missing, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(
                            activity,
                            activity.getString(R.string.msg_adoption_request_error, response.code()),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<SolicitudAdopcion>, t: Throwable) {
                Toast.makeText(activity, R.string.msg_connection_error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun currentUserId(context: Context): Int {
        return context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(MainActivity.KEY_USER_ID, -1)
    }
}
