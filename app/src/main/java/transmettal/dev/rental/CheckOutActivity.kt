package transmettal.dev.rental

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import java.io.IOException

class CheckOutActivity : AppCompatActivity() {

    private var filePath: Uri? = null
    private var imageView: ImageView? = null
    private var diskon :Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_out)

        val imgMobil: ImageView = findViewById(R.id.img_mobil)
        val txtJudul: TextView = findViewById(R.id.nama_mobil)
        val txtJumlah: TextView = findViewById(R.id.lama_sewa)
        val tglBooking: TextView = findViewById(R.id.tanggal_boking)
        val txtDiskon: TextView = findViewById(R.id.discount)
        val txtHarga: TextView = findViewById(R.id.harga_total)
        val txtPetunjuk: TextView = findViewById(R.id.ketInfo)
        val txtNamaPenyewa: TextView = findViewById(R.id.nama_penyewa)
        val txtNamaBroker: TextView = findViewById(R.id.nama_broker)
        val judulKodeTf: TextView = findViewById(R.id.judul_kode)
        imageView = findViewById(R.id.upload_transfer)

        val petunjukTf: ImageSlider = findViewById(R.id.petunjuk_transfer)
        val imageList = ArrayList<SlideModel>()

        val msgGambar = intent.getStringExtra("GAMBAR")
        val msgJudul = intent.getStringExtra("NAMA")
        val msgJumlah = intent.getStringExtra("JUMLAH")
        val msgBooke = intent.getStringExtra("TANGGAL")
        val msgDiskon = intent.getStringExtra("DISKON")
        val msgHarga = intent.getStringExtra("HARGA")
        val msgBroker = intent.getStringExtra("BROKER")

        Picasso.get().load(msgGambar).into(imgMobil)
        txtJudul.text = msgJudul
        txtJumlah.text = msgJumlah
        tglBooking.text = msgBooke
        txtDiskon.text = "$msgDiskon%"
        txtNamaBroker.text = msgBroker

        if (msgDiskon == "Tidak ada"){
            txtJumlah.text = msgJumlah
            txtDiskon.text = msgDiskon
            txtHarga.text = msgHarga
        }else{
            diskon = msgDiskon!!.toDouble()
            val harga = GlobeFunction(this).revertRupiah(msgHarga.toString())
            val hargaAkhir = harga.toDouble()*diskon/100
            txtHarga.text = GlobeFunction(this).rupiah(hargaAkhir.toInt())
        }

        GlobeFunction(this).basisData().collection("info")
            .document("101").get().addOnSuccessListener {
                val a = it["foto"] as List<String>
                txtPetunjuk.text = it.getString("deskripsi")
                for (docs in a.indices){
                    imageList.add(SlideModel(a[docs]))
                    petunjukTf.setImageList(imageList)
                }
            }

        GlobeFunction(this).basisData().collection("users")
            .document(FirebaseAuth.getInstance().uid.toString()).get().addOnCompleteListener {
                txtNamaPenyewa.text = it.result.getString("fullname")
            }

        imageView?.setOnClickListener {
            GlobeFunction(this).chooseImage(this)
        }
        judulKodeTf.text = "Kode Referensi Pembayaran Anda\n${GlobeFunction(this).getRandomString(10)}"

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 71 && resultCode == RESULT_OK && data != null && data.data != null) {
            filePath = data.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imageView?.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}