package tn.issatso.clubs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReservationAdapter(
    private val reservations: MutableList<Reservation>,
    private val canCancel: (Reservation) -> Boolean,
    private val onCancel: (Reservation) -> Unit
) : RecyclerView.Adapter<ReservationAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNom: TextView = view.findViewById(R.id.tvResNom)
        val tvAtelier: TextView = view.findViewById(R.id.tvResAtelier)
        val tvStatut: TextView = view.findViewById(R.id.tvResStatut)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reservation, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val reservation = reservations[position]
        holder.tvNom.text = "${reservation.prenom} ${reservation.nom}"
        holder.tvAtelier.text = reservation.atelier.nom
        holder.tvStatut.text = reservation.getStatut()
    }

    override fun getItemCount(): Int = reservations.size

    fun canCancel(position: Int): Boolean =
        position in reservations.indices && canCancel(reservations[position])

    fun notifyItemNeedsRefresh(position: Int) {
        if (position in reservations.indices) notifyItemChanged(position)
    }

    fun replaceItems(visibleReservations: List<Reservation>) {
        reservations.clear()
        reservations.addAll(visibleReservations)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position !in reservations.indices) return
        val reservation = reservations.removeAt(position)
        notifyItemRemoved(position)
        onCancel(reservation)
    }

    fun restoreItem(reservation: Reservation, position: Int) {
        val restoredPosition = position.coerceIn(0, reservations.size)
        reservations.add(restoredPosition, reservation)
        notifyItemInserted(restoredPosition)
    }
}
