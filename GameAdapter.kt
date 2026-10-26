
package org.apz.gamevault
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class GameAdapter(private var items: List<Game>, private val onDownload:(Game)->Unit)
: RecyclerView.Adapter<GameAdapter.VH>() {
    class VH(v:View):RecyclerView.ViewHolder(v) {
        val cover:ImageView=v.findViewById(R.id.cover); val name:TextView=v.findViewById(R.id.name)
        val meta:TextView=v.findViewById(R.id.meta); val desc:TextView=v.findViewById(R.id.description)
        val dl:Button=v.findViewById(R.id.download)
    }
    override fun onCreateViewHolder(p:ViewGroup,t:Int)=VH(LayoutInflater.from(p.context).inflate(R.layout.game_item,p,false))
    override fun getItemCount()=items.size
    override fun onBindViewHolder(h:VH,i:Int){
        val g=items[i]; h.name.text=g.name; h.meta.text=listOfNotNull(g.year,g.size).joinToString(" • ")
        h.desc.text=g.description.orEmpty()
        if(g.cover.isNullOrBlank()) h.cover.setImageResource(android.R.drawable.ic_menu_gallery)
        else Glide.with(h.cover).load(g.cover).centerCrop().into(h.cover)
        h.dl.setOnClickListener{onDownload(g)}
    }
    fun submit(x:List<Game>){items=x;notifyDataSetChanged()}
}
