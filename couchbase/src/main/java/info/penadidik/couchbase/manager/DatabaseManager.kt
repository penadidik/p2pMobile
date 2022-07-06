package info.penadidik.couchbase.manager

import android.content.Context
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfigurationFactory
import com.couchbase.lite.create

object DatabaseManager {

    fun initDatabase(context : Context) : Database {
        return Database(
            UtilsConstants.database_name,
            DatabaseConfigurationFactory.create(
                context.filesDir.absolutePath
            )
        )
    }

    fun initClientDatabase(context : Context) : Database {
        return Database(
            UtilsConstants.database_client_name,
            DatabaseConfigurationFactory.create(
                context.filesDir.absolutePath
            )
        )
    }
}