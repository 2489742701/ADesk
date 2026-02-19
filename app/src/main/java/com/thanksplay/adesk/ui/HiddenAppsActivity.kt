package com.thanksplay.adesk.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class HiddenAppsActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var appLoader: AppLoader
    
    private lateinit var hiddenAppsRecyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    
    private var hiddenApps: MutableList<AppInfo> = mutableListOf()
    private lateinit var adapter: HiddenAppsAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hidden_apps)
        
        prefsManager = PreferencesManager(this)
        appLoader = AppLoader(this)
        
        initViews()
        loadHiddenApps()
    }
    
    private fun initViews() {
        hiddenAppsRecyclerView = findViewById(R.id.hiddenAppsRecyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        
        hiddenAppsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HiddenAppsAdapter { app -> showApp(app) }
        hiddenAppsRecyclerView.adapter = adapter
    }
    
    private fun loadHiddenApps() {
        val allApps = appLoader.loadAllApps()
        val hiddenSet = prefsManager.hiddenApps
        
        hiddenApps = allApps.filter { hiddenSet.contains(it.packageName) }.toMutableList()
        
        if (hiddenApps.isEmpty()) {
            hiddenAppsRecyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        } else {
            hiddenAppsRecyclerView.visibility = View.VISIBLE
            tvEmpty.visibility = View.GONE
            adapter.setItems(hiddenApps)
        }
    }
    
    private fun showApp(app: AppInfo) {
        prefsManager.showApp(app.packageName)
        hiddenApps.removeAll { it.packageName == app.packageName }
        adapter.setItems(hiddenApps)
        Toast.makeText(this, R.string.app_shown, Toast.LENGTH_SHORT).show()
        
        if (hiddenApps.isEmpty()) {
            hiddenAppsRecyclerView.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
        }
    }
    
    inner class HiddenAppsAdapter(
        private val onShow: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<HiddenAppsAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        
        fun setItems(items: List<AppInfo>) {
            this.items = items
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_hidden_app, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            holder.itemView.setOnClickListener { onShow(app) }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
}
