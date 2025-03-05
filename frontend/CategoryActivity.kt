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
import com.example.hellofigma.ui.theme.HelloFigmaTheme
import com.example.hellofigma.frame3.Frame3

class CategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelloFigmaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    innerPadding ->
                    FullScreenScreen(this)
                    Frame3(modifier = Modifier.
                        fillMaxSize().padding(innerPadding),
                        //close this page
                        onGoBackTapped = { this.finish() },
                        searchTextContent = "Search",
                        onCellPhoneIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Cell Phone")
                            startActivity(intent)
                        },
                        onOthersOthersIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Others")
                            startActivity(intent)
                        },
                        onOtherClothesIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Other Clothes")
                            startActivity(intent)
                        },
                        onShoesIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Shoes")
                            startActivity(intent)
                        },
                        onBottomsIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Bottoms")
                            startActivity(intent)
                        },
                        onTopsIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Tops")
                            startActivity(intent)
                        },
                        onOtherFurnituresIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Other Furnitures")
                            startActivity(intent)
                        },
                        onStorageIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Storage")
                            startActivity(intent)
                        },
                        onDeskIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Desk")
                            startActivity(intent)
                        },
                        onBedIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Bed")
                            startActivity(intent)
                        },
                        onOtherAppliancesIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Other Appliances")
                            startActivity(intent)
                        },
                        onTVIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "TV")
                            startActivity(intent)
                        },
                        onLaundryIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Laundry")
                            startActivity(intent)
                        },
                        onFridgeIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Fridge")
                            startActivity(intent)
                        },
                        onPeripheralIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Peripheral")
                            startActivity(intent)
                        },
                        onTabletIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "Tablet")
                            startActivity(intent)
                        },
                        onPCIconTapped = {
                            val intent = Intent(this@CategoryActivity, SpecificCategorieActivity::class.java)
                            intent.putExtra("CATEGORY_NAME", "PC")
                            startActivity(intent)
                        },
                        onSearchBoxTapped = {
                            val intent = Intent(this@CategoryActivity, SearchActivity::class.java)
                            startActivity(intent)
                        },
                    )
                }
            }
        }
    }
}
