package com.thanksplay.adesk.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.plugin.PluginManager

class PluginsActivity : AppCompatActivity() {
    
    private lateinit var pluginsRecyclerView: RecyclerView
    private lateinit var tvNoPlugins: TextView
    private lateinit var btnScanPlugins: Button
    private lateinit var pluginManager: PluginManager
    
    private var installedPlugins: List<PluginManager.PluginInfo> = emptyList()
    private var enabledPlugins: MutableSet<String> = mutableSetOf()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plugins)
        
        pluginManager = PluginManager.getInstance(this)
        enabledPlugins = getSharedPreferences("adesk_plugins", MODE_PRIVATE)
            .getStringSet("enabled_plugins", emptySet())?.toMutableSet() ?: mutableSetOf()
        
        initViews()
        scanPlugins()
    }
    
    private fun initViews() {
        pluginsRecyclerView = findViewById(R.id.pluginsRecyclerView)
        tvNoPlugins = findViewById(R.id.tvNoPlugins)
        btnScanPlugins = findViewById(R.id.btnScanPlugins)
        
        pluginsRecyclerView.layoutManager = LinearLayoutManager(this)
        
        btnScanPlugins.setOnClickListener {
            scanPlugins()
        }
    }
    
    private fun scanPlugins() {
        installedPlugins = pluginManager.scanPlugins()
        
        if (installedPlugins.isEmpty()) {
            pluginsRecyclerView.visibility = View.GONE
            tvNoPlugins.visibility = View.VISIBLE
        } else {
            pluginsRecyclerView.visibility = View.VISIBLE
            tvNoPlugins.visibility = View.GONE
            pluginsRecyclerView.adapter = PluginAdapter()
        }
    }
    
    private fun togglePlugin(plugin: PluginManager.PluginInfo, enable: Boolean) {
        if (enable) {
            enabledPlugins.add(plugin.id)
            pluginManager.loadPlugin(plugin)
            Toast.makeText(this, getString(R.string.plugin_enabled, plugin.name), Toast.LENGTH_SHORT).show()
        } else {
            enabledPlugins.remove(plugin.id)
            pluginManager.unloadPlugin(plugin.id)
            Toast.makeText(this, getString(R.string.plugin_disabled, plugin.name), Toast.LENGTH_SHORT).show()
        }
        
        getSharedPreferences("adesk_plugins", MODE_PRIVATE)
            .edit()
            .putStringSet("enabled_plugins", enabledPlugins)
            .apply()
    }
    
    private fun openPluginSettings(plugin: PluginManager.PluginInfo) {
        val intent = Intent()
        intent.action = "com.thanksplay.adesk.plugin.SETTINGS"
        intent.putExtra("plugin_id", plugin.id)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, R.string.plugin_no_settings, Toast.LENGTH_SHORT).show()
        }
    }
    
    inner class PluginAdapter : RecyclerView.Adapter<PluginAdapter.ViewHolder>() {
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_plugin, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val plugin = installedPlugins[position]
            
            holder.pluginName.text = plugin.name
            holder.pluginVersion.text = "${plugin.version} · ${plugin.author}"
            holder.pluginDescription.text = plugin.description
            
            val isEnabled = enabledPlugins.contains(plugin.id)
            holder.btnToggle.text = if (isEnabled) getString(R.string.disable) else getString(R.string.enable)
            
            try {
                val appInfo = packageManager.getApplicationInfo(plugin.packageName, 0)
                holder.pluginIcon.setImageDrawable(appInfo.loadIcon(packageManager))
            } catch (e: Exception) {
                holder.pluginIcon.setImageResource(android.R.drawable.ic_menu_manage)
            }
            
            holder.btnToggle.setOnClickListener {
                togglePlugin(plugin, !isEnabled)
                notifyItemChanged(position)
            }
            
            holder.itemView.setOnLongClickListener {
                openPluginSettings(plugin)
                true
            }
        }
        
        override fun getItemCount(): Int = installedPlugins.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val pluginIcon: ImageView = itemView.findViewById(R.id.pluginIcon)
            val pluginName: TextView = itemView.findViewById(R.id.pluginName)
            val pluginVersion: TextView = itemView.findViewById(R.id.pluginVersion)
            val pluginDescription: TextView = itemView.findViewById(R.id.pluginDescription)
            val btnToggle: Button = itemView.findViewById(R.id.btnToggle)
        }
    }
}
