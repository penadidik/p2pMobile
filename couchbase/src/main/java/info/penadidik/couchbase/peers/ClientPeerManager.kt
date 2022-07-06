package info.penadidik.couchbase.peers

import com.couchbase.lite.*
import info.penadidik.couchbase.manager.UtilsConstants
import java.net.URI

// This is active peer
object ClientPeerManager {

    fun initReplicatorConfiguration(
        database: Database,
        hostAddress: String,
        username: String,
        password: String
    ): Replicator =
        // initialize the replicator configuration
        Replicator(
            ReplicatorConfigurationFactory.create(
                database = database,
                target = URLEndpoint(URI("wss://$hostAddress/${UtilsConstants.database_name}")),

                // Set replicator type
                type = ReplicatorType.PUSH_AND_PULL,

                // Configure Sync Mode
                continuous = true, // default value

                // Configure Server Authentication --
                // only accept self-signed certs
                acceptOnlySelfSignedServerCertificate = true,

                // Configure the credentials the
                // client will provide if prompted
                authenticator = BasicAuthenticator(
                    username,
                    password.toCharArray()
                ),

                /* Optionally set custom conflict resolver call back */
                conflictResolver = null
            )
        )

}