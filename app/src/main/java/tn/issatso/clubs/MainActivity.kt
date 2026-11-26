package tn.issatso.clubs

import android.os.Bundle
import android.os.Build
import android.content.Intent
import android.provider.Settings
import android.text.InputType
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    companion object {
        private const val ADMIN_PIN = "2026"
        private const val MIN_CAPACITY = 1
        private const val MAX_CAPACITY = 50
        private const val DEFAULT_CAPACITY = 2
        private const val DATE_PATTERN = "dd/MM/yyyy"
        private const val STATE_SERIALIZED = "hajz_state"
    }

    private fun setTooltipCompat(view: View, text: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            view.tooltipText = text
        }
    }

    private lateinit var viewA: View
    private lateinit var viewB: View
    private lateinit var viewC: View

    private lateinit var adapterReservations: ReservationAdapter
    private var filtreActif = 0
    private var adminMode = false
    private var searchText = ""
    private var selectedClub: String? = null
    private var activeView = "A"
    private var selectedWorkshopId: Int? = null

    // --- ADAPTATEUR RECYCLERVIEW POUR LES ATELIERS (VUE A) ---
    inner class AtelierAdapter(private val ateliers: List<Atelier>) :
        RecyclerView.Adapter<AtelierAdapter.ViewHolder>() {

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val tvClub: TextView = v.findViewById(R.id.tvClub)
            val tvTitre: TextView = v.findViewById(R.id.tvTitre)
            val tvDate: TextView = v.findViewById(R.id.tvDate)
            val tvStatus: TextView = v.findViewById(R.id.tvStatus)
            val tvSalle: TextView = v.findViewById(R.id.tvSalle)
            val tvInstructeur: TextView = v.findViewById(R.id.tvInstructeur)
            val pbPlaces: ProgressBar = v.findViewById(R.id.pbPlaces)
            val btnReserver: com.google.android.material.button.MaterialButton =
                v.findViewById(R.id.btnReserver)
            val btnDetails: com.google.android.material.button.MaterialButton =
                v.findViewById(R.id.btnDetails)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_atelier, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val a = ateliers[position]
            holder.tvClub.text = a.club
            holder.tvTitre.text = a.nom
            holder.tvSalle.text = a.salle
            holder.tvInstructeur.text = a.instructeur
            val today = SimpleDateFormat(DateHelper.DATE_PATTERN, Locale.getDefault())
                .format(Calendar.getInstance().time)
            val relation = DateHelper.compareWithToday(a.date, today)
            val isPast = relation == DateRelation.PAST
            holder.tvDate.text = if (isPast) "${a.date} · Terminé" else a.date

            val places = BookingManager.placesLibres(a)
            val occupied = (a.capacite - places).coerceIn(0, a.capacite)
            val statusText = if (places > 0) "$occupied/${a.capacite}" else "Complet"
            holder.tvStatus.text = if (isPast) "Terminé · $statusText" else statusText
            if (places > 0) {
                holder.tvStatus.setTextColor(applicationContext.getColor(R.color.secondary))
            } else {
                holder.tvStatus.setTextColor(applicationContext.getColor(R.color.danger))
            }
            holder.pbPlaces.max = a.capacite
            holder.pbPlaces.progress = occupied
            val capacityColor = when {
                places == 0 -> R.color.capacity_red
                places.toDouble() / a.capacite.toDouble() > 0.5 -> R.color.capacity_green
                else -> R.color.capacity_orange
            }
            holder.pbPlaces.progressTintList =
                android.content.res.ColorStateList.valueOf(getColor(capacityColor))
            holder.itemView.alpha = if (isPast) 0.55f else 1f
            holder.btnReserver.isEnabled = !isPast
            holder.btnReserver.text = ""
            val reservationTooltip = when {
                isPast -> getString(R.string.finished_icon_description)
                places == 0 -> getString(R.string.queue_icon_description)
                else -> getString(R.string.reserver)
            }
            setTooltipCompat(holder.btnReserver, reservationTooltip)
            holder.btnReserver.contentDescription = reservationTooltip
            setTooltipCompat(holder.btnDetails, getString(R.string.details))
            holder.btnDetails.contentDescription = getString(R.string.details)
            holder.btnReserver.setIconResource(
                when {
                    isPast -> R.drawable.ic_lock
                    places == 0 -> R.drawable.ic_queue
                    else -> R.drawable.ic_calendar_check
                }
            )

            // Click to reserve
            holder.itemView.setOnClickListener {
                if (isPast) {
                    Toast.makeText(this@MainActivity, "Atelier terminé", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                selectedWorkshopId = a.id
                val spinner = findViewById<Spinner>(R.id.spAteliers)
                val reservableIndex = reservableAteliers().indexOf(a)
                if (reservableIndex >= 0) spinner.setSelection(reservableIndex)
                afficherVueB()
            }
            holder.itemView.setOnLongClickListener {
                if (adminMode) {
                    showAdminWorkshopDialog(a)
                    true
                } else {
                    false
                }
            }
            holder.btnReserver.setOnClickListener {
                holder.itemView.performClick()
            }
            holder.btnReserver.isEnabled = !isPast
            holder.btnReserver.text = ""
            holder.btnDetails.setOnClickListener { showDetailsDialog(a) }
                        // Staggered fade-in animation for cards
            if (animationsEnabled() && position < 6) {
                holder.itemView.alpha = 0f
                holder.itemView.animate().alpha(1f).setDuration(250)
                    .setStartDelay((position * 40L)).start()
            } else {
                holder.itemView.animate().cancel()
                holder.itemView.alpha = 1f
            }
        }

        override fun getItemCount() = ateliers.size
    }

    inner class ReservationAdapter : BaseAdapter() {
        private val reservations = mutableListOf<Reservation>()

        fun replaceItems(items: List<Reservation>) {
            reservations.clear()
            reservations.addAll(items)
            notifyDataSetChanged()
        }

        override fun getCount() = reservations.size

        override fun getItem(position: Int): Reservation = reservations[position]

        override fun getItemId(position: Int): Long = reservations[position].hashCode().toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView ?: LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reservation, parent, false)
            val reservation = getItem(position)
            row.findViewById<TextView>(R.id.tvResNom).text =
                "${reservation.prenom} ${reservation.nom}"
            row.findViewById<TextView>(R.id.tvResAtelier).text =
                reservation.atelier.nom
            row.findViewById<TextView>(R.id.tvResDate).text = reservation.atelier.date
            row.findViewById<TextView>(R.id.tvResSalle).text = reservation.atelier.salle
            val statutView = row.findViewById<TextView>(R.id.tvResStatut)
            val attenteView = row.findViewById<TextView>(R.id.tvResAttente)
            if (reservation.ticketAttente == null) {
                statutView.text = getString(R.string.status_confirmed)
                statutView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check, 0, 0, 0)
                statutView.compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.space_4)
                attenteView.visibility = View.GONE
            } else {
                val ahead = BookingManager.personnesDevant(reservation)
                val personne = if (ahead == 1) "personne" else "personnes"
                statutView.text = " ${getString(R.string.status_waiting)} #${reservation.ticketAttente}"
                statutView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_hourglass, 0, 0, 0)
                statutView.compoundDrawablePadding = resources.getDimensionPixelSize(R.dimen.space_4)
                attenteView.visibility = View.VISIBLE
                attenteView.text = "$ahead $personne"
            }
            val cancelButton = row.findViewById<ImageButton>(R.id.btnCancelRes)
            setTooltipCompat(cancelButton, getString(R.string.annuler_reservation))
            cancelButton.setOnClickListener {
                showCancelDialog(reservation)
            }
            return row
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewA = findViewById(R.id.viewA)
        viewB = findViewById(R.id.viewB)
        viewC = findViewById(R.id.viewC)
        setTooltipCompat(findViewById(R.id.btnAjouterAtelier), getString(R.string.ajouter_atelier))
        setTooltipCompat(findViewById(R.id.btnExporter), getString(R.string.exporter))
        setTooltipCompat(findViewById(R.id.btnAdmin), getString(R.string.admin_access))
        setTooltipCompat(findViewById(R.id.btnBackB), getString(R.string.retour_ateliers))
        setTooltipCompat(findViewById(R.id.btnBackC), getString(R.string.retour_ateliers))
        setTooltipCompat(findViewById(R.id.btnGoToMesReservations), getString(R.string.voir_mes_reservations))

        if (savedInstanceState != null) {
            savedInstanceState.getString(STATE_SERIALIZED)?.let { serialized ->
                StateSerializer.deserialize(serialized)?.let { snapshot ->
                    BookingManager.restaurerEtat(snapshot.workshops, snapshot.ticketCounters, snapshot.nextAtelierId)
                    filtreActif = snapshot.ui.filter
                    searchText = snapshot.ui.searchText
                    selectedClub = snapshot.ui.selectedClub
                    adminMode = snapshot.ui.adminMode
                    activeView = snapshot.ui.activeView
                    selectedWorkshopId = snapshot.ui.selectedWorkshopId
                }
            }
        }

        // Setup RecyclerView
        val rvAteliers = findViewById<RecyclerView>(R.id.rvAteliers)
        rvAteliers.layoutManager = LinearLayoutManager(this)

        findViewById<EditText>(R.id.etRechercheAteliers).addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    searchText = s?.toString().orEmpty()
                    actualiserVueA()
                }
                override fun afterTextChanged(s: Editable?) {}
            }
        )
        findViewById<EditText>(R.id.etRechercheAteliers).setText(searchText)
        rebuildClubChips()
        
        findViewById<View>(R.id.btnGoToMesReservations).setOnClickListener { afficherVueC() }
        findViewById<View>(R.id.btnAdmin).setOnClickListener { onAdminButtonClicked() }
        findViewById<View>(R.id.btnAjouterAtelier).setOnClickListener { showAddWorkshopDialog() }
        findViewById<View>(R.id.btnExporter).setOnClickListener { exportAteliers() }
        updateAdminUi()

        // Setup Spinner
        refreshAtelierSpinner()
        
        findViewById<View>(R.id.btnBackB).setOnClickListener { afficherVueA() }
        findViewById<View>(R.id.btnConfirmerReservation).setOnClickListener { gererReservation() }

        // Setup Vue C
        val lvReservations = findViewById<ListView>(R.id.lvReservations)
        adapterReservations = ReservationAdapter()
        lvReservations.adapter = adapterReservations
        findViewById<View>(R.id.btnBackC).setOnClickListener { afficherVueA() }
        rebuildFilterChips()

        when (activeView) {
            "B" -> afficherVueBRestored()
            "C" -> afficherVueCRestored()
            else -> afficherVueARestored()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val snapshot = StateSerializer.Snapshot(
            BookingManager.ateliers.toList(),
            BookingManager.getTicketCounters(),
            BookingManager.getNextAtelierId(),
            StateSerializer.UiState(
                activeView,
                filtreActif,
                searchText,
                selectedClub,
                adminMode,
                selectedWorkshopId
            )
        )
        outState.putString(STATE_SERIALIZED, StateSerializer.serialize(snapshot))
    }

    private fun afficherVueA() {
        activeView = "A"
        animateTransition(viewB, viewA)
        animateTransition(viewC, viewA)
        actualiserVueA()
    }

    private fun afficherVueB() {
        activeView = "B"
        animateTransition(viewA, viewB)
        animateTransition(viewC, viewB)
    }

    private fun afficherVueC() {
        activeView = "C"
        animateTransition(viewA, viewC)
        animateTransition(viewB, viewC)
        actualiserVueC()
    }

    private fun afficherVueARestored() {
        activeView = "A"
        viewA.alpha = 1f
        viewA.visibility = View.VISIBLE
        viewB.visibility = View.GONE
        viewC.visibility = View.GONE
        viewB.alpha = 1f
        viewC.alpha = 1f
        actualiserVueA()
    }

    private fun afficherVueBRestored() {
        activeView = "B"
        viewB.alpha = 1f
        viewB.visibility = View.VISIBLE
        viewA.visibility = View.GONE
        viewC.visibility = View.GONE
        refreshAtelierSpinner()
    }

    private fun afficherVueCRestored() {
        activeView = "C"
        viewC.alpha = 1f
        viewC.visibility = View.VISIBLE
        viewA.visibility = View.GONE
        viewB.visibility = View.GONE
        actualiserVueC()
    }

    // The Animation Magic
    private fun animateTransition(fromView: View, toView: View) {
        if (!animationsEnabled()) {
            fromView.animate().cancel()
            toView.animate().cancel()
            fromView.visibility = View.GONE
            fromView.alpha = 1f
            toView.visibility = View.VISIBLE
            toView.alpha = 1f
            toView.translationY = 0f
            return
        }
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

    private fun animationsEnabled(): Boolean =
        Settings.Global.getFloat(
            contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) > 0f

    private fun actualiserVueC() {
        val toutes = BookingManager.ateliers.flatMap { it.reservations }
        val visibles = toutes.filter { r ->
            val estConfirme = r.ticketAttente == null
            filtreActif == 0 || (filtreActif == 1 && estConfirme) || (filtreActif == 2 && !estConfirme)
        }
        adapterReservations.replaceItems(visibles)
        val lv = findViewById<ListView>(R.id.lvReservations)
        val empty = findViewById<View>(R.id.emptyReservations)
        val emptyText = findViewById<TextView>(R.id.tvEmptyReservations)
        if (visibles.isEmpty()) {
            lv.visibility = View.GONE
            empty.visibility = View.VISIBLE
            emptyText.text = if (toutes.isEmpty()) {
                getString(R.string.empty_reservations)
            } else {
                getString(R.string.empty_filtered_reservations)
            }
        } else {
            lv.visibility = View.VISIBLE
            empty.visibility = View.GONE
        }
        val badge = findViewById<TextView>(R.id.tvReservationBadge)
        badge.text = toutes.size.toString()
        badge.visibility = if (toutes.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun actualiserVueA() {
        DateHelper.sortAteliers(BookingManager.ateliers)
        val rv = findViewById<RecyclerView>(R.id.rvAteliers)
        val empty = findViewById<View>(R.id.emptyAteliers)
        val filtered = FiltreAteliers.filtrer(
            BookingManager.ateliers,
            searchText,
            selectedClub
        )
        if (filtered.isEmpty()) {
            rv.visibility = View.GONE
            empty.visibility = View.VISIBLE
            val emptyText = findViewById<TextView>(R.id.tvEmptyAteliers)
            val filtering = searchText.trim().isNotEmpty() || selectedClub != null
            emptyText.text = if (filtering) {
                getString(R.string.aucun_atelier_filtre)
            } else {
                getString(R.string.empty_ateliers)
            }
        } else {
            rv.visibility = View.VISIBLE
            empty.visibility = View.GONE
        }
        rv.adapter = AtelierAdapter(filtered)
    }

    private fun refreshAtelierSpinner() {
        val reservables = reservableAteliers()
        val nomsAteliers = reservables.map { "${it.club} - ${it.nom}" }
        val spinner = findViewById<Spinner>(R.id.spAteliers)
        spinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nomsAteliers)
        if (selectedWorkshopId != null) {
            val index = reservables.indexOfFirst { it.id == selectedWorkshopId }
            if (index >= 0) spinner.setSelection(index)
        }
    }

    private fun reservableAteliers(): List<Atelier> {
        val today = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
            .format(Calendar.getInstance().time)
        return BookingManager.ateliers.filter {
            DateHelper.compareWithToday(it.date, today) != DateRelation.PAST
        }
    }

    private fun rebuildFilterChips() {
        val container = findViewById<LinearLayout>(R.id.filterChips)
        container.removeAllViews()
        val labels = listOf(
            R.string.filter_all,
            R.string.filter_confirmed,
            R.string.filter_waiting
        )
        labels.forEachIndexed { position, labelRes ->
            val chip = Chip(this).apply {
                isCheckable = true
                text = getString(labelRes)
                chipMinHeight = resources.getDimension(R.dimen.touch_target)
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(resources.getDimension(R.dimen.radius_chip))
                    .build()
                isChecked = filtreActif == position
                setChipBackgroundColorResource(
                    if (isChecked) R.color.blue_700 else R.color.blue_100
                )
                setTextColor(getColor(if (isChecked) R.color.onPrimary else R.color.blue_700))
                setOnClickListener {
                    filtreActif = position
                    rebuildFilterChips()
                    actualiserVueC()
                }
            }
            container.addView(
                chip,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = resources.getDimensionPixelSize(R.dimen.space_8) }
            )
        }
    }

    private fun rebuildClubChips() {
        val chipsContainer = findViewById<LinearLayout>(R.id.chipsClubs)
        chipsContainer.removeAllViews()

        val clubs = listOf<String?>(null) + FiltreAteliers.clubsDistincts(BookingManager.ateliers)
        if (selectedClub != null &&
            FiltreAteliers.clubsDistincts(BookingManager.ateliers)
                .none { FiltreAteliers.normalize(it) == FiltreAteliers.normalize(selectedClub!!) }
        ) {
            selectedClub = null
        }

        clubs.forEach { club ->
            val chip = Chip(this).apply {
                isCheckable = true
                text = club ?: getString(R.string.tous_les_clubs)
                chipMinHeight = 48f * resources.displayMetrics.density
                chipStrokeWidth = resources.displayMetrics.density
                shapeAppearanceModel = shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(resources.getDimension(R.dimen.radius_chip))
                    .build()
                rippleColor = android.content.res.ColorStateList.valueOf(getColor(R.color.blue_100))
                isChecked = if (club == null) selectedClub == null else
                    selectedClub != null &&
                        FiltreAteliers.normalize(club) == FiltreAteliers.normalize(selectedClub!!)
                val selected = isChecked
                setChipBackgroundColorResource(
                    if (selected) R.color.blue_700 else R.color.blue_100
                )
                setTextColor(getColor(if (selected) R.color.onPrimary else R.color.blue_700))
                chipStrokeColor = android.content.res.ColorStateList.valueOf(
                    getColor(if (selected) R.color.gold_500 else R.color.blue_100)
                )
                setOnClickListener {
                    selectedClub = club
                    for (i in 0 until chipsContainer.childCount) {
                        val sibling = chipsContainer.getChildAt(i) as Chip
                        sibling.isChecked = sibling === this
                        val siblingSelected = sibling.isChecked
                        sibling.setChipBackgroundColorResource(
                            if (siblingSelected) R.color.blue_700 else R.color.blue_100
                        )
                        sibling.setTextColor(
                            getColor(if (siblingSelected) R.color.onPrimary else R.color.blue_700)
                        )
                        sibling.chipStrokeColor =
                            android.content.res.ColorStateList.valueOf(
                                getColor(if (siblingSelected) R.color.gold_500 else R.color.blue_100)
                            )
                    }
                    actualiserVueA()
                }
            }
            chipsContainer.addView(
                chip,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
        }
    }

    private fun onAdminButtonClicked() {
        if (adminMode) {
            MaterialAlertDialogBuilder(this)
                .setMessage("Quitter le mode admin ?")
                .setNegativeButton("Non", null)
                .setPositiveButton("Oui") { _, _ ->
                    adminMode = false
                    updateAdminUi()
                    Toast.makeText(this, "Mode admin désactivé", Toast.LENGTH_SHORT).show()
                }
                .show()
        } else {
            showAdminPinDialog()
        }
    }

    private fun showAdminPinDialog() {
        val pinInput = EditText(this).apply {
            hint = getString(R.string.admin_pin_hint)
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.admin_pin_title)
            .setView(pinInput)
            .setNegativeButton(R.string.annuler, null)
            .setPositiveButton(R.string.valider, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (pinInput.text.toString().trim() == ADMIN_PIN) {
                    adminMode = true
                    updateAdminUi()
                    dialog.dismiss()
                    Toast.makeText(this, "Mode admin activé", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, R.string.pin_incorrect, Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun updateAdminUi() {
        val button = findViewById<ImageButton>(R.id.btnAdmin)
        val addButton = findViewById<ImageButton>(R.id.btnAjouterAtelier)
        val exportButton = findViewById<ImageButton>(R.id.btnExporter)
        if (adminMode) {
            button.setImageResource(R.drawable.ic_admin_unlock)
            button.setBackgroundResource(R.drawable.bg_admin_on)
            button.imageTintList = android.content.res.ColorStateList.valueOf(
                getColor(R.color.on_admin_accent)
            )
            button.contentDescription = getString(R.string.quit_admin_access)
            setTooltipCompat(button, getString(R.string.quit_admin_access))
            addButton.visibility = View.VISIBLE
            exportButton.visibility = View.VISIBLE
        } else {
            button.setImageResource(R.drawable.ic_admin_lock)
            button.setBackgroundResource(android.R.color.transparent)
            button.imageTintList = android.content.res.ColorStateList.valueOf(
                getColor(R.color.onPrimary)
            )
            button.contentDescription = getString(R.string.admin_access)
            setTooltipCompat(button, getString(R.string.admin_access))
            addButton.visibility = View.GONE
            exportButton.visibility = View.GONE
        }
        actualiserVueA()
    }

    private fun showDetailsDialog(atelier: Atelier) {
        val content = LayoutInflater.from(this).inflate(R.layout.dialog_details, null)
        content.findViewById<TextView>(R.id.tvDetailTitre).text = atelier.nom
        content.findViewById<TextView>(R.id.tvDetailClub).text =
            getString(R.string.detail_club, atelier.club)
        content.findViewById<TextView>(R.id.tvDetailDate).text =
            getString(R.string.detail_date, atelier.date)
        content.findViewById<TextView>(R.id.tvDetailSalle).text =
            getString(R.string.detail_salle, atelier.salle)
        content.findViewById<TextView>(R.id.tvDetailInstructeur).text =
            getString(R.string.detail_instructeur, atelier.instructeur)
        content.findViewById<TextView>(R.id.tvDetailPlaces).text =
            getString(R.string.detail_places, atelier.capacite, BookingManager.placesLibres(atelier))

        val today = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
            .format(Calendar.getInstance().time)
        val isPast = DateHelper.compareWithToday(atelier.date, today) == DateRelation.PAST
        val statusView = content.findViewById<TextView>(R.id.tvDetailStatut)
        statusView.text = if (isPast) getString(R.string.detail_status_termine) else ""
        statusView.visibility = if (isPast) View.VISIBLE else View.GONE

        val builder = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.details)
            .setView(content)
            .setPositiveButton(R.string.fermer, null)
        if (adminMode) {
            builder.setNeutralButton(R.string.modifier_details) { _, _ ->
                if (adminMode) showEditDetailsDialog(atelier)
            }
        }
        builder.show()
    }

    private fun showAddWorkshopDialog() {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                resources.getDimensionPixelSize(R.dimen.space_24),
                resources.getDimensionPixelSize(R.dimen.space_4),
                resources.getDimensionPixelSize(R.dimen.space_24),
                0
            )
        }
        val clubSpinner = Spinner(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val knownClubs = listOf(getString(R.string.choisir_club)) +
            BookingManager.ateliers.map { it.club }.distinct()
        clubSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, knownClubs)
        val customClub = EditText(this).apply {
            hint = getString(R.string.club_personnalise)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val nameInput = EditText(this).apply {
            hint = getString(R.string.nom_atelier)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val dateInput = EditText(this).apply {
            hint = getString(R.string.date_atelier)
            isFocusable = false
            isClickable = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val capacityInput = EditText(this).apply {
            hint = getString(R.string.capacite_atelier)
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(DEFAULT_CAPACITY.toString())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val instructorInput = EditText(this).apply {
            hint = getString(R.string.instructeur)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val roomSpinner = Spinner(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        roomSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.choisir_salle)) + Salles.salles
        )
        dateInput.setOnClickListener { showDatePicker(dateInput) }
        val dialogFieldParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        content.addView(clubSpinner, dialogFieldParams)
        content.addView(customClub, dialogFieldParams)
        content.addView(nameInput, dialogFieldParams)
        content.addView(dateInput, dialogFieldParams)
        content.addView(instructorInput, dialogFieldParams)
        content.addView(roomSpinner, dialogFieldParams)
        content.addView(capacityInput, dialogFieldParams)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.nouvel_atelier)
            .setView(content)
            .setNegativeButton(R.string.annuler, null)
            .setPositiveButton(R.string.valider, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val club = customClub.text.toString().trim().ifEmpty {
                    if (clubSpinner.selectedItemPosition > 0) clubSpinner.selectedItem.toString() else ""
                }
                val nom = nameInput.text.toString().trim()
                val date = dateInput.text.toString().trim()
                val capacite = capacityInput.text.toString().toIntOrNull() ?: 0
                val salle = if (roomSpinner.selectedItemPosition > 0) {
                    roomSpinner.selectedItem.toString()
                } else ""
                val result = BookingManager.ajouterAtelier(
                    club,
                    nom,
                    date,
                    capacite,
                    instructorInput.text.toString(),
                    salle
                )
                if (result == "Atelier ajouté") {
                    refreshAtelierSpinner()
                    rebuildClubChips()
                    actualiserVueA()
                    actualiserVueC()
                    Toast.makeText(this, R.string.atelier_ajoute, Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                }
            }
        }
        dialog.show()
    }

    private fun showDatePicker(target: EditText) {
        val calendar = Calendar.getInstance()
        android.app.DatePickerDialog(
            this,
            { _, year, month, day ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                target.setText(SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(selected.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showAdminWorkshopDialog(atelier: Atelier) {
        val actions = arrayOf(
            getString(R.string.voir_participants),
            getString(R.string.modifier_details),
            getString(R.string.modifier_capacite),
            getString(R.string.supprimer_atelier)
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(atelier.nom)
            .setItems(actions) { _, position ->
                when (position) {
                    0 -> showAttendeeDialog(atelier)
                    1 -> showEditDetailsDialog(atelier)
                    2 -> showCapacityDialog(atelier)
                    3 -> showDeleteWorkshopDialog(atelier)
                }
            }
            .setNegativeButton(R.string.annuler, null)
            .show()
    }

    private fun showAttendeeDialog(atelier: Atelier) {
        val confirmed = atelier.reservations.filter { it.ticketAttente == null }
        val waiting = atelier.reservations
            .filter { it.ticketAttente != null }
            .sortedBy { it.ticketAttente }
        val content = buildString {
            append(getString(R.string.liste_confirmes_titre))
            append("\n")
            if (confirmed.isEmpty()) {
                append(getString(R.string.aucun_participant))
            } else {
                confirmed.forEachIndexed { index, reservation ->
                    append("${index + 1}. ${reservation.prenom} ${reservation.nom}")
                    append("\n")
                }
            }
            append("\n")
            append(getString(R.string.liste_attente_titre))
            append("\n")
            if (waiting.isEmpty()) {
                append(getString(R.string.aucun_en_attente))
            } else {
                waiting.forEach { reservation ->
                    append("#${reservation.ticketAttente} ${reservation.prenom} ${reservation.nom}")
                    append("\n")
                }
            }
        }.trim()
        MaterialAlertDialogBuilder(this)
            .setTitle(atelier.nom)
            .setMessage(content)
            .setPositiveButton(R.string.valider, null)
            .show()
    }

    private fun showEditDetailsDialog(atelier: Atelier) {
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                resources.getDimensionPixelSize(R.dimen.space_24),
                resources.getDimensionPixelSize(R.dimen.space_4),
                resources.getDimensionPixelSize(R.dimen.space_24),
                0
            )
        }
        fun fieldInput(hint: String, value: String = "") = EditText(this).apply {
            this.hint = hint
            setText(value)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val clubInput = fieldInput(getString(R.string.club_personnalise), atelier.club)
        val nameInput = fieldInput(getString(R.string.nom_atelier), atelier.nom)
        val dateInput = fieldInput(getString(R.string.date_atelier), atelier.date).apply {
            isFocusable = false
            isClickable = true
            setOnClickListener { showDatePicker(this) }
        }
        val instructorInput = fieldInput(getString(R.string.instructeur), atelier.instructeur)
        val roomSpinner = Spinner(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        roomSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.choisir_salle)) + Salles.salles
        )
        val roomIndex = Salles.salles.indexOf(atelier.salle)
        roomSpinner.setSelection(if (roomIndex >= 0) roomIndex + 1 else 0)

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        content.addView(clubInput, params)
        content.addView(nameInput, params)
        content.addView(dateInput, params)
        content.addView(instructorInput, params)
        content.addView(roomSpinner, params)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.modifier_details)
            .setView(content)
            .setNegativeButton(R.string.annuler, null)
            .setPositiveButton(R.string.valider, null)
            .create()

        fun saveDetails() {
            val salle = if (roomSpinner.selectedItemPosition > 0) {
                roomSpinner.selectedItem.toString()
            } else ""
            val result = BookingManager.modifierDetails(
                atelier.id,
                clubInput.text.toString(),
                nameInput.text.toString(),
                dateInput.text.toString(),
                salle,
                instructorInput.text.toString()
            )
            if (result == "Détails mis à jour") {
                refreshAtelierSpinner()
                rebuildClubChips()
                actualiserVueA()
                actualiserVueC()
                Toast.makeText(this, R.string.details_mis_a_jour, Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, result, Toast.LENGTH_LONG).show()
            }
        }

        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val salle = if (roomSpinner.selectedItemPosition > 0) {
                    roomSpinner.selectedItem.toString()
                } else ""
                val dateChanged = dateInput.text.toString().trim() != atelier.date
                val roomChanged = salle != atelier.salle
                if (atelier.reservations.isNotEmpty() && (dateChanged || roomChanged)) {
                    MaterialAlertDialogBuilder(this)
                        .setMessage(getString(R.string.confirmation_modification, atelier.reservations.size))
                        .setNegativeButton(R.string.annuler, null)
                        .setPositiveButton(R.string.valider) { _, _ -> saveDetails() }
                        .show()
                } else {
                    saveDetails()
                }
            }
        }
        dialog.show()
    }

    private fun showCapacityDialog(atelier: Atelier) {
        val confirmedCount = atelier.reservations.count { it.ticketAttente == null }
        val input = EditText(this).apply {
            hint = getString(R.string.capacite_atelier)
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(atelier.capacite.toString())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.modifier_capacite)
            .setMessage(getString(R.string.parties_capacite_min))
            .setView(input)
            .setNegativeButton(R.string.annuler, null)
            .setPositiveButton(R.string.valider, null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val capacity = input.text.toString().toIntOrNull()
                if (capacity == null || capacity !in confirmedCount..MAX_CAPACITY) {
                    Toast.makeText(this, R.string.capacite_invalide, Toast.LENGTH_SHORT).show()
                } else {
                    val promoted = BookingManager.changerCapacite(atelier, capacity)
                    if (promoted < 0) {
                        Toast.makeText(this, R.string.capacite_invalide, Toast.LENGTH_SHORT).show()
                    } else {
                        actualiserVueA()
                        actualiserVueC()
                        Toast.makeText(this, getString(R.string.promus_confirmation, promoted), Toast.LENGTH_LONG).show()
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showDeleteWorkshopDialog(atelier: Atelier) {
        val reservationCount = atelier.reservations.size
        val message = if (reservationCount > 0) {
            getString(R.string.supprimer_confirmation, reservationCount)
        } else {
            getString(R.string.supprimer_atelier)
        }
        MaterialAlertDialogBuilder(this)
            .setMessage(message)
            .setNegativeButton(R.string.non, null)
            .setPositiveButton(R.string.oui) { _, _ ->
                if (BookingManager.supprimerAtelier(atelier)) {
                    refreshAtelierSpinner()
                    rebuildClubChips()
                    actualiserVueA()
                    actualiserVueC()
                    Toast.makeText(this, R.string.atelier_supprime, Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun exportAteliers() {
        if (!adminMode) return
        val text = ExportAteliers.generer(BookingManager.ateliers)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, getString(R.string.export_partage_titre)))
    }

    private fun showCancelDialog(reservation: Reservation) {
        MaterialAlertDialogBuilder(this)
            .setMessage("Annuler cette réservation ?")
            .setNegativeButton("Non", null)
            .setPositiveButton("Oui") { _, _ ->
                val undo = BookingManager.annuler(reservation) ?: return@setPositiveButton
                actualiserVueA()
                actualiserVueC()

                undo.promotedReservation?.let {
                    Toast.makeText(
                        this,
                        "Une place s'est libérée : ${it.prenom} ${it.nom} est maintenant confirmé(e)",
                        Toast.LENGTH_LONG
                    ).show()
                }

                Snackbar.make(
                    findViewById(android.R.id.content),
                    "Réservation annulée",
                    Snackbar.LENGTH_LONG
                ).setAction("ANNULER") {
                    BookingManager.restaurerAnnulation(undo)
                    actualiserVueA()
                    actualiserVueC()
                }.show()
            }
            .show()
    }

    private fun gererReservation() {
        val nom = findViewById<EditText>(R.id.etNom).text.toString()
        val prenom = findViewById<EditText>(R.id.etPrenom).text.toString()
        val spinner = findViewById<Spinner>(R.id.spAteliers)
        val reservables = reservableAteliers()
        if (reservables.isEmpty() || spinner.selectedItemPosition !in reservables.indices) {
            Toast.makeText(this, "Aucun atelier disponible pour réservation", Toast.LENGTH_LONG).show()
            return
        }
        val atelierChoisi = reservables[spinner.selectedItemPosition]

        val resultat = BookingManager.reserver(nom, prenom, atelierChoisi)
        Toast.makeText(this, resultat, Toast.LENGTH_LONG).show()

        if (resultat.startsWith("Succès")) {
            findViewById<EditText>(R.id.etNom).text.clear()
            findViewById<EditText>(R.id.etPrenom).text.clear()
            afficherVueA()
        }
    }
}