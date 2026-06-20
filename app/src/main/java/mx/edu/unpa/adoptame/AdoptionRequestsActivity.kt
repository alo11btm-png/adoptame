package mx.edu.unpa.adoptame

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import mx.edu.unpa.adoptame.adapter.AdoptionRequestAdapter
import mx.edu.unpa.adoptame.client.RetrofitClient
import mx.edu.unpa.adoptame.model.SolicitudAdopcion
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdoptionRequestsActivity : AppCompatActivity() {

    private lateinit var adapter: AdoptionRequestAdapter
    private var listMode: String = MODE_RECEIVED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_adoption_requests)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listMode = intent.getStringExtra(EXTRA_MODE) ?: MODE_RECEIVED

        window.statusBarColor = ContextCompat.getColor(this, R.color.adoptame_toolbar_background)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.adoptame_toolbar_background))
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (listMode == MODE_RECEIVED) {
            getString(R.string.adoption_requests_received_title)
        } else {
            getString(R.string.adoption_requests_sent_title)
        }
        toolbar.setNavigationOnClickListener { finish() }

        val showApprove = listMode == MODE_RECEIVED
        adapter = AdoptionRequestAdapter(
            showApproveButton = showApprove,
            onApproveClick = { request, position -> approveRequest(request, position) }
        )
        findViewById<RecyclerView>(R.id.recyclerRequests).apply {
            layoutManager = LinearLayoutManager(this@AdoptionRequestsActivity)
            adapter = this@AdoptionRequestsActivity.adapter
        }

        loadRequests()
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            loadRequests()
        }
    }

    private fun currentUserId(): Int {
        return getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)
            .getInt(MainActivity.KEY_USER_ID, -1)
    }

    private fun loadRequests() {
        val userId = currentUserId()
        if (userId <= 0) {
            Toast.makeText(this, R.string.msg_session_expired, Toast.LENGTH_SHORT).show()
            adapter.updateList(emptyList())
            return
        }

        val call = if (listMode == MODE_RECEIVED) {
            RetrofitClient.solicitudAdopcionService.getRecibidas(userId)
        } else {
            RetrofitClient.solicitudAdopcionService.getEnviadas(userId)
        }

        call.enqueue(object : Callback<List<SolicitudAdopcion>> {
            override fun onResponse(
                call: Call<List<SolicitudAdopcion>>,
                response: Response<List<SolicitudAdopcion>>
            ) {
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    adapter.updateList(items)
                    if (items.isEmpty()) {
                        val msg = if (listMode == MODE_RECEIVED) {
                            R.string.msg_no_adoption_requests_received
                        } else {
                            R.string.msg_no_adoption_requests_sent
                        }
                        Toast.makeText(this@AdoptionRequestsActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e(TAG, "Error cargando solicitudes: ${response.code()}")
                    adapter.updateList(emptyList())
                    Toast.makeText(
                        this@AdoptionRequestsActivity,
                        getString(R.string.msg_adoption_load_error, response.code()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<SolicitudAdopcion>>, t: Throwable) {
                Log.e(TAG, "Error cargando solicitudes: ${t.message}", t)
                adapter.updateList(emptyList())
                Toast.makeText(this@AdoptionRequestsActivity, R.string.msg_connection_error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun approveRequest(request: SolicitudAdopcion, position: Int) {
        val idSolicitud = request.idSolicitud
        if (idSolicitud == null || idSolicitud <= 0) {
            Toast.makeText(this, R.string.msg_adoption_approve_error, Toast.LENGTH_SHORT).show()
            return
        }

        val userId = currentUserId()
        RetrofitClient.solicitudAdopcionService.aprobar(
            idSolicitud,
            mapOf("idUsuario" to userId)
        ).enqueue(object : Callback<SolicitudAdopcion> {
            override fun onResponse(call: Call<SolicitudAdopcion>, response: Response<SolicitudAdopcion>) {
                if (response.isSuccessful) {
                    adapter.removeAt(position)
                    Toast.makeText(this@AdoptionRequestsActivity, R.string.msg_adoption_approved, Toast.LENGTH_SHORT).show()
                } else if (response.code() == 403) {
                    Toast.makeText(this@AdoptionRequestsActivity, R.string.msg_status_not_owner, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this@AdoptionRequestsActivity,
                        getString(R.string.msg_adoption_approve_error_code, response.code()),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SolicitudAdopcion>, t: Throwable) {
                Toast.makeText(this@AdoptionRequestsActivity, R.string.msg_connection_error, Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        const val EXTRA_MODE = "mode"
        const val MODE_RECEIVED = "received"
        const val MODE_SENT = "sent"
        private const val TAG = "ADOPTION_REQUESTS"
    }
}
