package mx.edu.unpa.adoptame

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import mx.edu.unpa.adoptame.adapter.PetAdapter
import mx.edu.unpa.adoptame.client.RetrofitClient
import mx.edu.unpa.adoptame.model.Mascota
import mx.edu.unpa.adoptame.util.AdoptionRequestHelper
import mx.edu.unpa.adoptame.util.PetListMerger
import mx.edu.unpa.adoptame.util.PetTipoToast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PetListActivity : AppCompatActivity() {
    private lateinit var adapter: PetAdapter
    private var tituloTarjeta: String = "Gato"
    private var listMode: String = MODE_ALL
    private var cachedApiPets: List<Mascota> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pet_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tituloTarjeta = intent.getStringExtra(EXTRA_TITULO)
            ?: intent.getStringExtra(EXTRA_TIPO)
            ?: "Gato"
        listMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_ALL

        window.statusBarColor = ContextCompat.getColor(this, R.color.adoptame_toolbar_background)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.adoptame_toolbar_background))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (listMode == MODE_MINE) {
            getString(R.string.my_publications_title)
        } else {
            getString(R.string.pet_list_title_format, tituloTarjeta)
        }
        toolbar.setNavigationOnClickListener { finish() }

        adapter = PetAdapter(
            currentUserId = currentUserId(),
            adoptButtonsEnabled = listMode != MODE_MINE,
            onPetClick = { pet ->
                val nombre = pet.nombre.orEmpty().ifBlank { getString(R.string.app_brand) }
                PetTipoToast.show(this, nombre)
                openPetGallery(pet)
            },
            onBadgeClick = { pet, position -> showEstadoDialog(pet, position) },
            onAdoptClick = { pet ->
                AdoptionRequestHelper.submit(this, pet, cachedApiPets) {
                    loadPets()
                }
            }
        )
        findViewById<RecyclerView>(R.id.recyclerPets).apply {
            layoutManager = LinearLayoutManager(this@PetListActivity)
            adapter = this@PetListActivity.adapter
        }

        loadPets()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            loadPets()
        }
    }

    private fun currentUserId(): Int {
        return getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
            .getInt(MainActivity.KEY_USER_ID, -1)
    }

    private fun loadPets() {
        val userId = currentUserId()
        if (listMode == MODE_MINE && userId <= 0) {
            Toast.makeText(this, R.string.msg_session_expired, Toast.LENGTH_SHORT).show()
            adapter.updateList(emptyList())
            return
        }

        val call = if (listMode == MODE_MINE) {
            RetrofitClient.mascotaService.getMascotasByUsuario(userId)
        } else {
            RetrofitClient.mascotaService.getMascotas()
        }

        call.enqueue(object : Callback<List<Mascota>> {
            override fun onResponse(call: Call<List<Mascota>>, response: Response<List<Mascota>>) {
                if (response.isSuccessful) {
                    cachedApiPets = response.body() ?: emptyList()
                    adapter.setCurrentUserId(userId)
                    adapter.setCachedApiPets(cachedApiPets)
                    val pets = buildDisplayList(userId)
                    adapter.updateList(pets)
                    if (listMode == MODE_MINE && pets.isEmpty()) {
                        Toast.makeText(
                            this@PetListActivity,
                            R.string.msg_no_my_publications,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.e(TAG, "Error cargando mascotas: ${response.code()}")
                    cachedApiPets = emptyList()
                    adapter.updateList(buildDisplayList(userId))
                    Toast.makeText(
                        this@PetListActivity,
                        getString(R.string.msg_pet_load_error, response.code()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Mascota>>, t: Throwable) {
                Log.e(TAG, "Error cargando mascotas: ${t.message}", t)
                cachedApiPets = emptyList()
                adapter.updateList(buildDisplayList(userId))
                Toast.makeText(this@PetListActivity, R.string.msg_connection_error, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun buildDisplayList(userId: Int): List<Mascota> {
        return if (listMode == MODE_MINE) {
            PetListMerger.mergeMyPublications(cachedApiPets, userId, FILTER_GATO)
        } else {
            PetListMerger.mergeForDisplay(cachedApiPets, FILTER_GATO)
        }
    }

    private fun openPetGallery(pet: Mascota) {
        val idMascota = PetListMerger.findIdInApi(pet, cachedApiPets)
        if (idMascota == null) {
            Toast.makeText(this, R.string.msg_no_photos, Toast.LENGTH_SHORT).show()
            return
        }
        startActivity(Intent(this, PetGalleryActivity::class.java).apply {
            putExtra(PetGalleryActivity.EXTRA_MASCOTA_ID, idMascota)
            putExtra(PetGalleryActivity.EXTRA_MASCOTA_NOMBRE, pet.nombre.orEmpty())
        })
    }

    private fun showEstadoDialog(pet: Mascota, position: Int) {
        if (!PetListMerger.isOwner(pet, currentUserId(), cachedApiPets)) {
            Toast.makeText(this, R.string.msg_status_not_owner, Toast.LENGTH_SHORT).show()
            return
        }

        val idMascota = PetListMerger.findIdInApi(pet, cachedApiPets)
        if (idMascota != null) {
            openEstadoDialog(pet, position, idMascota)
            return
        }

        val nombre = pet.nombre?.trim().orEmpty()
        if (nombre.isEmpty()) {
            Toast.makeText(this, R.string.msg_status_offline, Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.mascotaService.getMascotaByName(nombre).enqueue(object : Callback<Mascota> {
            override fun onResponse(call: Call<Mascota>, response: Response<Mascota>) {
                val id = response.body()?.idMascota
                if (response.isSuccessful && id != null && id > 0) {
                    openEstadoDialog(pet, position, id)
                } else {
                    Toast.makeText(this@PetListActivity, R.string.msg_status_offline, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Mascota>, t: Throwable) {
                Toast.makeText(this@PetListActivity, R.string.msg_connection_error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openEstadoDialog(pet: Mascota, position: Int, idMascota: Int) {
        val opciones = resources.getStringArray(R.array.pet_estado_options)
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_change_status_title))
            .setItems(opciones) { _, which ->
                val nuevoEstado = opciones[which]
                if (nuevoEstado.equals(pet.estadoAdopcion, ignoreCase = true)) {
                    return@setItems
                }
                actualizarEstado(pet, position, idMascota, nuevoEstado)
            }
            .show()
    }

    private fun actualizarEstado(pet: Mascota, position: Int, idMascota: Int, nuevoEstado: String) {
        val userId = currentUserId()
        RetrofitClient.mascotaService
            .actualizarEstado(
                idMascota,
                mapOf("estadoAdopcion" to nuevoEstado, "idUsuario" to userId)
            )
            .enqueue(object : Callback<Mascota> {
                override fun onResponse(call: Call<Mascota>, response: Response<Mascota>) {
                    if (response.isSuccessful && response.body() != null) {
                        val updated = response.body()!!
                        cachedApiPets = cachedApiPets.map { pet ->
                            if (pet.idMascota == idMascota ||
                                pet.nombre.equals(updated.nombre, ignoreCase = true)
                            ) {
                                updated
                            } else {
                                pet
                            }
                        }
                        adapter.updatePetAt(position, updated)
                        Toast.makeText(this@PetListActivity, R.string.msg_status_updated, Toast.LENGTH_SHORT).show()
                    } else if (response.code() == 403) {
                        Toast.makeText(
                            this@PetListActivity,
                            R.string.msg_status_not_owner,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Log.e(TAG, "Error actualizando estado: ${response.code()}")
                        Toast.makeText(
                            this@PetListActivity,
                            getString(R.string.msg_status_update_error, response.code()),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<Mascota>, t: Throwable) {
                    Log.e(TAG, "Error actualizando estado: ${t.message}", t)
                    Toast.makeText(this@PetListActivity, R.string.msg_connection_error, Toast.LENGTH_SHORT).show()
                }
            })
    }

    companion object {
        const val EXTRA_TITULO = "titulo"
        const val EXTRA_DISPONIBLE = "disponible"
        const val EXTRA_TIPO = "tipo"
        const val EXTRA_MODE = "mode"
        const val MODE_ALL = "all"
        const val MODE_MINE = "mine"
        private const val FILTER_GATO = "Gato"
        private const val TAG = "PET_LIST"
    }
}
