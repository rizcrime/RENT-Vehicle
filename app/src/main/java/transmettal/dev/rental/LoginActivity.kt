package transmettal.dev.rental

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d("hayam", Timestamp.now().toString())

        val email: EditText = findViewById(R.id.edt_email)
        val password: EditText = findViewById(R.id.edt_password)
        val btnLogin: Button = findViewById(R.id.btn_login)
        val btnRegister: TextView = findViewById(R.id.btn_register)

        btnLogin.setOnClickListener{
            signIn(email.text.toString(), password.text.toString())
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        val actionbar = supportActionBar
        actionbar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun signIn(email: String, password: String) {
        GlobeFunction(this).autentikasi().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    finish()
                    startActivity(Intent(this, HomeActivity::class.java))
                } else {
                    Toast.makeText(this, "Data tidak ada", Toast.LENGTH_SHORT).show()
                }
            }
    }

}