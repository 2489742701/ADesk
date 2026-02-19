package com.thanksplay.adesk.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
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

class CustomOrderActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var appLoader: AppLoader
    
    private lateinit var searchInput: EditText
    private lateinit var tvSearchResults: TextView
    private lateinit var searchResultsRecyclerView: RecyclerView
    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var btnSave: android.widget.Button
    
    private var allApps: List<AppInfo> = emptyList()
    private var orderedApps: MutableList<AppInfo> = mutableListOf()
    private var searchResults: List<AppInfo> = emptyList()
    
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var searchAdapter: SearchAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_order)
        
        prefsManager = PreferencesManager(this)
        appLoader = AppLoader(this)
        
        initViews()
        loadApps()
    }
    
    private fun initViews() {
        searchInput = findViewById(R.id.searchInput)
        tvSearchResults = findViewById(R.id.tvSearchResults)
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView)
        orderRecyclerView = findViewById(R.id.orderRecyclerView)
        btnSave = findViewById(R.id.btnSave)
        
        orderRecyclerView.layoutManager = LinearLayoutManager(this)
        orderAdapter = OrderAdapter(
            onMoveToTop = { app -> moveToTop(app) },
            onHide = { app -> hideApp(app) },
            onStartDrag = { viewHolder -> }
        )
        orderRecyclerView.adapter = orderAdapter
        
        val callback = DragItemCallback(orderAdapter) { from, to ->
            val app = orderedApps.removeAt(from)
            orderedApps.add(to, app)
            orderAdapter.setItems(orderedApps)
        }
        val touchHelper = androidx.recyclerview.widget.ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(orderRecyclerView)
        
        searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        searchAdapter = SearchAdapter { app -> addToOrder(app) }
        searchResultsRecyclerView.adapter = searchAdapter
        
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterApps(s?.toString() ?: "")
            }
        })
        
        btnSave.setOnClickListener {
            saveOrder()
        }
    }
    
    private fun loadApps() {
        allApps = appLoader.loadAllApps().sortedBy { it.label.lowercase() }
        val customOrder = prefsManager.customOrder
        
        if (customOrder.isNotEmpty()) {
            val orderList = customOrder.split(",")
            orderedApps = orderList.mapNotNull { packageName ->
                allApps.find { it.packageName == packageName }
            }.toMutableList()
            
            allApps.filter { app ->
                !orderedApps.any { it.packageName == app.packageName }
            }.forEach { orderedApps.add(it) }
        } else {
            orderedApps = allApps.toMutableList()
        }
        
        orderAdapter.setItems(orderedApps)
    }
    
    private fun filterApps(query: String) {
        if (query.isEmpty()) {
            tvSearchResults.visibility = View.GONE
            searchResultsRecyclerView.visibility = View.GONE
            return
        }
        
        searchResults = allApps.filter { 
            it.label.lowercase().contains(query.lowercase()) 
        }
        
        if (searchResults.isNotEmpty()) {
            tvSearchResults.visibility = View.VISIBLE
            searchResultsRecyclerView.visibility = View.VISIBLE
            searchAdapter.setItems(searchResults)
        } else {
            tvSearchResults.visibility = View.GONE
            searchResultsRecyclerView.visibility = View.GONE
        }
    }
    
    private fun addToOrder(app: AppInfo) {
        if (!orderedApps.any { it.packageName == app.packageName }) {
            orderedApps.add(0, app)
            orderAdapter.setItems(orderedApps)
            searchInput.text.clear()
            tvSearchResults.visibility = View.GONE
            searchResultsRecyclerView.visibility = View.GONE
        }
    }
    
    private fun moveToTop(app: AppInfo) {
        val index = orderedApps.indexOfFirst { it.packageName == app.packageName }
        if (index > 0) {
            orderedApps.removeAt(index)
            orderedApps.add(0, app)
            orderAdapter.setItems(orderedApps)
            orderRecyclerView.scrollToPosition(0)
        }
    }
    
    private fun hideApp(app: AppInfo) {
        prefsManager.hideApp(app.packageName)
        orderedApps.removeAll { it.packageName == app.packageName }
        orderAdapter.setItems(orderedApps)
        Toast.makeText(this, R.string.app_hidden, Toast.LENGTH_SHORT).show()
    }
    
    private fun saveOrder() {
        val orderString = orderedApps.joinToString(",") { it.packageName }
        prefsManager.customOrder = orderString
        Toast.makeText(this, R.string.order_saved, Toast.LENGTH_SHORT).show()
        finish()
    }
    
    class DragItemCallback(
        private val adapter: OrderAdapter,
        private val onMove: (Int, Int) -> Unit
    ) : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(
        androidx.recyclerview.widget.ItemTouchHelper.UP or androidx.recyclerview.widget.ItemTouchHelper.DOWN,
        0
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            onMove(from, to)
            return true
        }
        
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }
    
    inner class OrderAdapter(
        private val onMoveToTop: (AppInfo) -> Unit,
        private val onHide: (AppInfo) -> Unit,
        private val onStartDrag: (RecyclerView.ViewHolder) -> Unit
    ) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {
        
        private var items: List<AppInfo> = emptyList()
        
        fun setItems(items: List<AppInfo>) {
            this.items = items
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_custom_order, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val app = items[position]
            holder.icon.setImageDrawable(app.icon)
            holder.label.text = app.label
            
            holder.btnMoveToTop.setOnClickListener { onMoveToTop(app) }
            holder.btnHide.setOnClickListener { onHide(app) }
            
            holder.dragHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    onStartDrag(holder)
                }
                false
            }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
            val dragHandle: ImageView = itemView.findViewById(R.id.dragHandle)
            val btnMoveToTop: ImageButton = itemView.findViewById(R.id.btnMoveToTop)
            val btnHide: ImageButton = itemView.findViewById(R.id.btnHide)
        }
    }
    
    inner class SearchAdapter(
        private val onAdd: (AppInfo) -> Unit
    ) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {
        
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
            holder.itemView.setOnClickListener { onAdd(app) }
        }
        
        override fun getItemCount(): Int = items.size
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val icon: ImageView = itemView.findViewById(R.id.appIcon)
            val label: TextView = itemView.findViewById(R.id.appLabel)
        }
    }
}
