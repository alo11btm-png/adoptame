package mx.edu.unpa.adoptame

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import mx.edu.unpa.adoptame.adapter.CategoryAdapter
import mx.edu.unpa.adoptame.util.CategoryMapper
import mx.edu.unpa.adoptame.util.PetTipoToast

class DashboardActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE)

        window.statusBarColor = ContextCompat.getColor(this, R.color.adoptame_toolbar_background)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val categories = CategoryMapper.dashboardCategories(this)
        findViewById<RecyclerView>(R.id.recyclerCategories).apply {
            layoutManager = GridLayoutManager(this@DashboardActivity, 2)
            adapter = CategoryAdapter(categories) { category ->
                PetTipoToast.show(this@DashboardActivity, category.name)
                openPetList(category.name)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_pet -> {
                startActivity(Intent(this, RegisterPetActivity::class.java).apply {
                    putExtra(RegisterPetActivity.EXTRA_TIPO, "Gato")
                })
                true
            }
            R.id.action_more -> {
                val anchor = toolbar.findViewById<View>(R.id.action_more) ?: toolbar
                showOverflowMenu(anchor)
                true
            }
            else -> handleMenuAction(item.itemId) || super.onOptionsItemSelected(item)
        }
    }

    private fun openPetList(tipoTarjeta: String) {
        val intent = Intent(this, PetListActivity::class.java)
        intent.putExtra(PetListActivity.EXTRA_TITULO, tipoTarjeta)
        startActivity(intent)
    }

    private fun showOverflowMenu(anchor: View) {
        val popup = PopupMenu(this, anchor)
        popup.menuInflater.inflate(R.menu.menu_dashboard, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            handleMenuAction(item.itemId)
        }
        popup.show()
    }

    private fun handleMenuAction(itemId: Int): Boolean {
        return when (itemId) {
            R.id.action_my_publications -> {
                startActivity(Intent(this, PetListActivity::class.java).apply {
                    putExtra(PetListActivity.EXTRA_MODE, PetListActivity.MODE_MINE)
                })
                true
            }
            R.id.action_adoption_requests_received -> {
                startActivity(Intent(this, AdoptionRequestsActivity::class.java).apply {
                    putExtra(AdoptionRequestsActivity.EXTRA_MODE, AdoptionRequestsActivity.MODE_RECEIVED)
                })
                true
            }
            R.id.action_adoption_requests_sent -> {
                startActivity(Intent(this, AdoptionRequestsActivity::class.java).apply {
                    putExtra(AdoptionRequestsActivity.EXTRA_MODE, AdoptionRequestsActivity.MODE_SENT)
                })
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_logout -> {
                prefs.edit().clear().apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> false
        }
    }
}
