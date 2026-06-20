package mx.edu.unpa.adoptame

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import mx.edu.unpa.adoptame.adapter.GalleryImageAdapter
import mx.edu.unpa.adoptame.client.RetrofitClient
import mx.edu.unpa.adoptame.model.GalleryImageItem
import mx.edu.unpa.adoptame.model.Mascota
import mx.edu.unpa.adoptame.util.AdoptionRequestHelper
import mx.edu.unpa.adoptame.util.PetGalleryHelper

class PetGalleryActivity : AppCompatActivity() {

    private lateinit var galleryAdapter: GalleryImageAdapter
    private lateinit var btnAdopt: Button
    private var currentPet: Mascota? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pet_gallery)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val idMascota = intent.getIntExtra(EXTRA_MASCOTA_ID, -1)
        val nombre = intent.getStringExtra(EXTRA_MASCOTA_NOMBRE).orEmpty().ifBlank { getString(R.string.app_brand) }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.pet_gallery_title_format, nombre)
        toolbar.setNavigationOnClickListener { finish() }

        btnAdopt = findViewById(R.id.btnAdopt)
        btnAdopt.setOnClickListener {
            currentPet?.let { pet ->
                AdoptionRequestHelper.submit(this, pet) {
                    loadPetFromApi(pet.idMascota ?: idMascota, pet.nombre.orEmpty())
                }
            }
        }

        galleryAdapter = GalleryImageAdapter(emptyList()) { item -> openImageViewer(item) }
        findViewById<RecyclerView>(R.id.recyclerGallery).apply {
            layoutManager = GridLayoutManager(this@PetGalleryActivity, 2)
            adapter = galleryAdapter
        }

        if (idMascota > 0) {
            loadPetFromApi(idMascota, nombre)
        } else {
            showGallery(Mascota(nombre = nombre, tipo = "Gato"))
        }
    }

    private fun currentUserId(): Int {
        return getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
            .getInt(MainActivity.KEY_USER_ID, -1)
    }

    private fun loadPetFromApi(idMascota: Int, nombre: String) {
        if (idMascota <= 0) {
            showGallery(Mascota(nombre = nombre, tipo = "Gato"))
            return
        }
        RetrofitClient.mascotaService.getMascota(idMascota).enqueue(object : retrofit2.Callback<Mascota> {
            override fun onResponse(call: retrofit2.Call<Mascota>, response: retrofit2.Response<Mascota>) {
                if (response.isSuccessful && response.body() != null) {
                    showGallery(response.body()!!)
                } else {
                    Log.w(TAG, "No se pudo cargar mascota $idMascota: ${response.code()}")
                    Toast.makeText(
                        this@PetGalleryActivity,
                        getString(R.string.msg_pet_load_error, response.code()),
                        Toast.LENGTH_SHORT
                    ).show()
                    showGallery(Mascota(idMascota = idMascota, nombre = nombre, tipo = "Gato"))
                }
            }

            override fun onFailure(call: retrofit2.Call<Mascota>, t: Throwable) {
                Log.e(TAG, "Error cargando mascota $idMascota: ${t.message}", t)
                Toast.makeText(this@PetGalleryActivity, R.string.msg_connection_error, Toast.LENGTH_SHORT).show()
                showGallery(Mascota(idMascota = idMascota, nombre = nombre, tipo = "Gato"))
            }
        })
    }

    private fun showGallery(pet: Mascota) {
        currentPet = pet
        val items = PetGalleryHelper.buildGalleryItems(pet)
        galleryAdapter.updateList(items)
        if (items.isEmpty()) {
            Toast.makeText(this, R.string.msg_no_photos, Toast.LENGTH_SHORT).show()
        }
        val canAdopt = AdoptionRequestHelper.canRequestAdoption(pet, currentUserId())
        btnAdopt.visibility = if (canAdopt) View.VISIBLE else View.GONE
    }

    private fun openImageViewer(item: GalleryImageItem) {
        startActivity(android.content.Intent(this, PetImageViewerActivity::class.java).apply {
            putExtra(PetImageViewerActivity.EXTRA_IMAGE_URL, item.url)
            item.drawableRes?.let { putExtra(PetImageViewerActivity.EXTRA_DRAWABLE_RES, it) }
        })
    }

    companion object {
        const val EXTRA_MASCOTA_ID = "mascota_id"
        const val EXTRA_MASCOTA_NOMBRE = "mascota_nombre"
        private const val TAG = "PET_GALLERY"
    }
}
