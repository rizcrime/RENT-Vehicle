package transmettal.dev.rental

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DescriptionActivity : AppCompatActivity() {

    lateinit var btnCheckOut: Button
    lateinit var msgHarga: String
    lateinit var banyakHari: EditText
    lateinit var hasilCheckOut: String
    lateinit var status: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        val imageList = ArrayList<SlideModel>()
        val namaMobil:EditText = findViewById(R.id.nama_mobil)
        val picMobil:ImageSlider = findViewById(R.id.pic_mobil)
        val deskMobil: EditText = findViewById(R.id.deskripsi_mobil)
        val sisaWaktu: EditText = findViewById(R.id.sisa_Waktu)
        val hargaAsli: EditText = findViewById(R.id.harga_asli)
        val inputTanggal : EditText = findViewById(R.id.tglMulai)

        banyakHari = findViewById(R.id.jml_input)
        btnCheckOut = findViewById(R.id.btn_checkout)
        msgHarga = intent.getStringExtra("HARGA").toString()

        val msgGambar = intent.getStringExtra("GAMBAR")
        val msgId = intent.getStringExtra("ID")
        val msgNamaMobil = intent.getStringExtra("NAMA")
        val msgDeskMobil = intent.getStringExtra("DESKRIPSI")
        val msgSisaWaktu = intent.getStringExtra("WAKTU")
        val formatHarga = GlobeFunction(this).rupiah(msgHarga.toInt()) + " /${getString(R.string.hari)}"

        GlobeFunction(this).basisData().collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .get().addOnCompleteListener {
                status = it.result.getString("status").toString()
        }

        namaMobil.setText(msgNamaMobil)
        deskMobil.setText(msgDeskMobil)
        sisaWaktu.setText(msgSisaWaktu)
        hargaAsli.setText(formatHarga)
        GlobeFunction(this).basisData().collection("kendaraan")
            .document(msgId.toString()).get().addOnSuccessListener {
                val a = it["list_foto"] as List<String>
                for (docs in a.indices){
                    imageList.add(SlideModel(a[docs]))
                    picMobil.setImageList(imageList)
                }
            }

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

        val myCalendar: Calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, day)
            val myFormat = "dd MMMM yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)
            inputTanggal.setText(sdf.format(myCalendar.time))
        }

        inputTanggal.setOnClickListener {
            DatePickerDialog(this, dateSetListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        banyakHari.filters = arrayOf(LimitNumbers(1, 30))
        banyakHari.addTextChangedListener(textWatcher)
        btnCheckOut.setOnClickListener {

            if (status != "admin" && btnCheckOut.text != "Rp0" && btnCheckOut.text != "Bayar"
                && inputTanggal.text.toString() != ""){
                startActivity(Intent(this, CheckOutActivity::class.java).apply {
                    putExtra("HARGA", btnCheckOut.text)
                    putExtra("NAMA", msgNamaMobil)
                    putExtra("JUMLAH", banyakHari.text.toString())
                    putExtra("TANGGAL", inputTanggal.text.toString())
                    putExtra("DISKON", "0%")
                    putExtra("GAMBAR", msgGambar)
                })
            }else if (status == "admin"){
                startActivity(Intent(this, CheckOutActivity::class.java).apply {
                    putExtra("HARGA", btnCheckOut.text)
                    putExtra("NAMA", msgNamaMobil)
                    putExtra("JUMLAH", banyakHari.text.toString())
                    putExtra("TANGGAL", inputTanggal.text.toString())
                    putExtra("DISKON", "0%")
                    putExtra("GAMBAR", msgGambar)
                })
            } else{
                GlobeFunction(this).vibratePhone()
                Toast.makeText(this, "Masukan rencana sewa anda", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s?.length == 0 || s?.length == null){
                btnCheckOut.text = "Rp0"
            }else{
                hasilCheckOut = simpleCalc(banyakHari.text.toString().toInt(), msgHarga.toInt()).toString()
                btnCheckOut.text = GlobeFunction(this@DescriptionActivity)
                                    .rupiah(hasilCheckOut.toInt())
            }
        }
    }

    fun simpleCalc(vararg a:Int):Int{
        return a.component1().times(a.component2())
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}