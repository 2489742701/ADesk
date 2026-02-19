package com.thanksplay.adesk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.PreferencesManager

class HomeAppAdapter(private val context: Context) : RecyclerView.Adapter<HomeAppAdapter.ViewHolder>() {
    
    private var items: List<AppInfo> = emptyList()
    private val prefsManager = PreferencesManager(context)
    
    fun setItems(apps: List<AppInfo>) {
        this.items = apps
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
        
        if (prefsManager.showHomeAppLabels) {
            holder.label.text = app.label
            holder.label.visibility = View.VISIBLE
        } else {
            holder.label.visibility = View.GONE
        }
        
        holder.itemView.setOnClickListener {
            launchApp(app)
        }
    }
    
    override fun getItemCount(): Int = items.size
    
    private fun launchApp(app: AppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.appIcon)
        val label: TextView = itemView.findViewById(R.id.appLabel)
    }
}
