package info.penadidik.p2pmobile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.ui.AppBarConfiguration
import info.penadidik.couchbase.ClientPeerActivity
import info.penadidik.couchbase.ServerPeerActivity
import info.penadidik.p2pmobile.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setContent()
    }

    private fun setContent() {
        binding.btnServer.setOnClickListener {
            startActivity(Intent(this, ServerPeerActivity::class.java))
        }

        binding.btnClient.setOnClickListener {
            startActivity(Intent(this, ClientPeerActivity::class.java))
        }
    }

}