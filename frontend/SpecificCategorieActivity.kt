package com.example.hellofigma

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hellofigma.frame3.Frame3
import com.example.hellofigma.frame5.Frame5
import com.example.hellofigma.ui.theme.HelloFigmaTheme

class SpecificCategorieActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val categoryName = intent.getStringExtra("CATEGORY_NAME")
        enableEdgeToEdge()
        setContent {
            Frame5(modifier = Modifier.fillMaxSize(),
                categoryNameTextContent = categoryName ?: "Default Category",
                onGoBackFrameTapped = { this.finish() },
                onItem1Tapped = {
                    val intent = Intent(this@SpecificCategorieActivity, ItemActivity::class.java)
                    startActivity(intent)
                },
                onItem2Tapped = {
                    val intent = Intent(this@SpecificCategorieActivity, ItemActivity::class.java)
                    startActivity(intent)
                },
                onItem3Tapped = {
                    val intent = Intent(this@SpecificCategorieActivity, ItemActivity::class.java)
                    startActivity(intent)
                },
                onItem4Tapped = {
                    val intent = Intent(this@SpecificCategorieActivity, ItemActivity::class.java)
                    startActivity(intent)
                }

            )
        }
    }
}
