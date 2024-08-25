import android.content.Context

class Toast(private val message: String, private val context: Context) {
    init {
        val toast = android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT)
        toast.show()
    }
}