package tn.issatso.clubs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var viewA: View
    private lateinit var viewB: View
    private lateinit var viewC: View

    private lateinit var adapterReservations: ArrayAdapter<String>
    private var filtreActif = 0

    // --- ADAPTATEUR RECYCLERVIEW POUR LES ATELIERS (VUE A) ---
    inner class AtelierAdapter(private val ateliers: List<Atelier>) :
        RecyclerView.Adapter<AtelierAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvClub: TextView = v.findViewById(R.id.tvClub)
            val tvTitre: TextView = v.findViewById(R.id.tvTitre)
            val tvDate: TextView = v.findViewById(R.id.tvDate)
            val tvStatus: TextView = v.findViewById(R.id.tvStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_atelier, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val a = ateliers[position]
            holder.tvClub.text = a.club
            holder.tvTitre.text = a.nom
            holder.tvDate.text = a.date
            
            val places = BookingManager.placesLibres(a)
            if (places > 0) {
                holder.tvStatus.text = "$places/${a.capacite}"
                holder.tvStatus.setTextColor(applicationContext.getColor(R.color.secondary))
            } else {
                holder.tvStatus.text = "COMPLET"
                holder.tvStatus.setTextColor(applicationContext.getColor(R.color.danger))
            }

            // Click to reserve
            holder.itemView.setOnClickListener {
                val spinner = findViewById<Spinner>(R.id.spAteliers)
                spinner.setSelection(position)
                afficherVueB()
            }
                        // Staggered fade-in animation for cards
            holder.itemView.alpha = 0f
            holder.itemView.animate().alpha(1f).setDuration(400).setStartDelay((position * 50).toLong()).start()
        }

        override fun getItemCount() = ateliers.size
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewA = findViewById(R.id.viewA)
        viewB = findViewById(R.id.viewB)
        viewC = findViewById(R.id.viewC)

        // Setup RecyclerView
        val rvAteliers = findViewById<RecyclerView>(R.id.rvAteliers)
        rvAteliers.layoutManager = LinearLayoutManager(this)
        
        findViewById<Button>(R.id.btnGoToMesReservations).setOnClickListener { afficherVueC() }

        // Setup Spinner
         val spAteliers = findViewById<Spinner>(R.id.spAteliers)
        val nomsAteliers: List<String> = BookingManager.ateliers.map { "${it.club} - ${it.nom}" }
         spAteliers.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, nomsAteliers)
        
        findViewById<Button>(R.id.btnBackB).setOnClickListener { afficherVueA() }
        findViewById<Button>(R.id.btnConfirmerReservation).setOnClickListener { gererReservation() }

        // Setup Vue C
        val lvReservations = findViewById<ListView>(R.id.lvReservations)
        adapterReservations = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        lvReservations.adapter = adapterReservations
        findViewById<Button>(R.id.btnBackC).setOnClickListener { afficherVueA() }

        val spFiltre = findViewById<Spinner>(R.id.spFiltre)
        spFiltre.adapter = ArrayAdapter.createFromResource(this, R.array.filtres_reservations, android.R.layout.simple_spinner_dropdown_item)
        spFiltre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filtreActif = position
                actualiserVueC()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        afficherVueA()
    }

    private fun afficherVueA() {
        animateTransition(viewB, viewA)
        animateTransition(viewC, viewA)
        val rvAteliers = findViewById<RecyclerView>(R.id.rvAteliers)
        rvAteliers.adapter = AtelierAdapter(BookingManager.ateliers)
    }

    private fun afficherVueB() {
        animateTransition(viewA, viewB)
        animateTransition(viewC, viewB)
    }

    private fun afficherVueC() {
        animateTransition(viewA, viewC)
        animateTransition(viewB, viewC)
        actualiserVueC()
    }

    // The Animation Magic
    private fun animateTransition(fromView: View, toView: View) {
        if (fromView.visibility == View.VISIBLE) {
            fromView.animate().alpha(0f).setDuration(200).withEndAction {
                fromView.visibility = View.GONE
            }.start()
        }
        
        toView.alpha = 0f
        toView.visibility = View.VISIBLE
        toView.animate().alpha(1f).translationY(0f).setDuration(300).start()
        
        // Start slightly lower for a slide-up effect
        toView.translationY = 50f
    }

    private fun actualiserVueC() {
        adapterReservations.clear()
        val toutes = BookingManager.ateliers.flatMap { it.reservations }
        for (r in toutes) {
            val estConfirme = r.ticketAttente == null
            if (filtreActif == 0 || (filtreActif == 1 && estConfirme) || (filtreActif == 2 && !estConfirme)) {
                adapterReservations.add(r.toString())
            }
        }
        adapterReservations.notifyDataSetChanged()
    }

    private fun gererReservation() {
        val nom = findViewById<EditText>(R.id.etNom).text.toString()
        val prenom = findViewById<EditText>(R.id.etPrenom).text.toString()
        val spinner = findViewById<Spinner>(R.id.spAteliers)
        val atelierChoisi = BookingManager.ateliers[spinner.selectedItemPosition]

        val resultat = BookingManager.reserver(nom, prenom, atelierChoisi)
        Toast.makeText(this, resultat, Toast.LENGTH_LONG).show()

        if (resultat.startsWith("Succès")) {
            findViewById<EditText>(R.id.etNom).text.clear()
            findViewById<EditText>(R.id.etPrenom).text.clear()
            afficherVueA()
        }
    }
}