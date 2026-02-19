package com.thanksplay.adesk.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R

class DesktopPagerAdapter(
    private val pages: List<View>
) : RecyclerView.Adapter<DesktopPagerAdapter.PageViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        return PageViewHolder(pages[viewType])
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
    
    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
    }
    
    override fun getItemCount(): Int = pages.size
    
    class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
