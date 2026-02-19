package com.thanksplay.adesk.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.AppLoader
import com.thanksplay.adesk.util.PreferencesManager

class FavoritesActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var appLoader: AppLoader
    
    private lateinit var searchInput: EditText
    private lateinit var allAppsRecyclerView: RecyclerView
    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var btnSave: android.widget.Button
    
    private var allApps: List<AppInfo> = emptyList()
    private var filteredApps: List<AppInfo> = emptyList()
    private var favoriteApps: MutableList<AppInfo> = mutableListOf()
    
    private lateinit var allAppsAdapter: AllAppsAdapter
    private lateinit var favoritesAdapter: FavoriteAppAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)
        
        prefsManager = PreferencesManager(this)
        appLoader = AppLoader(this)
        
        initViews()
        loadApps()
    }
    
    private fun initViews() {
        searchInput = findViewById(R.id.searchInput)
        allAppsRecyclerView = findViewById(R.id.allAppsRecyclerView)
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView)
        btnSave = findViewById(R.id.btnSave)
        
        allAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        allAppsAdapter = AllAppsAdapter(
            onAddClick = { app -> addToFavorites(app) },
            onRemoveClick = { app -> removeFromFavorites(app) },
            isFavorite = { app -> prefsManager.isFavorite(app.packageName) }
        )
        allAppsRecyclerView.adapter = allAppsAdapter
        
        favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        favoritesAdapter = FavoriteAppAdapter { app -> removeFromFavorites(app) }
        favoritesRecyclerView.adapter = favoritesAdapter
        
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString() ?: "")
            }
        })
        
        btnSave.setOnClickListener {
            finish()
        }
    }
    
    private fun loadApps() {
        allApps = appLoader.loadAllApps().sortedBy { it.label.lowercase() }
        val favoriteSet = prefsManager.favoriteApps
        
        favoriteApps = allApps.filter { favoriteSet.contains(it.packageName) }.toMutableList()
        filteredApps = allApps
        
        allAppsAdapter.setItems(filteredApps, favoriteApps.map { it.packageName }.toSet())
        favoritesAdapter.setItems(favoriteApps)
    }
    
    private fun filterApps(query: String) {
        filteredApps = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { it.label.lowercase().contains(query.lowercase()) }
        }
        allAppsAdapter.setItems(filteredApps, favoriteApps.map { it.packageName }.toSet())
    }
    
    private fun addToFavorites(app: AppInfo) {
        if (!favoriteApps.any { it.packageName == app.packageName }) {
            prefsManager.addFavoriteApp(app.packageName)
            favoriteApps.add(0, app)
            favoritesAdapter.setItems(favoriteApps)
            allAppsAdapter.setItems(filteredApps, favoriteApps.map { it.packageName }.toSet())
            Toast.makeText(this, R.string.added_to_favorites, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun removeFromFavorites(app: AppInfo) {
        prefsManager.removeFavoriteApp(app.packageName)
        favoriteApps.removeAll { it.packageName == app.packageName }
        favoritesAdapter.setItems(favoriteApps)
        allAppsAdapter.setItems(filteredApps, favoriteApps.map { it.packageName }.toSet())
        Toast.makeText(this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show()
    }
    
    inner class AllAppsAdapter(
        private val onAddClick: (AppInfo) -> Unit,
        private val onRemoveClick: (AppInfo) -> Unit,
        private val isFavorite: (AppInfo) -> Boolean
    ) : RecyclerView.Adapter<AllAppsAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        private var favoriteSet: Set<String> = emptySet()
        
        fun setItems(items: List<AppInfo>, favoriteSet: Set<String>) {
            this.items = items
            this.favoriteSet = favoriteSet
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_horizontal, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            
            holder.itemView.setOnClickListener {
                if (favoriteSet.contains(app.packageName)) {
                    onRemoveClick(app)
                } else {
                    onAddClick(app)
                }
            }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
    
    inner class FavoriteAppAdapter(
        private val onRemoveClick: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<FavoriteAppAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        
        fun setItems(items: List<AppInfo>) {
            this.items = items
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app_horizontal, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            
            holder.itemView.setOnClickListener {
                onRemoveClick(app)
            }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
}
