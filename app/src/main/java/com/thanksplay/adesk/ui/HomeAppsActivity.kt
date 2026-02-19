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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.AppLoader
import com.thanksplay.adesk.util.PreferencesManager

class HomeAppsActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var appLoader: AppLoader
    
    private lateinit var searchInput: EditText
    private lateinit var homeAppsRecyclerView: RecyclerView
    private lateinit var allAppsRecyclerView: RecyclerView
    private lateinit var btnSave: android.widget.Button
    
    private var allApps: List<AppInfo> = emptyList()
    private var homeApps: MutableList<AppInfo> = mutableListOf()
    private var filteredApps: List<AppInfo> = emptyList()
    
    private lateinit var homeAppsAdapter: HomeAppsEditAdapter
    private lateinit var allAppsAdapter: AllAppsEditAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_apps)
        
        prefsManager = PreferencesManager(this)
        appLoader = AppLoader(this)
        
        initViews()
        loadApps()
    }
    
    private fun initViews() {
        searchInput = findViewById(R.id.searchInput)
        homeAppsRecyclerView = findViewById(R.id.homeAppsRecyclerView)
        allAppsRecyclerView = findViewById(R.id.allAppsRecyclerView)
        btnSave = findViewById(R.id.btnSave)
        
        val homeColumns = prefsManager.homeColumns
        homeAppsRecyclerView.layoutManager = GridLayoutManager(this, homeColumns)
        homeAppsAdapter = HomeAppsEditAdapter { app -> removeFromHome(app) }
        homeAppsRecyclerView.adapter = homeAppsAdapter
        
        allAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        allAppsAdapter = AllAppsEditAdapter { app -> addToHome(app) }
        allAppsRecyclerView.adapter = allAppsAdapter
        
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString() ?: "")
            }
        })
        
        btnSave.setOnClickListener {
            saveHomeApps()
        }
    }
    
    private fun loadApps() {
        allApps = appLoader.loadAllApps().sortedBy { it.label.lowercase() }
        val homeAppsOrder = prefsManager.homeApps
        val maxApps = prefsManager.homeColumns * prefsManager.homeRows
        
        if (homeAppsOrder.isNotEmpty()) {
            val orderList = homeAppsOrder.split(",").filter { it.isNotEmpty() }
            homeApps = orderList.mapNotNull { packageName ->
                allApps.find { it.packageName == packageName }
            }.toMutableList()
        } else {
            homeApps = allApps.take(maxApps).toMutableList()
        }
        
        filteredApps = allApps
        homeAppsAdapter.setItems(homeApps)
        allAppsAdapter.setItems(filteredApps, homeApps.map { it.packageName }.toSet())
    }
    
    private fun filterApps(query: String) {
        filteredApps = if (query.isEmpty()) {
            allApps
        } else {
            allApps.filter { it.label.lowercase().contains(query.lowercase()) }
        }
        allAppsAdapter.setItems(filteredApps, homeApps.map { it.packageName }.toSet())
    }
    
    private fun addToHome(app: AppInfo) {
        val maxApps = prefsManager.homeColumns * prefsManager.homeRows
        if (homeApps.size >= maxApps) {
            Toast.makeText(this, "主页应用已满", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!homeApps.any { it.packageName == app.packageName }) {
            homeApps.add(app)
            homeAppsAdapter.setItems(homeApps)
            allAppsAdapter.setItems(filteredApps, homeApps.map { it.packageName }.toSet())
        }
    }
    
    private fun removeFromHome(app: AppInfo) {
        homeApps.removeAll { it.packageName == app.packageName }
        homeAppsAdapter.setItems(homeApps)
        allAppsAdapter.setItems(filteredApps, homeApps.map { it.packageName }.toSet())
    }
    
    private fun saveHomeApps() {
        val orderString = homeApps.joinToString(",") { it.packageName }
        prefsManager.homeApps = orderString
        Toast.makeText(this, R.string.order_saved, Toast.LENGTH_SHORT).show()
        finish()
    }
    
    inner class HomeAppsEditAdapter(
        private val onRemove: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<HomeAppsEditAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        
        fun setItems(items: List<AppInfo>) {
            this.items = items
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_app, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            holder.itemView.setOnClickListener { onRemove(app) }
            holder.itemView.setOnLongClickListener {
                onRemove(app)
                true
            }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
    
    inner class AllAppsEditAdapter(
        private val onAdd: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<AllAppsEditAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        private var homeSet: Set<String> = emptySet()
        
        fun setItems(items: List<AppInfo>, homeSet: Set<String>) {
            this.items = items
            this.homeSet = homeSet
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
            
            if (homeSet.contains(app.packageName)) {
                holder.itemView.alpha = 0.5f
            } else {
                holder.itemView.alpha = 1.0f
            }
            
            holder.itemView.setOnClickListener { onAdd(app) }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
}
