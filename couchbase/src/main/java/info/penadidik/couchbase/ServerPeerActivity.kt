package info.penadidik.couchbase

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.couchbase.lite.*
import info.penadidik.couchbase.databinding.ActivityServerPeerBinding
import info.penadidik.couchbase.manager.DatabaseManager
import info.penadidik.couchbase.manager.UtilsConstants
import info.penadidik.couchbase.manager.WIFIManager
import info.penadidik.couchbase.peers.ServerPeerManager
import info.penadidik.utils.Preferences
import info.penadidik.utils.base.BaseActivity
import info.penadidik.utils.extension.showToast
import info.penadidik.utils.getRandomString
import info.penadidik.utils.hideSoftKeyboard
import info.penadidik.utils.showLog
import net.glxn.qrgen.android.QRCode

class ServerPeerActivity: BaseActivity() {
    private val TAG = "ServerPairingActivity"

    private val database by lazy {
        DatabaseManager.initDatabase(this)
    }
    private lateinit var binding: ActivityServerPeerBinding

    private var serverListener: URLEndpointListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerPeerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        hideSoftKeyboard()

        binding.btnGenerate.setOnClickListener {
            val user = getRandomString(8)
            val password = getRandomString(16)
            Preferences(this).saveStringData(UtilsConstants.prefUsername, user)
            Preferences(this).saveStringData(UtilsConstants.prefPassword, password)

            showToast("generate QR")

            Handler().postDelayed({
                initQrCode()
            }, 1000)
        }

        if (Preferences(this).getStringData(UtilsConstants.prefUsername).isNotEmpty() &&
            Preferences(this).getStringData(UtilsConstants.prefPassword).isNotEmpty()) {

            initQrCode()
        }
    }

    private fun initQrCode() {
        binding.imgQr.visibility = View.VISIBLE
        binding.btnGenerate.text = getString(R.string.qr_edit)

        val ipAddress = WIFIManager.getIpAddress(this)
        val user = Preferences(this).getStringData(UtilsConstants.prefUsername)
        val password = Preferences(this).getStringData(UtilsConstants.prefPassword)

        val qrBuilder =
            "app://couchbase/waitress?ip=$ipAddress:${UtilsConstants.hostPort}&user=$user&password=$password"
        binding.imgQr.setImageBitmap(QRCode.from(qrBuilder).withSize(1000, 1000).bitmap())

        initCouchbase(ipAddress)
    }

    private fun initCouchbase(ipAddress: String) {
        CouchbaseLite.init(this)

        Log.d(TAG, "name ${database.name}")

        try {
            serverListener = ServerPeerManager.initListenerConfiguration(
                database,
                ipAddress,
                authenticatorListener
            )
            serverListener?.start()
        } catch (e: Exception) {
            showToast(e.message.toString())
        }
        ibListenerGetNetworkInterfaces()
        ibListenerStatusCheck()
        ibListenerDatabase()
    }

    private val authenticatorListener = ListenerPasswordAuthenticator { username, password ->
        (username == Preferences(this).getStringData(UtilsConstants.prefUsername)) &&
                (String(password) == Preferences(this).getStringData(UtilsConstants.prefPassword))
    }

    private fun ibListenerDatabase() {
        database.addDocumentChangeListener(
            "order"
        ) {
            showLog(TAG, "addDocumentChangeListener-send_data : ${it.database.getDocument("order").toString()}")
            Toast.makeText(this, "Database change send_data, ${it.database}", Toast.LENGTH_LONG)
                .show()
        }
        database.addDocumentChangeListener(
            "product_list"
        ) {
            showLog(TAG, "addDocumentChangeListener-product_list : ${it.database}")
            Toast.makeText(this, "Database change product_list, ${it.database}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun ibListenerGetNetworkInterfaces() {
        showLog(TAG, "URLS are ${serverListener?.urls}")
        val activeConnectionCount = serverListener?.status?.activeConnectionCount
        showLog(TAG, "activeConnectionCount count $activeConnectionCount")
    }

    private fun ibListenerStatusCheck() {
        val connectionCount = serverListener?.status?.connectionCount
        showLog(TAG, "Connection count $connectionCount")
    }

    private fun ibListenerStop() {
        serverListener?.stop()
    }

    override fun onBackPressed() {
        ibListenerStop()
        super.onBackPressed()
    }
}