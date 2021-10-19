package transmettal.dev.rental

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.datetime.dateTimePicker
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList


class DescriptionActivity : AppCompatActivity() {

    private val PICK_IMG = 71

    lateinit var btnCheckOut: Button
    lateinit var banyakHari: EditText
    lateinit var sisaWaktu: EditText
    lateinit var picMobil: ImageSlider
    lateinit var uploadCars: ImageView

    private var urlArray = mutableListOf<String>()
    private var imageList = ArrayList<SlideModel>()
    private var ImageList = ArrayList<Uri>()

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

        val namaMobil:EditText = findViewById(R.id.nama_mobil)
        val deskMobil: EditText = findViewById(R.id.deskripsi_mobil)
        val hargaAsli: EditText = findViewById(R.id.harga_asli)
        val inputTanggal : EditText = findViewById(R.id.tglMulai)
        val parentInputTanggal : TextInputLayout = findViewById(R.id.parent_tglMulai)
        val parentBanyakHari : TextInputLayout = findViewById(R.id.parent_jml_input)

        sisaWaktu = findViewById(R.id.sisa_Waktu)
        banyakHari = findViewById(R.id.jml_input)
        btnCheckOut = findViewById(R.id.btn_checkout)
        picMobil = findViewById(R.id.pic_mobil)
        uploadCars = findViewById(R.id.upload_cars)

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

        picMobil.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                Toast.makeText(this@DescriptionActivity, "gambar", Toast.LENGTH_SHORT).show()
                GlobeFunction(this@DescriptionActivity).chooseImage(this@DescriptionActivity)
            }
        })

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
                    sisaWaktu.setOnClickListener {
                        getTime(sisaWaktu)
                    }
                    btnCheckOut.text = "Update"
                }else if (status == "admin" && msgAdmin == "Create"){
                    namaMobil.isFocusableInTouchMode = true
                    deskMobil.isFocusableInTouchMode = true
                    hargaAsli.isFocusableInTouchMode = true
                    uploadCars.visibility = View.VISIBLE
                    picMobil.visibility = View.GONE
                    parentInputTanggal.visibility = View.GONE
                    parentBanyakHari.visibility = View.GONE
                    btnCheckOut.text = msgAdmin
                    sisaWaktu.setOnClickListener {
                        getTime(sisaWaktu)
                    }
                    uploadCars.setOnClickListener {
                        Toast.makeText(this, "Masukan gambar kendaraan", Toast.LENGTH_SHORT).show()
                        GlobeFunction(this).chooseImage(this)
                    }
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


        inputTanggal.setOnClickListener {
            getTime(inputTanggal)
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
                upload()
                Log.d("ArrayCobaan", "$urlArray")
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

    fun getTime(edtText: EditText){
        MaterialDialog(this).show {this
            dateTimePicker(requireFutureDateTime = true) { _, dateTime ->
                val tahun = dateTime.get(Calendar.YEAR)
                val bulan = dateTime.get(Calendar.MONTH)
                val hari = dateTime.get(Calendar.DAY_OF_MONTH)
                val jam = dateTime.get(Calendar.HOUR_OF_DAY)
                val menit = dateTime.get(Calendar.MINUTE)
                val detik = dateTime.get(Calendar.SECOND)
                edtText.setText("$tahun-$bulan-${hari}T$jam:$menit:${detik}Z")
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

    fun upload() {
        val imageFolder = FirebaseStorage.getInstance().reference.child("Kendaraan")
        for (uploads in ImageList.indices) {
            val namaFile = GlobeFunction(this).getRandomString(12)
            val imagename = imageFolder.child("image/$namaFile")
            imagename.putFile(ImageList[uploads]).addOnCompleteListener {
                ImageList.clear()
                urlArray.add(imagename.toString())
                GlobeFunction(this).penyimpanan().reference.child("Kendaraan/$imagename/")
                    .downloadUrl.addOnSuccessListener { uri ->
                        GlobeFunction(this).basisData().collection("kendaraan").document()
                            .set(mapOf("list_foto" to arrayListOf(uri.toString()))).addOnCompleteListener {
                                Toast.makeText(this, "Berhasil upload", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMG) {
            if (resultCode == RESULT_OK) {
                if (data?.clipData != null) {
                    val count = data.clipData!!.itemCount
                    var CurrentImageSelect = 0
                    while (CurrentImageSelect < count) {
                        val imageuri = data.clipData!!.getItemAt(CurrentImageSelect).uri
                        ImageList.add(imageuri)
                        CurrentImageSelect += 1
                    }
                }
            }
        }
    }

}