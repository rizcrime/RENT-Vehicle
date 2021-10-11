package transmettal.dev.rental

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private var imageView: CircleImageView? = null
    private var filePath: Uri? = null
    private var loadingLayout : LinearLayout? = null
    private var loading : ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        imageView = findViewById(R.id.profile_image)
        loadingLayout = findViewById(R.id.layout_progressbar)
        loading = findViewById(R.id.progressbar)

        val edtEmail: EditText = findViewById(R.id.edt_email)
        val edtFname: EditText = findViewById(R.id.edt_fullname)
        val edtNum: EditText = findViewById(R.id.edt_numtelp)
        val edtPassword: EditText = findViewById(R.id.edt_password)
        val btnRegister: Button = findViewById(R.id.btn_register)

        btnRegister.setOnClickListener {
            signUp(
                edtFname.text.toString(), edtNum.text.toString(),
                edtEmail.text.toString(), edtPassword.text.toString()
            )
        }

        imageView?.setOnClickListener {
            GlobeFunction(this).chooseImage(this)
        }

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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

    private fun signUp(fullname: String, numTelp: String, email: String, password: String) {
        GlobeFunction(this).autentikasi().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                val dataUser = GlobeFunction(this).regData(GlobeFunction(this).autentikasi()
                    .uid.toString(), fullname, numTelp)
                if (task.isSuccessful && filePath != null) {
                    //upload image
                    val addressImage = "foto_profile/${GlobeFunction(this).autentikasi().uid.toString()}"
                    val refImage = GlobeFunction(this).penyimpanan().reference.child(addressImage)
                    refImage.putFile(filePath!!).addOnFailureListener {
                        Toast.makeText(this, "Gagal mengupload gambar", Toast.LENGTH_SHORT).show()
                    }.addOnProgressListener {
                        val percentage = (it.bytesTransferred / it.totalByteCount) * 100
                        loadingLayout?.visibility = View.VISIBLE
                        loading?.progress = percentage.toInt()
                    }.addOnCompleteListener{
                        //upload data non image
                        GlobeFunction(this).basisData().collection("users")
                            .document(GlobeFunction(this).autentikasi().uid.toString())
                            .set(dataUser).addOnFailureListener {
                                Toast.makeText(this, "Gagal mengupload identitas", Toast.LENGTH_SHORT).show()
                            }.addOnCompleteListener {
                                loadingLayout?.visibility = View.GONE
                                Firebase.auth.signOut()
                                finish()
                                startActivity(Intent(this, LoginActivity::class.java))
                                Toast.makeText(this, "Akun berhasil dibuat", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Akun gagal dibuat", Toast.LENGTH_SHORT).show()
                }
            }
    }

}