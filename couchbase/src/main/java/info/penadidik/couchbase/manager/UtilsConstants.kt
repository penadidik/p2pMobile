package info.penadidik.couchbase.manager
object UtilsConstants {

    const val database_name = "my-db"
    const val database_client_name = "my-db-client"

    const val hostPort = 55990

    const val waitressLink = "waitress"
    const val waitressLinkResult = "waitressLinkResult"

    const val prefIpAddress = "pref_ip_address"
    const val prefUsername = "pref_username"
    const val prefPassword = "pref_password"

    // status code
    interface ERROR {
        companion object {
            const val CLOSED_BY_SERVER = 11001 /* WebSocket connection closed by peer */
            const val FAILED_TO_CONNECT_SERVER = 111 /* WebSocket connection closed by peer */
            const val UNAUTHORIZED = 10401 /* Unauthorized please check username or password */
        }
    }

}