package info.penadidik.couchbase

import android.app.Activity
import android.net.UrlQuerySanitizer
import android.os.Bundle
import android.util.Log
import android.view.View
import com.couchbase.lite.*
import info.penadidik.couchbase.databinding.ActivityClientPeerBinding
import info.penadidik.couchbase.manager.DatabaseManager
import info.penadidik.couchbase.manager.UtilsConstants
import info.penadidik.couchbase.peers.ClientPeerManager
import info.penadidik.qrscanner.ActivityScannerQR
import info.penadidik.utils.Preferences
import info.penadidik.utils.base.BaseActivity
import info.penadidik.utils.extension.DialogTwoActionListener
import info.penadidik.utils.extension.showDialogMessage
import info.penadidik.utils.extension.showToast
import info.penadidik.utils.showLog
import okhttp3.internal.Util

class ClientPeerActivity: BaseActivity() {
    private val TAG = "ClientPeerActivity"

    private lateinit var binding: ActivityClientPeerBinding

    private val database by lazy {
        DatabaseManager.initDatabase(this)
    }

    private lateinit var thisReplicator: Replicator

    private var status: ReplicatorActivityLevel = ReplicatorActivityLevel.OFFLINE
    private var isDialogShow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClientPeerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.visibility = View.GONE

        binding.btnScanQr.setOnClickListener { intentScanQr() }

        binding.btnSend.setOnClickListener {
            when (status) {
                ReplicatorActivityLevel.CONNECTING ->
                    database.save(
                        MutableDocument(
                            "order",
                            "{\"data\":[{\"id\":\"1\",\"title\":\"sate goreng\"}]}"
                        )
                    )
                else ->
                    initCouchbase()
            }
        }

        initBundle()
    }

    private fun initBundle() {
        val data = intent.data
        if (data.toString().contains(UtilsConstants.waitressLink) && data != null) {
            loadUrl(data.toString())
            initCouchbase()
        }
    }

    private fun intentScanQr() {
        val intent = ActivityScannerQR.newIntent(this, UtilsConstants.waitressLink, UtilsConstants.waitressLinkResult)
        activityLauncher.launch(intent) {
            when(it.resultCode) {
                Activity.RESULT_OK -> {
                    if (it.data?.hasExtra(UtilsConstants.waitressLinkResult) == true) {
                        val url = it.data?.getStringExtra(UtilsConstants.waitressLinkResult).orEmpty()
                        showToast(url)
                        loadUrl(url)
                        initCouchbase()
                    }
                }
            }
        }
    }

    private fun loadUrl(url: String) {
        val sanitizer = UrlQuerySanitizer(url)
        val ipAddress = sanitizer.getValue("ip")
        val user = sanitizer.getValue("user")
        val password = sanitizer.getValue("password")

        Preferences(this).apply {
            saveStringData(UtilsConstants.prefIpAddress, ipAddress)
            saveStringData(UtilsConstants.prefUsername, user)
            saveStringData(UtilsConstants.prefPassword, password)
        }
    }

    private fun initCouchbase() {
        // init counchbase
        CouchbaseLite.init(this)

        startToConnect(
            Preferences(this).getStringData(UtilsConstants.prefIpAddress),
            Preferences(this).getStringData(UtilsConstants.prefUsername),
            Preferences(this).getStringData(UtilsConstants.prefPassword)
        )
        initReplicatorListener()
        checkConnectionStatus()
    }

    private fun initReplicatorListener() {
        thisReplicator.addChangeListener { change ->
            val err: CouchbaseLiteException? = change.status.error
            if (err != null) {
                Log.d(TAG, "Error code ::  ${err.code}")
                Log.d(TAG, "Error message ::  ${err.message}")
                binding.statusConnection.text = err.message
                binding.btnSend.text = getString(R.string.retry_connection)
                status = ReplicatorActivityLevel.OFFLINE
                var message = getString(R.string.error_try_again)
                var action = getString(R.string.error_action_try_again)

                when (err.code) {
                    UtilsConstants.ERROR.CLOSED_BY_SERVER, UtilsConstants.ERROR.FAILED_TO_CONNECT_SERVER -> {
                        message = getString(R.string.error_closed_by_server)
                        action = getString(R.string.error_action_try_again)
                    }
                    UtilsConstants.ERROR.UNAUTHORIZED -> {
                        message = getString(R.string.error_unauthorized)
                        action = getString(R.string.error_action_scan_qr)
                    }
                }
                if (!isDialogShow) {
                    showDialogMessage(
                        message = message,
                        actionOk = action,
                        object : DialogTwoActionListener {
                            override fun onClickCancel() {
                                finish()
                            }

                            override fun onClickOk() {
                                when (err.code) {
                                    UtilsConstants.ERROR.CLOSED_BY_SERVER, UtilsConstants.ERROR.FAILED_TO_CONNECT_SERVER ->
                                        initCouchbase()
                                    UtilsConstants.ERROR.UNAUTHORIZED ->
                                        intentScanQr()
                                }
                            }

                            override fun onDismiss() {
                                isDialogShow = false
                            }
                        })
                    isDialogShow = true
                }
            }
        }
    }

    private fun checkConnectionStatus() {
        thisReplicator.status.let {
            showLog(TAG, "Activity level :  ${it.activityLevel}")
            showLog(TAG, "Progress :  ${it.progress}")
            val stringStatus = if (it.activityLevel != ReplicatorActivityLevel.CONNECTING) it.activityLevel.name else "CONNECTED"
            binding.statusConnection.text = stringStatus
            binding.btnSend.text = getString(
                if (it.activityLevel == ReplicatorActivityLevel.CONNECTING) R.string.send_data
                else R.string.retry_connection
            )
            status = ReplicatorActivityLevel.CONNECTING

            showLog(
                TAG,
                if (it.activityLevel === ReplicatorActivityLevel.BUSY) {
                    "Replication Processing"
                } else {
                    "It has completed ${it.progress.total} changes"
                }
            )
        }

        binding.btnSend.visibility = View.VISIBLE
    }

    private fun startToConnect(hostAddress: String, username: String, password: String) {
        thisReplicator =
            ClientPeerManager.initReplicatorConfiguration(database, hostAddress, username, password)
        thisReplicator.start()
    }
}