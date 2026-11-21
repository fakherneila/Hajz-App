package tn.issatso.clubs

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewA: View
    private lateinit var viewB: View
    private lateinit var viewC: View

    // Adaptateurs pour les ListViews
    private lateinit var adapterAteliers: ArrayAdapter<String>
    private lateinit var adapterReservations: ArrayAdapter<String>

    // Filtre actif pour la Vue C
    private var filtreActif = 0 // 0=Toutes, 1=Confirmées, 2=En attente

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Récupération des 3 vues
        viewA = findViewById(R.id.viewA)
        viewB = findViewById(R.id.viewB)
        viewC = findViewById(R.id.viewC)

        // 2. Initialisation Vue A
        val lvAteliers = findViewById<ListView>(R.id.lvAteliers)
        adapterAteliers = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        lvAteliers.adapter = adapterAteliers
        findViewById<Button>(R.id.btnGoToMesReservations).setOnClickListener { afficherVueC() }
        
        // Clic sur un atelier = passer à la Vue B avec cet atelier présélectionné
        lvAteliers.setOnItemClickListener { _, _, position, _ ->
            val spinner = findViewById<Spinner>(R.id.spAteliers)
            spinner.setSelection(position)
            afficherVueB()
        }

        // 3. Initialisation Vue B
        val spAteliers = findViewById<Spinner>(R.id.spAteliers)
        val nomsAteliers = BookingManager.ateliers.map { it.toString() }
        spAteliers.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nomsAteliers)
        
        findViewById<Button>(R.id.btnBackB).setOnClickListener { afficherVueA() }
        findViewById<Button>(R.id.btnConfirmerReservation).setOnClickListener { gererReservation() }

        // 4. Initialisation Vue C
        val lvReservations = findViewById<ListView>(R.id.lvReservations)
        adapterReservations = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf())
        lvReservations.adapter = adapterReservations
        findViewById<Button>(R.id.btnBackC).setOnClickListener { afficherVueA() }

        val spFiltre = findViewById<Spinner>(R.id.spFiltre)
        spFiltre.adapter = ArrayAdapter.createFromResource(this, R.array.filtres_reservations, android.R.layout.simple_spinner_dropdown_item)
        spFiltre.setSelection(filtreActif)
        spFiltre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filtreActif = position
                actualiserVueC()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Premier affichage
        actualiserVueA()
        afficherVueA()
    }

    // --- GESTION DES VUES ---
    private fun afficherVueA() {
        viewA.visibility = View.VISIBLE
        viewB.visibility = View.GONE
        viewC.visibility = View.GONE
        actualiserVueA()
    }

    private fun afficherVueB() {
        viewA.visibility = View.GONE
        viewB.visibility = View.VISIBLE
        viewC.visibility = View.GONE
    }

    private fun afficherVueC() {
        viewA.visibility = View.GONE
        viewB.visibility = View.GONE
        viewC.visibility = View.VISIBLE
        actualiserVueC()
    }

    // --- MISE À JOUR DES DONNÉES ---
    private fun actualiserVueA() {
        adapterAteliers.clear()
        for (a in BookingManager.ateliers) {
            val places = BookingManager.placesLibres(a)
            val statut = if (places > 0) "$places/${a.capacite}" else "COMPLET"
            adapterAteliers.add("${a.club} - ${a.nom}\n$statut")
        }
        adapterAteliers.notifyDataSetChanged()
    }

    private fun actualiserVueC() {
        adapterReservations.clear()
        // Pour l'examen, on affiche toutes les réservations de tous les ateliers
        val toutesLesReservations = BookingManager.ateliers.flatMap { it.reservations }
        
        for (r in toutesLesReservations) {
            val estConfirme = r.ticketAttente == null
            if (filtreActif == 0 || (filtreActif == 1 && estConfirme) || (filtreActif == 2 && !estConfirme)) {
                adapterReservations.add(r.toString())
            }
        }
        adapterReservations.notifyDataSetChanged()
    }

    // --- ACTION : RÉSERVER ---
    private fun gererReservation() {
        val nom = findViewById<EditText>(R.id.etNom).text.toString()
        val prenom = findViewById<EditText>(R.id.etPrenom).text.toString()
        val spinner = findViewById<Spinner>(R.id.spAteliers)
        val atelierChoisi = BookingManager.ateliers[spinner.selectedItemPosition]

        val resultat = BookingManager.reserver(nom, prenom, atelierChoisi)
        Toast.makeText(this, resultat, Toast.LENGTH_LONG).show()

        // Si succès (le message commence par "Succès"), on vide les champs et retourne à la Vue A
        if (resultat.startsWith("Succès")) {
            findViewById<EditText>(R.id.etNom).text.clear()
            findViewById<EditText>(R.id.etPrenom).text.clear()
            afficherVueA()
        }
    }
}