package transmettal.dev.rental

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.collections.ArrayList

class DescriptionActivity : AppCompatActivity() {

    private val PICK_IMG = 71

    private lateinit var btnCheckOut: Button
    private lateinit var banyakHari: EditText
    private lateinit var sisaWaktu: EditText
    private lateinit var picMobil: ImageSlider
    private lateinit var uploadCars: ImageView
    private lateinit var edtDiskon: EditText
    private lateinit var edtMitra: EditText

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

    private var refKode = ArrayList<String>()
    private var mitra = ArrayList<String>()

    private var getPersen = ArrayList<String>()
    private var getKodePromo = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        diskon()
        referenceMitra()

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
        edtDiskon = findViewById(R.id.edt_discount)
        edtMitra = findViewById(R.id.edt_mitra)

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
            if (status != "admin" && btnCheckOut.text != "Rp0" && btnCheckOut.text != "Bayar"
                && inputTanggal.text.toString() != "" || seleksiDiskon() != "Tidak ada" || seleksiMitra() != "Tidak ada"){
                startActivity(Intent(this, CheckOutActivity::class.java).apply {
                    putExtra("HARGA", btnCheckOut.text)
                    putExtra("NAMA", msgNamaMobil)
                    putExtra("JUMLAH", banyakHari.text.toString())
                    putExtra("TANGGAL", inputTanggal.text.toString())
                    putExtra("DISKON", seleksiDiskon())
                    putExtra("BROKER", seleksiMitra())
                    putExtra("GAMBAR", msgGambar)
                })
            }else if (btnCheckOut.text == "Update"){
                update(namaMobil.text.toString(), deskMobil.text.toString(),
                    GlobeFunction(this).revertRupiah(hargaAsli.text.toString()),
                    GlobeFunction(this).fbCalendar(sisaWaktu.text.toString()))
            }else if(btnCheckOut.text == "Create"){
                upload()
            }
            else{
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
                hasilCheckOut = simpleCalc(banyakHari.text.toString().toInt(),
                                           msgHarga.toInt()).toString()
                btnCheckOut.text = GlobeFunction(this@DescriptionActivity).rupiah(hasilCheckOut.toInt())
            }
        }
        override fun afterTextChanged(s: Editable?) {}
    }

    fun getTime(edtText: EditText){
        MaterialDialog(this).show {
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

    fun diskon(){
        GlobeFunction(this).basisData().collection("diskon").get()
            .addOnSuccessListener { dataDiskon ->
                for (datas in dataDiskon){
                    GlobeFunction(this).basisData().collection("diskon")
                        .document(datas.id).get().addOnSuccessListener {
                            getPersen.add(it.getString("diskon_persen").toString())
                            getKodePromo.add(it.getString("kode_promo").toString())
                        }
                }
            }
    }

    fun seleksiDiskon():String{
        for (i in getKodePromo.indices) {
            if (getKodePromo[i] == edtDiskon.text.toString()){
                return getPersen[i]
            }
        }
        Toast.makeText(this, "Tidak ada diskon", Toast.LENGTH_LONG).show()
        return "Tidak ada"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMG) {
            if (resultCode == RESULT_OK) {
                if (data?.clipData != null) {
                    val count = data.clipData!!.itemCount
                    var currentImageSelect = 0
                    while (currentImageSelect < count) {
                        val imageuri = data.clipData!!.getItemAt(currentImageSelect).uri
                        ImageList.add(imageuri)
                        currentImageSelect += 1
                    }
                }
            }
        }
    }

    fun referenceMitra(){
        GlobeFunction(this).basisData().collection("users").get()
            .addOnSuccessListener { dataRef ->
                for (datas in dataRef){
                    GlobeFunction(this).basisData().collection("users")
                        .document(datas.id).get().addOnSuccessListener {
                            refKode.add(it.getString("special_kode").toString())
                            mitra.add(it.getString("fullname").toString())
                        }
                }
            }
    }

    fun seleksiMitra():String{
        for (i in refKode.indices) {
            if (refKode[i] == edtMitra.text.toString()){
                return mitra[i]
            }
        }
        Toast.makeText(this, "Tidak ada mitra", Toast.LENGTH_LONG).show()
        return "Tidak ada"
    }

}