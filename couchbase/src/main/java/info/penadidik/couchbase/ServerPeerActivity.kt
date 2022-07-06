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
            Log.d(TAG, "addDocumentChangeListener-send_data : ${it.database.getDocument("order").toString()}")
            Toast.makeText(this, "Database change send_data, ${it.database}", Toast.LENGTH_LONG)
                .show()
        }
        database.addDocumentChangeListener(
            "product_list"
        ) {
            Log.d(TAG, "addDocumentChangeListener-product_list : ${it.database}")
            Toast.makeText(this, "Database change product_list, ${it.database}", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun ibListenerGetNetworkInterfaces() {
        Log.i(TAG, "URLS are ${serverListener?.urls}")
        val activeConnectionCount = serverListener?.status?.activeConnectionCount
        Log.d(TAG, "activeConnectionCount count $activeConnectionCount")
    }

    fun ibListenerConfigTlsDisable() {
        URLEndpointListenerConfigurationFactory.create(database, disableTls = false)
    }

    // !!! USERS SHOULD BE CAUTIONED THAT THIS IS INSECURE
    // Android has much better ways of importing keys
    fun ibListenerConfigTlsIdFull() {
        // Use CA Cert
        // Import a key pair into secure storage
        // Create a TLSIdentity from the imported key-pair

        this.javaClass.getResourceAsStream("serverkeypair.p12")?.use {
            KeyStoreUtils.importEntry(
                "teststore.p12",  // KeyStore type, eg: "PKCS12"
                it,  // An InputStream from the keystore
                "let me in".toCharArray(),  // The keystore password
                "topSekritKey",  // The alias to be used (in external keystore)
                null,  // The key password or null if the key has none
                "test-alias" // The alias for the imported key
            )
        }

        // Set the TLS Identity
        URLEndpointListenerConfigurationFactory.create(
            database, identity = TLSIdentity.getIdentity("test-alias")
        )

    }

    fun ibListenerConfigClientAuthRoot() {
        // Configure the client authenticator
        // to validate using ROOT CA
        // thisClientID.certs is a list containing a client cert to accept
        // and any other certs needed to complete a chain between the client cert
        // and a CA
        val validId = TLSIdentity.getIdentity("Our Corporate Id")
            ?: throw IllegalStateException("Cannot find corporate id")
        // accept only clients signed by the corp cert
        serverListener = URLEndpointListener(
            URLEndpointListenerConfigurationFactory.create(
                // get the identity
                database = database,
                identity = validId,
                authenticator = ListenerCertificateAuthenticator(validId.certs)
            )
        )

    }

    fun ibListenerConfigTlsDisable2() {
        URLEndpointListenerConfigurationFactory.create(database = database, disableTls = true)
    }

    private fun ibListenerStatusCheck() {
        val connectionCount = serverListener?.status?.connectionCount
        Log.d(TAG, "Connection count $connectionCount")
    }

    private fun ibListenerStop() {
        serverListener?.stop()
    }

    override fun onBackPressed() {
        ibListenerStop()
        super.onBackPressed()
    }
}