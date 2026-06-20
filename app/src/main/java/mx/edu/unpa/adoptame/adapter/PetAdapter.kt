package mx.edu.unpa.adoptame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.adoptame.R
import mx.edu.unpa.adoptame.model.Mascota
import mx.edu.unpa.adoptame.util.AdoptionRequestHelper
import mx.edu.unpa.adoptame.util.GlideImageLoader

class PetAdapter(
    private var pets: MutableList<Mascota> = mutableListOf(),
    private var currentUserId: Int = -1,
    private var cachedApiPets: List<Mascota> = emptyList(),
    private var adoptButtonsEnabled: Boolean = true,
    private val onPetClick: (Mascota) -> Unit = {},
    private val onBadgeClick: (Mascota, Int) -> Unit = { _, _ -> },
    private val onAdoptClick: (Mascota) -> Unit = {}
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    fun updateList(newPets: List<Mascota>) {
        pets = newPets.toMutableList()
        notifyDataSetChanged()
    }

    fun setCurrentUserId(userId: Int) {
        currentUserId = userId
        notifyDataSetChanged()
    }

    fun setCachedApiPets(apiPets: List<Mascota>) {
        cachedApiPets = apiPets
        notifyDataSetChanged()
    }

    fun updatePetAt(position: Int, pet: Mascota) {
        if (position in pets.indices) {
            pets[position] = pet
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pet, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        holder.bind(
            pets[position],
            position,
            currentUserId,
            cachedApiPets,
            adoptButtonsEnabled,
            onPetClick,
            onBadgeClick,
            onAdoptClick
        )
    }

    override fun getItemCount(): Int = pets.size

    class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgPet: ImageView = itemView.findViewById(R.id.imgPet)
        private val txtName: TextView = itemView.findViewById(R.id.txtPetName)
        private val txtType: TextView = itemView.findViewById(R.id.txtPetType)
        private val txtBreed: TextView = itemView.findViewById(R.id.txtPetBreed)
        private val txtSex: TextView = itemView.findViewById(R.id.txtPetSex)
        private val txtStatus: TextView = itemView.findViewById(R.id.txtPetStatus)
        private val txtBadge: TextView = itemView.findViewById(R.id.txtPetBadge)
        private val btnAdopt: android.widget.Button = itemView.findViewById(R.id.btnAdopt)

        fun bind(
            pet: Mascota,
            position: Int,
            currentUserId: Int,
            cachedApiPets: List<Mascota>,
            adoptButtonsEnabled: Boolean,
            onPetClick: (Mascota) -> Unit,
            onBadgeClick: (Mascota, Int) -> Unit,
            onAdoptClick: (Mascota) -> Unit
        ) {
            GlideImageLoader.load(imgPet, pet.mainImageUrl(), R.drawable.imagen_gatito)
            txtName.text = pet.nombre.orEmpty()
            txtType.text = pet.tipo ?: "Gato"
            txtBreed.text = itemView.context.getString(
                R.string.pet_format_breed,
                pet.raza.orEmpty().ifBlank { "—" }
            )
            txtSex.text = itemView.context.getString(
                R.string.pet_format_sex,
                pet.sexo.orEmpty().ifBlank { "—" }
            )
            val estado = pet.estadoAdopcion.orEmpty().ifBlank { "Disponible" }
            txtStatus.text = itemView.context.getString(R.string.pet_format_status, estado)
            txtBadge.text = estado
            itemView.setOnClickListener { onPetClick(pet) }
            txtBadge.setOnClickListener { onBadgeClick(pet, position) }

            val canAdopt = adoptButtonsEnabled &&
                AdoptionRequestHelper.canRequestAdoption(pet, currentUserId, cachedApiPets)
            btnAdopt.visibility = if (canAdopt) View.VISIBLE else View.GONE
            btnAdopt.setOnClickListener { onAdoptClick(pet) }
        }
    }
}
