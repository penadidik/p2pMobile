package info.penadidik.couchbase.peers

import com.couchbase.lite.*
import info.penadidik.couchbase.manager.UtilsConstants

// This is passive peer
object ServerPeerManager {

    fun initListenerConfiguration(
        database: Database,
        netWorkInterface: String,
        authenticatorListener: ListenerAuthenticator
    ): URLEndpointListener {
        return URLEndpointListener(
            URLEndpointListenerConfigurationFactory.create(
                database = database,
                port = UtilsConstants.hostPort,
                networkInterface = netWorkInterface,

                enableDeltaSync = false,

                // Configure server security
                disableTls = false,

                // Use an Anonymous Self-Signed Cert
                identity = null,

                // Configure Client Security using an Authenticator
                // For example, Basic Authentication
                authenticator = authenticatorListener
            )
        )
    }


}