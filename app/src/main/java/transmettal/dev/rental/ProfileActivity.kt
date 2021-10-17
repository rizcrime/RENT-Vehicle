package transmettal.dev.rental

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.DecimalFormat

class ProfileActivity : AppCompatActivity() {

    private var imageView: CircleImageView? = null
    private var loadingLayout: LinearLayout? = null
    private var loading: ProgressBar? = null

    private var validateLayout: LinearLayout? = null
    private var titleValidate: TextInputLayout? = null
    private var edtValidate: EditText? = null
    private var btnValidate: Button? = null
    private var edtEmail: EditText? = null
    private var edtFname: EditText? = null
    private var edtNum: EditText? = null

    private var points: String? = null
    private var status: String? = null
    private var fullname: String? = null
    private var numTelp: String? = null
    private var email: String? = null

    private var pemberiKode = mutableListOf<String>()
    private var kodeAdmin = mutableListOf<String>()
    private var dapatkanPemberiKode = mutableListOf<String>()
    private var kunciKodeAdmin : String? = null

    private var titleItem1: String? = null
    private var titleItem2: String? = null
    private var generateKodeAdmin: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imageView = findViewById(R.id.profile_image)
        loadingLayout = findViewById(R.id.layout_progressbar)
        loading = findViewById(R.id.progressbar)

        validateLayout = findViewById(R.id.layout_validate)
        titleValidate = findViewById(R.id.title_validate)
        edtValidate = findViewById(R.id.edt_kode_admin)
        btnValidate = findViewById(R.id.btn_validasi)

        generateKodeAdmin = GlobeFunction(this).getRandomString(5)

        val showEmail: TextInputLayout = findViewById(R.id.show_email)
        val showFname: TextInputLayout = findViewById(R.id.show_fullname)
        val showNum: TextInputLayout = findViewById(R.id.show_telp)
        val showStatus: TextInputLayout = findViewById(R.id.show_status)
        val point: TextView = findViewById(R.id.points)
        val btnUpdate: Button = findViewById(R.id.btn_update)

        edtEmail = findViewById(R.id.edt_email)
        edtFname = findViewById(R.id.edt_fullname)
        edtNum = findViewById(R.id.edt_numtelp)
        val edtStatus: EditText = findViewById(R.id.edt_status)

        GlobeFunction(this).basisData().collection("users").get()
            .addOnSuccessListener {
                for (document in it){
                    GlobeFunction(this).basisData().collection("users")
                        .document(document.id).get().addOnSuccessListener { data_kode ->
                            pemberiKode.add(data_kode.getString("fullname").toString())
                            dapatkanPemberiKode.add(data_kode.getString("id").toString())
                            kodeAdmin.add(data_kode.getString("key_admin").toString())
                        }
                }
        }

        GlobeFunction(this).basisData().collection("users")
            .document(FirebaseAuth.getInstance().uid.toString()).get().addOnCompleteListener {
                fullname = it.result.getString("fullname")
                status = it.result.getString("status")
                numTelp = it.result.getString("num_telp")
                email = GlobeFunction(this).autentikasi().currentUser?.email
                points = it.result.getString("poin")
                kunciKodeAdmin = it.result.getString("key_admin")

                val decim = DecimalFormat("#,###.##")
                val money = points?.toInt()?.times(100000)

                showFname.hint = fullname
                showStatus.hint = status
                showEmail.hint = email
                showNum.hint = numTelp

                point.text = "Point Anda $points\natau\nRp.${decim.format(money)}"

                clicker(edtFname!!, showFname, "Nama Lengkap", fullname.toString())
                clicker(edtEmail!!, showEmail, "Email", email.toString())
                clicker(edtNum!!, showNum, "Nomor Telepon", numTelp.toString())
                clicker(edtStatus, showStatus, "Status", status.toString())
            }

        btnUpdate.setOnClickListener {
            updateData()
        }

        getFotoProfile()

    }

    private fun updateData(){
        if(!haramText(edtFname?.text.toString(), fullname!!) && !haramText(edtNum?.text.toString(), fullname!!)){
            GlobeFunction(this).basisData().collection("users")
                .document(GlobeFunction(this).autentikasi().uid.toString())
                .update(mapOf("fullname" to edtFname?.text.toString(),
                              "num_telp" to edtNum?.text.toString())).addOnCompleteListener {
                    Toast.makeText(this, "Nama lengkap dan Nomor Telepon terganti", Toast.LENGTH_LONG).show()
                }
            GlobeFunction(this).reloadPage(this)
        } else if(!haramText(edtFname?.text.toString(), fullname!!)) {
            GlobeFunction(this).basisData().collection("users")
                .document(GlobeFunction(this).autentikasi().uid.toString())
                .update(mapOf("fullname" to edtFname?.text.toString())).addOnCompleteListener {
                    Toast.makeText(this, "Nama lengkap terganti", Toast.LENGTH_LONG).show()
                }
            GlobeFunction(this).reloadPage(this)
        } else if(!haramText(edtNum?.text.toString(), fullname!!)) {
            GlobeFunction(this).basisData().collection("users")
                .document(GlobeFunction(this).autentikasi().uid.toString())
                .update(mapOf("num_telp" to edtNum?.text.toString())).addOnCompleteListener {
                    Toast.makeText(this, "Nomor Telepon terganti", Toast.LENGTH_LONG).show()
                }
            GlobeFunction(this).reloadPage(this)
        }else{Toast.makeText(this, "Tidak ada data baru", Toast.LENGTH_LONG).show()}
    }

    private fun clicker(edtText: EditText, textInputLayout: TextInputLayout, stringBefore: String, stringAfter: String){
        edtText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                textInputLayout.hint = stringBefore
            } else {
                textInputLayout.hint = stringAfter
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val itemAdmin = menu?.findItem(R.id.register_admin)
        val itemMitra = menu?.findItem(R.id.register_broker)
        titleItem2 = "Referensikan kode $status"
        titleItem1 = "Daftar Admin"
        if (status == "admin") {
            itemAdmin?.title = titleItem2
            itemMitra?.isVisible = false
        }
        else if(status == "mitra") {
            itemMitra?.isVisible = false
            itemAdmin?.title = titleItem1
        }else {
            itemAdmin?.title = "Daftar Admin"
        }
            return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.register_admin -> {
                if (titleItem2 == "Referensikan kode admin"){
                    GlobeFunction(this).copyTextToClipboard(kunciKodeAdmin.toString())
                    Toast.makeText(this, "kode rahasia tersalin", Toast.LENGTH_LONG).show()
                }else{
                    validateLayout?.visibility = View.VISIBLE
                    btnValidate?.setOnClickListener {
                        for (out in kodeAdmin.indices){
                            if(edtValidate?.text.toString() == kodeAdmin[out] && status != "admin"
                                && generateKodeAdmin != kodeAdmin[out]){
                                GlobeFunction(this).basisData().collection("users")
                                    .document(GlobeFunction(this).autentikasi().uid.toString())
                                    .update(mapOf("status" to "admin",
                                        "key_admin" to generateKodeAdmin,
                                        "reference_by" to dapatkanPemberiKode[out])).addOnCompleteListener {
                                        Toast.makeText(this, "Anda dimasukan oleh ${pemberiKode[out]}", Toast.LENGTH_LONG).show()
                                        Toast.makeText(this, "Selamat, sekarang anda Admin kami", Toast.LENGTH_LONG).show()
                                        validateLayout?.visibility = View.GONE
                                        GlobeFunction(this).reloadPage(this)
                                    }
                                break
                            }else if(edtValidate?.text.toString() != kodeAdmin[out]){
                                Toast.makeText(this, "Kode salah", Toast.LENGTH_LONG).show()
                                validateLayout?.visibility = View.GONE
                            }
                        }
                    }
                }
                return true
            }
            R.id.register_broker -> {
                val hasil: Double = 10.minus(points?.toDouble()!!)
                when {
                    points?.toDouble()!!<10 -> {
                        Toast.makeText(this, "Belum Cukup, anda Butuh $hasil point lagi", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        GlobeFunction(this).basisData().collection("users")
                            .document(GlobeFunction(this).autentikasi().uid.toString())
                            .update("status", "mitra").addOnCompleteListener {
                                Toast.makeText(this, "Selamat, sekarang anda Mitra kami", Toast.LENGTH_LONG).show()
                            }
                    }
                }
                return true
            }
            R.id.delete_account -> {
                val builder = AlertDialog.Builder(this)
                if(status == "mitra"){
                    Toast.makeText(this, "Hanya admin/superadmin yang boleh menghapusnya", Toast.LENGTH_LONG).show()
                }else if(status == "admin"){
                    Toast.makeText(this, "Hanya superadmin yang boleh menghapusnya", Toast.LENGTH_LONG).show()
                }else{
                    validateLayout?.visibility = View.VISIBLE
                    titleValidate?.hint = "Masukan Password"
                    btnValidate?.setOnClickListener {
                        val credential = EmailAuthProvider.getCredential(GlobeFunction(this)
                            .autentikasi().currentUser?.uid.toString(), edtValidate?.text.toString())
                        GlobeFunction(this).autentikasi().currentUser?.reauthenticate(credential)
                            ?.addOnCompleteListener {
                                builder.setTitle("Menghapus Akun")
                                builder.setMessage("Apakah kamu yakin?")
                                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                                    GlobeFunction(this).autentikasi().currentUser?.delete()?.addOnCompleteListener {
                                        if(it.isSuccessful){
                                            Toast.makeText(this, "Data Terhapus", Toast.LENGTH_LONG).show()
                                            GlobeFunction(this).exitMain(this)
                                            validateLayout?.visibility = View.GONE
                                        }else{
                                            Toast.makeText(this, "Password anda salah, akun gagal terhapus ", Toast.LENGTH_LONG).show()
                                            validateLayout?.visibility = View.GONE
                                        }
                                    }
                                }
                                builder.setNegativeButton(android.R.string.no) { dialog, which ->
                                    Toast.makeText(applicationContext,
                                        android.R.string.no, Toast.LENGTH_SHORT).show()
                                }
                                builder.show()
                            }
                    }
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    private fun getFotoProfile(){
        val refStorage = GlobeFunction(this).penyimpanan().reference
        loadingLayout?.visibility = View.VISIBLE
        refStorage.child("foto_profile/${GlobeFunction(this).autentikasi()
            .uid.toString()}").downloadUrl.addOnSuccessListener {
                loadingLayout?.visibility = View.GONE
                Picasso.get().load(it.toString()).into(imageView)
            }
    }

    fun haramText(text: String, nilai: String):Boolean{
        if (text == "" || text == null || text.length < 3){
            return true
        }else if(text == nilai){
            return true
        }
        return false
    }

}