package com.example.hellofigma

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.hellofigma.data.repository.LoginState
import com.example.hellofigma.data.repository.Result
import com.example.hellofigma.message.ChatActivity
import com.example.hellofigma.viewmodel.ItemViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ItemActivity : ComponentActivity() {
    private val viewModel: ItemViewModel by viewModels()
    private var currentLoginState: LoginState = LoginState(false,"","", "")

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)

        val content = this
        lifecycleScope.launch {
            viewModel.deleteState.collect { response ->
                if (response != null) {
                    if (response.message === "") {
                        Toast.makeText(content, "Delete fail!", Toast.LENGTH_SHORT).show()
                    } else {
                        val resultIntent = Intent().apply {
                            putExtra("REFRESH_NEEDED", true)
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                currentLoginState = state
            }
        }

        val id = intent.getStringExtra("id") ?: ""
        val title = intent.getStringExtra("title") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val price = intent.getDoubleExtra("price", 0.0)
        val seller_id = intent.getStringExtra("seller_id") ?: ""
        // val latitude = intent.getDoubleExtra("latitude", 0.0)
        // val longitude = intent.getDoubleExtra("longitude", 0.0)
        //val image = intent.getStringExtra("image") ?: ""

        val productImage: ImageView = findViewById(R.id.productImage)
        //productImage.setImageBitmap(decodeBase64(image))
        val productPrice: TextView = findViewById(R.id.productPrice)
        productPrice.text = "%.1f".format(price)
        val productName: TextView = findViewById(R.id.productName)
        productName.text = title
        val productDescription: TextView = findViewById(R.id.productDescription)
        productDescription.text = description

        val exitButton: ImageView = findViewById(R.id.exitButton)
        val productSavings: TextView = findViewById(R.id.productSavings)
        val remove: Button = findViewById(R.id.remove)
        val iWantItButton: Button = findViewById(R.id.iWantItButton)

        lifecycleScope.launch {
            viewModel.getProduct(id)
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { result ->
                    when (result) {
                        is Result.Success -> {
                            productImage.setImageBitmap(decodeBase64(result.data.images[0]))
                        }
                        else -> {
                        }
                    }
                }
            }
        }

        // Exit Button Listener
        exitButton.setOnClickListener {
            finish() // Close the activity
        }

        // Savings Link Listener
        productSavings.setOnClickListener {
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.amazon.ca/HUAWEI-P40-128GB-Vision-Supercharge-Renewed/dp/B08JLMYMG3/ref=sr_1_1?crid=UQP967L2P6I9")
            )
            startActivity(browserIntent)
        }

        remove.setOnClickListener {
            if (currentLoginState.isLoggedIn && currentLoginState.googleId == seller_id) {
                viewModel.deleteProduct(id)
            }
            else {
                Toast.makeText(this, "You do not have permission to delete", Toast.LENGTH_SHORT).show()
                remove.contentDescription = "You do not have permission to delete"
            }
        }

        // I Want It Button Listener
        iWantItButton.setOnClickListener {
            if (currentLoginState.isLoggedIn) {
                lifecycleScope.launch {
                    val user = viewModel.getUser(seller_id)
                    if (user != null) {
                        startActivity(
                            Intent(content, ChatActivity::class.java)
                                .putExtra("userId", currentLoginState.googleId)
                                .putExtra("otherUserId", user.google_id)
                                .putExtra("otherUserName", user.username)
                        )
                    }
                }
            } else {
                // Handle the click event for the "I Want It" button
                Toast.makeText(this, "You haven't logged in yet", Toast.LENGTH_SHORT).show()
                iWantItButton.contentDescription = "You haven't logged in yet"
            }
        }
    }

    private fun decodeBase64(base64: String): Bitmap? {
        return try {
            val pureBase64 = base64.substringAfter(",", base64)
            val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
