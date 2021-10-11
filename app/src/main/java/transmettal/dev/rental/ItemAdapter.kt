package transmettal.dev.rental

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ItemAdapter(private var context: Context,
                  private var id: MutableList<String>,
                  private var tarif: MutableList<String>,
                  private var judul : MutableList<String>,
                  private var deskripsi: MutableList<String>,
                  private var gambar : MutableList<String>,
                  private var waktu: MutableList<String>,
                  private var status: String = "") :
    RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var txtHarga: TextView? = view.findViewById(R.id.harga_sewa)
        var txtJudul: TextView? = view.findViewById(R.id.nama_mobil)
        var image: ImageView = view.findViewById(R.id.pic_mobil)
        var txtStatus: TextView? = view.findViewById(R.id.status_item)
        var rvMain : RelativeLayout? = view.findViewById(R.id.rv_main)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.model_rv_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val convertRp = GlobeFunction(context).rupiah(tarif[position].toInt())
        viewHolder.txtHarga?.text = convertRp + "/${context.getString(R.string.hari)}"
        viewHolder.txtJudul?.text = judul[position]
        viewHolder.txtStatus?.text = waktu[position]
        viewHolder.txtStatus?.setBackgroundColor(Color.BLACK)
        Picasso.get().load(gambar[position]).into(viewHolder.image)
        if (waktu[position] == context.getString(R.string.tersedia_desc)){
            viewHolder.txtStatus?.setTextColor(Color.GREEN)
        }else{
            viewHolder.txtStatus?.setTextColor(Color.RED)
        }
        val activity = viewHolder.itemView.context as Activity
        val intent = Intent(activity, DescriptionActivity::class.java).apply {
            putExtra("ID", id[position])
            putExtra("HARGA", tarif[position])
            putExtra("NAMA", judul[position])
            putExtra("WAKTU", waktu[position] )
            putExtra("DESKRIPSI", deskripsi[position] )
            putExtra("GAMBAR", gambar[position])
        }
//        DatabaseSQLite(viewHolder.itemView.context).createKendaraan(id[position].toInt(), gambar[position],
//            judul[position], tarif[position],deskripsi[position],waktu[position])
        viewHolder.rvMain?.setOnClickListener {
            if (status == ""){
                GlobeFunction(context).vibratePhone()
                Toast.makeText(context, "Fitur dibatasi", Toast.LENGTH_SHORT).show()
            }else{
                activity.startActivity(intent)
            }
        }

    }

    override fun getItemCount() = tarif.size

}