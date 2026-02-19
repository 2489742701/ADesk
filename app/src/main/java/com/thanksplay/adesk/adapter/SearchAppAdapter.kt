package com.thanksplay.adesk.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thanksplay.adesk.R
import com.thanksplay.adesk.model.AppInfo
import java.text.Collator
import java.util.Locale

class SearchAppAdapter(
    private val context: Context,
    private val columns: Int = 6
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    
    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_APP = 1
    }
    
    private var items: MutableList<ListItem> = mutableListOf()
    
    fun setItems(apps: List<AppInfo>) {
        items.clear()
        
        val groupedApps = apps.groupBy { app ->
            getFirstLetter(app.label)
        }.toSortedMap(compareBy { it })
        
        groupedApps.forEach { (letter, appList) ->
            items.add(ListItem.Header(letter))
            appList.sortedBy { it.label.lowercase() }.forEach { app ->
                items.add(ListItem.AppItem(app))
            }
        }
        
        notifyDataSetChanged()
    }
    
    private fun getFirstLetter(label: String): String {
        if (label.isEmpty()) return "#"
        
        val firstChar = label[0]
        
        if (firstChar in 'A'..'Z') return firstChar.toString()
        if (firstChar in 'a'..'z') return firstChar.uppercaseChar().toString()
        
        if (firstChar.code in 0x4E00..0x9FFF) {
            val pinyin = getPinyin(firstChar)
            if (pinyin.isNotEmpty()) {
                return pinyin[0].uppercaseChar().toString()
            }
        }
        
        if (firstChar.isLetter()) {
            return firstChar.uppercaseChar().toString()
        }
        
        return "#"
    }
    
    private fun getPinyin(c: Char): String {
        val collator = Collator.getInstance(Locale.CHINESE)
        val pinyinMap = mapOf(
            '阿' to "A", '啊' to "A", '安' to "A", '爱' to "A", '艾' to "A",
            '八' to "B", '百' to "B", '北' to "B", '本' to "B", '边' to "B",
            '才' to "C", '长' to "C", '城' to "C", '出' to "C", '从' to "C",
            '大' to "D", '道' to "D", '地' to "D", '电' to "D", '东' to "D",
            '二' to "E",
            '发' to "F", '方' to "F", '非' to "F", '风' to "F", '福' to "F",
            '高' to "G", '个' to "G", '工' to "G", '公' to "G", '国' to "G",
            '海' to "H", '和' to "H", '河' to "H", '黑' to "H", '红' to "H",
            '机' to "J", '基' to "J", '即' to "J", '几' to "J", '家' to "J",
            '开' to "K", '看' to "K", '可' to "K", '空' to "K", '口' to "K",
            '来' to "L", '老' to "L", '乐' to "L", '里' to "L", '力' to "L",
            '马' to "M", '美' to "M", '们' to "M", '名' to "M", '明' to "M",
            '南' to "N", '你' to "N", '年' to "N", '那' to "N", '能' to "N",
            '欧' to "O",
            '平' to "P", '朋' to "P", '普' to "P",
            '七' to "Q", '期' to "Q", '起' to "Q", '前' to "Q", '去' to "Q",
            '人' to "R", '日' to "R", '然' to "R", '让' to "R", '热' to "R",
            '三' to "S", '上' to "S", '生' to "S", '时' to "S", '手' to "S",
            '他' to "T", '天' to "T", '通' to "T", '同' to "T", '头' to "T",
            '为' to "W", '文' to "W", '我' to "W", '无' to "W", '五' to "W",
            '西' to "X", '下' to "X", '小' to "X", '新' to "X", '学' to "X",
            '一' to "Y", '以' to "Y", '有' to "Y", '于' to "Y", '月' to "Y",
            '在' to "Z", '中' to "Z", '这' to "Z", '主' to "Z", '子' to "Z"
        )
        
        return pinyinMap[c] ?: try {
            val chars = charArrayOf(c)
            val sb = StringBuilder()
            for (ch in chars) {
                if (ch.code in 0x4E00..0x9FFF) {
                    val pinyin = getSimplePinyin(ch)
                    sb.append(pinyin)
                } else {
                    sb.append(ch)
                }
            }
            sb.toString()
        } catch (e: Exception) {
            "#"
        }
    }
    
    private fun getSimplePinyin(c: Char): String {
        val code = c.code
        return when {
            code in 0x4E00..0x4EFF -> "A"
            code in 0x4F00..0x4FFF -> "B"
            code in 0x5000..0x50FF -> "C"
            code in 0x5100..0x51FF -> "D"
            code in 0x5200..0x52FF -> "E"
            code in 0x5300..0x53FF -> "F"
            code in 0x5400..0x54FF -> "G"
            code in 0x5500..0x55FF -> "H"
            code in 0x5600..0x56FF -> "J"
            code in 0x5700..0x57FF -> "K"
            code in 0x5800..0x58FF -> "L"
            code in 0x5900..0x59FF -> "M"
            code in 0x5A00..0x5AFF -> "N"
            code in 0x5B00..0x5BFF -> "O"
            code in 0x5C00..0x5CFF -> "P"
            code in 0x5D00..0x5DFF -> "Q"
            code in 0x5E00..0x5EFF -> "R"
            code in 0x5F00..0x5FFF -> "S"
            code in 0x6000..0x60FF -> "T"
            code in 0x6100..0x61FF -> "W"
            code in 0x6200..0x62FF -> "X"
            code in 0x6300..0x63FF -> "Y"
            code in 0x6400..0x64FF -> "Z"
            code in 0x6500..0x65FF -> "A"
            code in 0x6600..0x66FF -> "B"
            code in 0x6700..0x67FF -> "C"
            code in 0x6800..0x68FF -> "D"
            code in 0x6900..0x69FF -> "G"
            code in 0x6A00..0x6AFF -> "H"
            code in 0x6B00..0x6BFF -> "J"
            code in 0x6C00..0x6CFF -> "L"
            code in 0x6D00..0x6DFF -> "M"
            code in 0x6E00..0x6EFF -> "N"
            code in 0x6F00..0x6FFF -> "P"
            code in 0x7000..0x70FF -> "Q"
            code in 0x7100..0x71FF -> "R"
            code in 0x7200..0x72FF -> "S"
            code in 0x7300..0x73FF -> "T"
            code in 0x7400..0x74FF -> "W"
            code in 0x7500..0x75FF -> "X"
            code in 0x7600..0x76FF -> "Y"
            code in 0x7700..0x77FF -> "Z"
            code in 0x7800..0x78FF -> "A"
            code in 0x7900..0x79FF -> "B"
            code in 0x7A00..0x7AFF -> "C"
            code in 0x7B00..0x7BFF -> "D"
            code in 0x7C00..0x7CFF -> "E"
            code in 0x7D00..0x7DFF -> "F"
            code in 0x7E00..0x7EFF -> "G"
            code in 0x7F00..0x7FFF -> "H"
            code in 0x8000..0x80FF -> "J"
            code in 0x8100..0x81FF -> "K"
            code in 0x8200..0x82FF -> "L"
            code in 0x8300..0x83FF -> "M"
            code in 0x8400..0x84FF -> "N"
            code in 0x8500..0x85FF -> "O"
            code in 0x8600..0x86FF -> "P"
            code in 0x8700..0x87FF -> "Q"
            code in 0x8800..0x88FF -> "R"
            code in 0x8900..0x89FF -> "S"
            code in 0x8A00..0x8AFF -> "T"
            code in 0x8B00..0x8BFF -> "W"
            code in 0x8C00..0x8CFF -> "X"
            code in 0x8D00..0x8DFF -> "Y"
            code in 0x8E00..0x8EFF -> "Z"
            code in 0x8F00..0x8FFF -> "A"
            code in 0x9000..0x90FF -> "B"
            code in 0x9100..0x91FF -> "C"
            code in 0x9200..0x92FF -> "D"
            code in 0x9300..0x93FF -> "E"
            code in 0x9400..0x94FF -> "F"
            code in 0x9500..0x95FF -> "G"
            code in 0x9600..0x96FF -> "H"
            code in 0x9700..0x97FF -> "J"
            code in 0x9800..0x98FF -> "K"
            code in 0x9900..0x99FF -> "L"
            code in 0x9A00..0x9AFF -> "M"
            code in 0x9B00..0x9BFF -> "N"
            code in 0x9C00..0x9CFF -> "O"
            code in 0x9D00..0x9DFF -> "P"
            code in 0x9E00..0x9EFF -> "Q"
            code in 0x9F00..0x9FFF -> "R"
            else -> "#"
        }
    }
    
    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> TYPE_HEADER
            is ListItem.AppItem -> TYPE_APP
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_section_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_search_app_grid, parent, false)
                AppViewHolder(view)
            }
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ListItem.Header -> {
                (holder as HeaderViewHolder).headerText.text = item.letter
            }
            is ListItem.AppItem -> {
                val appHolder = holder as AppViewHolder
                appHolder.icon.setImageDrawable(item.app.icon)
                appHolder.label.text = item.app.label
                appHolder.itemView.setOnClickListener {
                    launchApp(item.app)
                }
            }
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
    
    fun getSpanSize(position: Int): Int {
        return when (items[position]) {
            is ListItem.Header -> columns
            is ListItem.AppItem -> 1
        }
    }
    
    private sealed class ListItem {
        data class Header(val letter: String) : ListItem()
        data class AppItem(val app: AppInfo) : ListItem()
    }
    
    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headerText: TextView = itemView.findViewById(R.id.headerText)
    }
    
    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.appIcon)
        val label: TextView = itemView.findViewById(R.id.appLabel)
    }
}
