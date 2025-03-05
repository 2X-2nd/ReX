package com.example.hellofigma

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hellofigma.data.repository.Result
import com.example.hellofigma.viewmodel.SearchViewModel
import com.example.weather_dashboard.data.models.Product
import com.google.relay.compose.RelayImage
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SearchActivity : AppCompatActivity() {
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val composeContainer: ComposeView = findViewById(R.id.compose_container)
        composeContainer.setContent {
            SearchScreen(viewModel)
        }

        val backButton: ImageView = findViewById(R.id.backButton)
        val searchButton: ImageView = findViewById(R.id.searchButton)
        val searchInput: EditText = findViewById(R.id.searchInput)

        // return the user to the previous activity
        backButton.setOnClickListener {
            finish()
        }

        // activity of the searchButton on clicked
        searchButton.setOnClickListener {
            performSearch(searchInput.text.toString())
        }

        // activity of the searchInput on pressed enter
        searchInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                searchInput.clearFocus()
                hideKeyboard(searchInput)
                performSearch(searchInput.text.toString())
                true
            } else {
                false
            }
        }

        searchInput.requestFocus()
        showKeyboard(searchInput)
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            viewModel.searchProduct(query)
        } else {
            Toast.makeText(this@SearchActivity, "Please enter a search term", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(editText: EditText) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }
}

@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    val context = LocalContext.current
    when (val state = viewModel.uiState.value) {
        is Result.Loading -> LoadingScreen()
        is Result.Success -> ProductList(
            products = state.data.results,
            onProductClick = { product ->
                val intent = Intent(context, ItemActivity::class.java)
                intent.putExtra("id", product.id)
                intent.putExtra("title", product.title)
                intent.putExtra("description", product.description)
                intent.putExtra("price", product.price)
                intent.putExtra("latitude", product.latitude)
                intent.putExtra("longitude", product.longitude)
                // intent.putExtra("image", product.image)
                context.startActivity(intent)
            }
        )
        is Result.Error -> {
            Toast.makeText(context, state.exception.toString(), Toast.LENGTH_SHORT).show()
        }
        else -> {}
    }
}

// Displays a list of tasks with options to add a new task, view categories, and view analytics
@Composable
fun ProductList(
    products: List<Product>,
    onProductClick: (Product) -> Unit,
) {
    Column(Modifier.padding(top = 8.dp)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(products) { product ->
                ProductItem(product = product, onProductClick = onProductClick)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/*
 Displays a single product item
 */
@Composable
fun ProductItem(product: Product, onProductClick: (Product) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onProductClick(product) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            /*
            RelayImage(
                image = loadNetworkPainter(product.image),
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(100.dp).height(150.dp).padding(8.dp)
            )*/
            Column {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
                /*
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )*/
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$%.1f".format(product.price),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Red
                )
            }
        }
    }
}