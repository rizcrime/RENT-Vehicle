package transmettal.dev.rental

import android.R.attr.label
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


open class GlobeFunction(var context: Context) {

    open fun reloadPage(activity: Activity){
        activity.finish()
        activity.startActivity(activity.intent)
    }

    open fun autentikasi(): FirebaseAuth {
        return Firebase.auth
    }

    open fun penyimpanan(): FirebaseStorage{
        return FirebaseStorage.getInstance()
    }

    open fun basisData(): FirebaseFirestore{
        return FirebaseFirestore.getInstance()
    }

    open fun regData(id: String, fullname: String, numTelp: String, status: String = "penyewa",poin: String = "0",
                     special_kode: String = "0", keyAdmin: String = "0", referenceBy: String = "0"):Map<String, String>{
        return hashMapOf("id" to id, "fullname" to fullname, "num_telp" to numTelp, "status" to status, "poin" to poin,
            "special_kode" to special_kode, "key_admin" to keyAdmin, "reference_by" to referenceBy)
    }

    open fun rupiah(nilai:Int):String{
        val decim = DecimalFormat("#,###.##")
        return "Rp${decim.format(nilai)}"
    }

    open fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    open fun copyTextToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(label.toString(), text)
        clipboard!!.setPrimaryClip(clip)
    }

    open fun vibrateError(): TranslateAnimation {
        val vibrate = TranslateAnimation(0F, 10F, 0F, 0F)
        vibrate.duration = 600
        vibrate.interpolator = CycleInterpolator(8F)
        return vibrate
    }

    open fun vibratePhone() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(200)
        }
    }

    open fun limitFeatures(header: TextView){
        vibratePhone()
        header.startAnimation(vibrateError())
        Toast.makeText(context, "Fitur dibatasi", Toast.LENGTH_SHORT).show()
    }

    open fun chooseImage(activity:Activity, pickImageRequest:Int = 71) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), pickImageRequest)
    }

    open fun exitMain(activity: Activity){
        activity.startActivity(Intent(activity, HomeActivity::class.java))
        activity.finish()
    }

    open fun tanggal(timestampEnd: Timestamp, timestampNow: Timestamp = Timestamp.now()):String{
        val millisecondsEnd = timestampEnd.seconds * 1000 + timestampEnd.nanoseconds / 1000000
        val millisecondsNow = timestampNow.seconds * 1000 + timestampNow.nanoseconds / 1000000
        val sdf = SimpleDateFormat("dd MMMM yyyy")
        val netDateEnd = Date(millisecondsEnd)
        val netDateNow = Date(millisecondsNow)
        if (netDateEnd == netDateNow || netDateEnd < netDateNow){
            return context.getString(R.string.tersedia_desc)
        }else{
            return "${context.getString(R.string.tersewa_desc)}\n${sdf.format(netDateEnd)}"
        }
    }

}

