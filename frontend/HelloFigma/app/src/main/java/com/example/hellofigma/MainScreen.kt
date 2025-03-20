package com.example.hellofigma

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hellofigma.data.repository.LoginState
import com.example.hellofigma.data.repository.Result
import com.example.hellofigma.frame1.Frame1
import com.example.hellofigma.message.MessageActivity
import com.example.hellofigma.viewmodel.ProductViewModel
import com.google.relay.compose.EmptyPainter
import com.example.weather_dashboard.data.models.Product


@Composable
fun MainScreen(
    viewModel: ProductViewModel = hiltViewModel()
) {
    var currentLoginState by remember { mutableStateOf(LoginState(false,"","", "")) }
    LaunchedEffect(Unit) {
        viewModel.loginState.collect { state ->
            currentLoginState = state
        }
    }

    var reloadRequested by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.queryProduct("")
    }

    val activityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        if (activityResult.resultCode == Activity.RESULT_OK) {
            val needRefresh = activityResult.data?.getBooleanExtra("REFRESH_NEEDED", false) ?: false
            if (needRefresh) {
                viewModel.queryProduct("")
            }
        }
    }

    when (val state = viewModel.uiState.value) {
        is Result.Loading -> {
            MainCard(currentLoginState, emptyList(), activityLauncher = activityLauncher)
            LoadingScreen()
        }
        is Result.Success -> MainCard(currentLoginState, state.data.results, activityLauncher = activityLauncher)
        is Result.Error -> {
            MainCard(currentLoginState, emptyList(), activityLauncher = activityLauncher)
            ErrorScreen(state.exception) {
                viewModel.queryProduct("")
                reloadRequested = true
            }
            if (reloadRequested) {
                reloadRequested = false
            }
        }
        else -> {}
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize().testTag("loadingIndicator"),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(
    exception: Throwable,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Error: ${exception.localizedMessage}",
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Reload")
            }
        }
    }
}

@Composable
fun MainCard(
    currentLoginState: LoginState,
    products: List<Product>,
    activityLauncher: ActivityResultLauncher<Intent>
) {
    val context = LocalContext.current

    var currentPage by remember { mutableIntStateOf(0) }
    val itemsPerPage = 4
    val totalPages = (products.size + itemsPerPage - 1) / itemsPerPage
    val currentProducts = products.subList(
        currentPage * itemsPerPage,
        minOf((currentPage + 1) * itemsPerPage, products.size)
    )

    Frame1(modifier = Modifier.fillMaxSize(),
        searchTextContent = "Search",
        image1Content = if (currentProducts.isNotEmpty()) loadNetworkPainter(currentProducts[0].image) else EmptyPainter(),
        image2Content = if (currentProducts.size >= 2) loadNetworkPainter(currentProducts[1].image) else EmptyPainter(),
        image3Content = if (currentProducts.size >= 3) loadNetworkPainter(currentProducts[2].image) else EmptyPainter(), // painterResource(id = R.drawable.frame_1_image_3),
        image4Content = if (currentProducts.size >= 4) loadNetworkPainter(currentProducts[3].image) else EmptyPainter(),
        item1TitleTextContent = if (currentProducts.isNotEmpty()) currentProducts[0].title else "",
        item1PriceTextContent = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                append(if (currentProducts.isNotEmpty()) "%.1f".format(currentProducts[0].price) else "")
            }
        },
        item2TitleTextContent = if (currentProducts.size >= 2) currentProducts[1].title else "",
        item2PriceTextContent = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                append(if (currentProducts.size >= 2) "%.1f".format(currentProducts[1].price) else "")
            }
        },
        item3TitleTextContent = if (currentProducts.size >= 3) currentProducts[2].title else "",
        item3PriceTextContent = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                append(if (currentProducts.size >= 3) "%.1f".format(currentProducts[2].price) else "")
            }
        },

        item4TitleTextContent = if (currentProducts.size >= 4) currentProducts[3].title else "",
        item4PriceTextContent = buildAnnotatedString {
            withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                append(if (currentProducts.size >= 4) "%.1f".format(currentProducts[3].price) else "")
            }
        },
        //open CategoryActivity
        onClickCategoriesIcon = {
            val intent = Intent(context, CategoryActivity::class.java)
            context.startActivity(intent)
        },
        onClickSearchBox = {
            val intent = Intent(context, SearchActivity::class.java)
            context.startActivity(intent)
        },
        onClickPostIcon = {
            val intent = Intent(context, PostActivity::class.java)
            //context.startActivity(intent)
            activityLauncher.launch(intent)
        },
        onItem1Tapped = {
            if (currentProducts.isNotEmpty()) {
                val intent = Intent(context, ItemActivity::class.java)
                intent.putExtra("id", currentProducts[0].id)
                intent.putExtra("title", currentProducts[0].title)
                intent.putExtra("description", currentProducts[0].description)
                intent.putExtra("price", currentProducts[0].price)
                intent.putExtra("seller_id", currentProducts[0].seller_id)
                intent.putExtra("latitude", currentProducts[0].latitude)
                intent.putExtra("longitude", currentProducts[0].longitude)
                // intent.putExtra("image", currentProducts[0].image)
                activityLauncher.launch(intent)
            }
        },
        onItem2Tapped = {
            if (currentProducts.size >= 2) {
                val intent = Intent(context, ItemActivity::class.java)
                intent.putExtra("id", currentProducts[1].id)
                intent.putExtra("title", currentProducts[1].title)
                intent.putExtra("description", currentProducts[1].description)
                intent.putExtra("price", currentProducts[1].price)
                intent.putExtra("seller_id", currentProducts[1].seller_id)
                intent.putExtra("latitude", currentProducts[1].latitude)
                intent.putExtra("longitude", currentProducts[1].longitude)
                // intent.putExtra("image", currentProducts[1].image)
                activityLauncher.launch(intent)
            }
        },
        onItem3Tapped = {
            if (currentProducts.size >= 3) {
                val intent = Intent(context, ItemActivity::class.java)
                intent.putExtra("id", currentProducts[2].id)
                intent.putExtra("title", currentProducts[2].title)
                intent.putExtra("description", currentProducts[2].description)
                intent.putExtra("price", currentProducts[2].price)
                intent.putExtra("seller_id", currentProducts[2].seller_id)
                intent.putExtra("latitude", currentProducts[2].latitude)
                intent.putExtra("longitude", currentProducts[2].longitude)
                // intent.putExtra("image", currentProducts[2].image)
                activityLauncher.launch(intent)
            }
        },
        onItem4Tapped = {
            if (currentProducts.size >= 4) {
                val intent = Intent(context, ItemActivity::class.java)
                intent.putExtra("id", currentProducts[3].id)
                intent.putExtra("title", currentProducts[3].title)
                intent.putExtra("description", currentProducts[3].description)
                intent.putExtra("price", currentProducts[3].price)
                intent.putExtra("seller_id", currentProducts[3].seller_id)
                intent.putExtra("latitude", currentProducts[3].latitude)
                intent.putExtra("longitude", currentProducts[3].longitude)
                // intent.putExtra("image", currentProducts[3].image)
                activityLauncher.launch(intent)
            }
        },
        onClickStorage = {
            val intent = Intent(context, StorageActivity::class.java)
            context.startActivity(intent)
        },
        onClickUserIcon = {
            val intent = Intent(context, ProfileActivity::class.java)
            context.startActivity(intent)
        },
        onClickMessagesIcon = {
            val intent = Intent(context, MessageActivity::class.java)
            intent.putExtra("userId", currentLoginState.googleId)
            context.startActivity(intent)
        },
        onNextPageTapped = {
            if (currentPage < totalPages - 1) {
                currentPage++
            }
        },
        onLastPageTapped = {
            if (currentPage > 0) {
                currentPage--
            }
        }
    )
}

@Composable
public fun loadNetworkPainter(base64: String): Painter {
    if (!base64.startsWith("data:")) {
        return painterResource(R.drawable.frame_1_rectangle_item_1)
    }

    val bitmap = remember(base64) {
        try {
            // 去除可能的 Base64 前缀
            val pureBase64 = base64.substringAfter(",", base64)
            val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null // 解码失败返回 null
        }
    }

    return bitmap?.asImageBitmap()?.let { BitmapPainter(it) }
        ?: painterResource(R.drawable.frame_1_rectangle_item_1) // 失败时显示占位图
}
