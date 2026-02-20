package com.thanksplay.adesk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import com.thanksplay.adesk.util.PreferencesManager

class AppAdapter(
    private val context: Context,
    private val prefsManager: PreferencesManager
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(AppDiffCallback()) {
    
    companion object {
        private const val VIEW_TYPE_HORIZONTAL = 0
        private const val VIEW_TYPE_VERTICAL = 1
        private const val VIEW_TYPE_HEADER = 2
    }
    
    private var labelPosition: Int = PreferencesManager.LABEL_POSITION_RIGHT
    private var onSettingsClickListener: (() -> Unit)? = null
    private var columns: Int = 2
    
    fun setApps(apps: List<AppInfo>) {
        val items = apps.map { ListItem.AppItem(it) }
        submitList(items)
    }
    
    fun setItemsWithSeparator(favoriteApps: List<AppInfo>, otherApps: List<AppInfo>, separatorTitle: String) {
        val items = mutableListOf<ListItem>()
        
        favoriteApps.forEach { app ->
            items.add(ListItem.AppItem(app))
        }
        
        if (otherApps.isNotEmpty()) {
            items.add(ListItem.Header(separatorTitle))
            otherApps.forEach { app ->
                items.add(ListItem.AppItem(app))
            }
        }
        
        submitList(items)
    }
    
    fun setLabelPosition(position: Int) {
        this.labelPosition = position
        notifyDataSetChanged()
    }
    
    fun setColumns(columns: Int) {
        this.columns = columns
    }
    
    fun setOnSettingsClickListener(listener: () -> Unit) {
        onSettingsClickListener = listener
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.Header -> VIEW_TYPE_HEADER
            is ListItem.AppItem -> if (labelPosition == PreferencesManager.LABEL_POSITION_RIGHT) {
                VIEW_TYPE_HORIZONTAL
            } else {
                VIEW_TYPE_VERTICAL
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_HORIZONTAL -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_app_horizontal, parent, false)
                AppViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_app_vertical, parent, false)
                AppViewHolder(view)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.Header -> {
                (holder as HeaderViewHolder).headerText.text = item.title
            }
            is ListItem.AppItem -> {
                val appHolder = holder as AppViewHolder
                appHolder.icon.setImageDrawable(item.app.icon)
                appHolder.label.text = item.app.label
                
                appHolder.itemView.setOnClickListener {
                    if (item.app.packageName == "com.thanksplay.adesk.settings") {
                        onSettingsClickListener?.invoke()
                    } else {
                        launchApp(item.app)
                    }
                }
                
                appHolder.itemView.setOnLongClickListener {
                    if (item.app.packageName != "com.thanksplay.adesk.settings" && context is OnAppActionListener) {
                        (context as OnAppActionListener).onAppLongClick(item.app)
                    }
                    true
                }
            }
        }
    }
    
    fun getSpanSize(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.Header -> columns
            is ListItem.AppItem -> 1
        }
    }
    
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
    
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerText: TextView = itemView.findViewById(R.id.headerText)
    }
    
    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.appIcon)
        val label: TextView = itemView.findViewById(R.id.appLabel)
    }
    
    interface OnAppActionListener {
        fun onAppLongClick(app: AppInfo)
    }
}

sealed class ListItem {
    data class Header(val title: String) : ListItem()
    data class AppItem(val app: AppInfo) : ListItem()
}

class AppDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return when {
            oldItem is ListItem.Header && newItem is ListItem.Header -> oldItem.title == newItem.title
            oldItem is ListItem.AppItem && newItem is ListItem.AppItem -> oldItem.app.packageName == newItem.app.packageName
            else -> false
        }
    }
    
    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return oldItem == newItem
    }
}
