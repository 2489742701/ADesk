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

class AppAdapter(
    private val context: Context,
    private val prefsManager: PreferencesManager
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
    
    private var apps: List<AppInfo> = emptyList()
    private var labelPosition: Int = PreferencesManager.LABEL_POSITION_RIGHT
    private var onSettingsClickListener: (() -> Unit)? = null
    
    fun setApps(apps: List<AppInfo>) {
        this.apps = apps
        notifyDataSetChanged()
    }
    
    fun setLabelPosition(position: Int) {
        this.labelPosition = position
        notifyDataSetChanged()
    }
    
    fun setOnSettingsClickListener(listener: () -> Unit) {
        onSettingsClickListener = listener
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val layoutRes = if (viewType == VIEW_TYPE_HORIZONTAL) {
            R.layout.item_app_horizontal
        } else {
            R.layout.item_app_vertical
        }
        val view = LayoutInflater.from(context).inflate(layoutRes, parent, false)
        return AppViewHolder(view)
    }
    
    override fun getItemViewType(position: Int): Int {
        return if (labelPosition == PreferencesManager.LABEL_POSITION_RIGHT) {
            VIEW_TYPE_HORIZONTAL
        } else {
            VIEW_TYPE_VERTICAL
        }
    }
    
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.icon.setImageDrawable(app.icon)
        holder.label.text = app.label
        
        holder.itemView.setOnClickListener {
            if (app.packageName == "com.thanksplay.adesk.settings") {
                onSettingsClickListener?.invoke()
            } else {
                launchApp(app)
            }
        }
        
        holder.itemView.setOnLongClickListener {
            if (app.packageName != "com.thanksplay.adesk.settings" && context is OnAppActionListener) {
                (context as OnAppActionListener).onAppLongClick(app)
            }
            true
        }
    }
    
    override fun getItemCount(): Int = apps.size
    
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
    
    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.appIcon)
        val label: TextView = itemView.findViewById(R.id.appLabel)
    }
    
    interface OnAppActionListener {
        fun onAppLongClick(app: AppInfo)
    }
    
    companion object {
        private const val VIEW_TYPE_HORIZONTAL = 0
        private const val VIEW_TYPE_VERTICAL = 1
    }
}
