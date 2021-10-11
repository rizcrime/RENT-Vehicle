package transmettal.dev.rental

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.net.URLEncoder


open class HomeActivity : AppCompatActivity() {

    private var id = mutableListOf<String>()
    private var firebaseFirestore: FirebaseFirestore? = null
    private var firebaseStorage : FirebaseStorage? = null
    private var fullname: String? = null
    open var status: String = ""
    open var header: TextView? = null
    private var toolbar: Toolbar? = null
    private var content = Html.fromHtml("<u><i>Pengunjung, silahkan klik untuk masuk/daftar & nikmati seluruh fitur</i></u>")
    private var iconProfile: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val gambar = mutableListOf<String>()
        val tarif = mutableListOf<String>()
        val judul = mutableListOf<String>()
        val deskripsi = mutableListOf<String>()
        val waktu = mutableListOf<String>()

        val rvHome : RecyclerView = findViewById(R.id.rv_home)
        val fabChat: FloatingActionButton = findViewById(R.id.fab_msg)

        iconProfile = findViewById(R.id.icon_profile)
        header = findViewById(R.id.header_title)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        firebaseStorage = FirebaseStorage.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        rvHome.layoutManager = LinearLayoutManager(this)

        firebaseFirestore?.collection("kendaraan")?.get()?.addOnSuccessListener { satu ->
                for (document in satu){
                    firebaseFirestore?.collection("kendaraan")?.document(document.id)
                        ?.get()?.addOnSuccessListener { dua ->
                            id.add(dua.getString("id").toString())
                            gambar.add(dua.getString("foto").toString())
                            tarif.add(dua.getString("tarif").toString())
                            judul.add(dua.getString("judul").toString())
                            deskripsi.add(dua.getString("deskripsi").toString())
                            waktu.add(GlobeFunction(this).tanggal(dua["waktu_sewa"] as Timestamp))
                            rvHome.adapter = ItemAdapter(this, id, tarif, judul,
                                                            deskripsi, gambar, waktu, status)
                        }
                }
            }

        fabChat.setOnClickListener {
            val packageManager: PackageManager = this.packageManager
            val i = Intent(Intent.ACTION_VIEW)
            val peringatan = "Silahkan Install Whatsapp Dahulu"
            try { val url = "https://api.whatsapp.com/send?phone=" + "+6281280588002" + "&text=" +
                             URLEncoder.encode("*Izin bertanya admin* \n\n ", "UTF-8")
                i.setPackage("com.whatsapp")
                i.data = Uri.parse(url)
                if (i.resolveActivity(packageManager) != null) {
                    this.startActivity(i)
                } else{Toast.makeText(this, peringatan, Toast.LENGTH_SHORT).show()}
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        getUserData()


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.logout -> {
                if (status == ""){
                    GlobeFunction(this).limitFeatures(header!!)
                }else{
                    Firebase.auth.signOut()
                    GlobeFunction(this).exitMain(this)
                }
                return true
            }
            R.id.profile -> {
                if (status == ""){
                    GlobeFunction(this).limitFeatures(header!!)
                }else{
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getUserData(){
        firebaseFirestore?.collection("users")?.document(FirebaseAuth.getInstance().uid.toString())
            ?.get()?.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.getString("status") != null) {
                    fullname = task.result.getString("fullname")
                    status = task.result.getString("status")!!
                    toolbar?.title = "Hallo, $fullname"
                    header?.text = "Terverifikasi $status"
                    Toast.makeText(this, "Selamat datang kembali $fullname", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(this, "Terdeteksi, masuk sebagai pengunjung", Toast.LENGTH_SHORT).show()
                    toolbar?.title = "Hallo, Pengunjung"
                    header?.text = content
                    header?.setOnClickListener {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                }
            }?.addOnFailureListener {
                Toast.makeText(this, "Jaringan Error, silahkan coba lagi!", Toast.LENGTH_SHORT).show()
            }
    }

}