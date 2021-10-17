package transmettal.dev.rental

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.collections.ArrayList

class DescriptionActivity : AppCompatActivity() {

    lateinit var btnCheckOut: Button
    lateinit var banyakHari: EditText
    lateinit var sisaWaktu: EditText

    private var msgHarga: String = ""
    private var hasilCheckOut: String = ""
    private var status: String = ""
    private var msgId: String = ""
    private var msgNamaMobil: String = ""
    private var msgDeskMobil: String = ""
    private var msgSisaWaktu: String = ""
    private var msgGambar: String = ""
    private var msgAdmin: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        val imageList = ArrayList<SlideModel>()
        val namaMobil:EditText = findViewById(R.id.nama_mobil)
        val picMobil:ImageSlider = findViewById(R.id.pic_mobil)
        val deskMobil: EditText = findViewById(R.id.deskripsi_mobil)
        val hargaAsli: EditText = findViewById(R.id.harga_asli)
        val inputTanggal : EditText = findViewById(R.id.tglMulai)
        val parentInputTanggal : TextInputLayout = findViewById(R.id.parent_tglMulai)
        val parentBanyakHari : TextInputLayout = findViewById(R.id.parent_jml_input)

        sisaWaktu = findViewById(R.id.sisa_Waktu)
        banyakHari = findViewById(R.id.jml_input)
        btnCheckOut = findViewById(R.id.btn_checkout)

        msgHarga = intent.getStringExtra("HARGA").toString()
        msgGambar = intent.getStringExtra("GAMBAR").toString()
        msgId = intent.getStringExtra("ID").toString()
        msgNamaMobil = intent.getStringExtra("NAMA").toString()
        msgDeskMobil = intent.getStringExtra("DESKRIPSI").toString()
        msgSisaWaktu = intent.getStringExtra("WAKTU").toString()
        msgAdmin = intent.getStringExtra("TOMBOL").toString()

        hargaAsli.onFocusChangeListener = OnFocusChangeListener { view: View, _: Boolean ->
            if (view.isFocused) {
                hargaAsli.setText(GlobeFunction(this).revertRupiah(hargaAsli.text.toString()))
            }else{
                hargaAsli.setText(GlobeFunction(this).rupiah(hargaAsli.text.toString().toInt()))
            }
        }

        GlobeFunction(this).basisData().collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .get().addOnCompleteListener {
                status = it.result.getString("status").toString()
                if (status == "admin" && msgAdmin != "Create"){
                    namaMobil.isFocusableInTouchMode = true
                    deskMobil.isFocusableInTouchMode = true
                    hargaAsli.isFocusableInTouchMode = true
                    parentInputTanggal.visibility = View.GONE
                    parentBanyakHari.visibility = View.GONE
                    btnCheckOut.text = "Update"
                }else{
                    namaMobil.isFocusableInTouchMode = true
                    deskMobil.isFocusableInTouchMode = true
                    hargaAsli.isFocusableInTouchMode = true
                    parentInputTanggal.visibility = View.GONE
                    parentBanyakHari.visibility = View.GONE
                    btnCheckOut.text = msgAdmin
                }
        }

        try{
            namaMobil.setText(msgNamaMobil)
            deskMobil.setText(msgDeskMobil)
            sisaWaktu.setText(msgSisaWaktu)
            hargaAsli.setText(GlobeFunction(this).rupiah(msgHarga.toInt()))
            GlobeFunction(this).basisData().collection("kendaraan")
                .document(msgId).get().addOnSuccessListener {
                    val a = it["list_foto"] as List<String>
                    for (docs in a.indices){
                        imageList.add(SlideModel(a[docs]))
                        picMobil.setImageList(imageList)
                    }
                }
        }catch (e:Exception){
            namaMobil.setText("")
            deskMobil.setText("")
            sisaWaktu.setText("")
        }

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)


//        inputTanggal.setOnClickListener {
//            GlobeFunction(this).datePicker(inputTanggal)
//        }
//
        sisaWaktu.setOnClickListener {
            MaterialDialog(this).show {this
                dateTimePicker(requireFutureDateTime = true) { _, dateTime ->
                    val tahun = dateTime.get(Calendar.YEAR)
                    val bulan = dateTime.get(Calendar.MONTH)
                    val hari = dateTime.get(Calendar.DAY_OF_MONTH)
                    val jam = dateTime.get(Calendar.HOUR_OF_DAY)
                    val menit = dateTime.get(Calendar.MINUTE)
                    val detik = dateTime.get(Calendar.SECOND)
                    sisaWaktu.setText("$tahun-$bulan-${hari}T$jam:$menit:${detik}Z")
                }
            }
        }

        banyakHari.filters = arrayOf(LimitNumbers(1, 30))
        banyakHari.addTextChangedListener(textWatcher)
        btnCheckOut.setOnClickListener {

            if (status != "admin" && btnCheckOut.text != "Rp0"
                && btnCheckOut.text != "Bayar" && inputTanggal.text.toString() != ""){
                startActivity(Intent(this, CheckOutActivity::class.java).apply {
                    putExtra("HARGA", btnCheckOut.text)
                    putExtra("NAMA", msgNamaMobil)
                    putExtra("JUMLAH", banyakHari.text.toString())
                    putExtra("TANGGAL", inputTanggal.text.toString())
                    putExtra("DISKON", "0%")
                    putExtra("GAMBAR", msgGambar)
                })
            }else if (btnCheckOut.text == "Update"){
                update(namaMobil.text.toString(), deskMobil.text.toString(),
                    GlobeFunction(this).revertRupiah(hargaAsli.text.toString()),
                    GlobeFunction(this).fbCalendar(sisaWaktu.text.toString()))
            }else if(btnCheckOut.text == "Create"){

            } else{
                GlobeFunction(this).vibratePhone()
                Toast.makeText(this, "Masukan rencana sewa anda", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s?.length == 0 || s?.length == null){
                btnCheckOut.text = "Rp0"
            }else{
                hasilCheckOut = simpleCalc(banyakHari.text.toString().toInt(), msgHarga.toInt()).toString()
                btnCheckOut.text = GlobeFunction(this@DescriptionActivity).rupiah(hasilCheckOut.toInt())
            }
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    fun simpleCalc(vararg a:Int):Int{
        return a.component1().times(a.component2())
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun update(namaMobil: String = msgNamaMobil, deskripsi: String = msgDeskMobil,
                       harga: String = msgHarga, waktu_sewa: Date? = msgSisaWaktu as Date){
        GlobeFunction(this).basisData().collection("kendaraan")
            .document(msgId)
            .update(mapOf("deskripsi" to deskripsi, "judul" to namaMobil, "tarif" to harga,
                          "waktu_sewa" to waktu_sewa))
            .addOnCompleteListener {
                Toast.makeText(this, "Update Sukses", Toast.LENGTH_SHORT).show()
                finish()
                startActivity(Intent(this, HomeActivity::class.java))
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "$e", Toast.LENGTH_LONG).show() }
    }

}