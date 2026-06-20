package mx.edu.unpa.adoptame.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.unpa.adoptame.R
import mx.edu.unpa.adoptame.model.SolicitudAdopcion

class AdoptionRequestAdapter(
    private var requests: MutableList<SolicitudAdopcion> = mutableListOf(),
    private val showApproveButton: Boolean = false,
    private val onApproveClick: (SolicitudAdopcion, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<AdoptionRequestAdapter.RequestViewHolder>() {

    fun updateList(newRequests: List<SolicitudAdopcion>) {
        requests = newRequests.toMutableList()
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        if (position in requests.indices) {
            requests.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_adoption_request, parent, false)
        return RequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        holder.bind(requests[position], position, showApproveButton, onApproveClick)
    }

    override fun getItemCount(): Int = requests.size

    class RequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtPetName: TextView = itemView.findViewById(R.id.txtPetName)
        private val txtDetail: TextView = itemView.findViewById(R.id.txtDetail)
        private val txtStatus: TextView = itemView.findViewById(R.id.txtStatus)
        private val btnApprove: Button = itemView.findViewById(R.id.btnApprove)

        fun bind(
            request: SolicitudAdopcion,
            position: Int,
            showApproveButton: Boolean,
            onApproveClick: (SolicitudAdopcion, Int) -> Unit
        ) {
            val context = itemView.context
            txtPetName.text = request.nombreMascota.orEmpty().ifBlank { "—" }

            if (showApproveButton) {
                val solicitante = request.nombreSolicitante.orEmpty().ifBlank { "—" }
                val email = request.emailSolicitante.orEmpty()
                txtDetail.text = if (email.isNotBlank()) {
                    context.getString(R.string.adoption_request_from_format, solicitante, email)
                } else {
                    solicitante
                }
                txtStatus.visibility = View.GONE
                btnApprove.visibility = View.VISIBLE
                btnApprove.setOnClickListener { onApproveClick(request, position) }
            } else {
                txtDetail.text = context.getString(
                    R.string.adoption_request_sent_date_format,
                    request.fechaSolicitud.orEmpty().ifBlank { "—" }
                )
                txtStatus.text = request.estado.orEmpty().ifBlank { "Pendiente" }
                txtStatus.visibility = View.VISIBLE
                btnApprove.visibility = View.GONE
            }
        }
    }
}
