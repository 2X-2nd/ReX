package com.example.hellofigma

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hellofigma.ui.theme.HelloFigmaTheme
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import com.example.hellofigma.data.model.RegisterRequest
import com.example.hellofigma.viewmodel.ProductViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initGoogleSignInClient()
        googleLogin()

        setContent {
            HelloFigmaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
//                    Greeting("Android")
//                    Frame1()
//                    TopSearch()
//                    Group1()
                    FullScreenScreen(this)
                    MainScreen()
                }
            }
        }
    }


    // sha-1获取,右边的工具栏Gradle里运行: gradle signingReport
    // 获取后去这里申请: https://console.cloud.google.com/auth/
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private fun initGoogleSignInClient() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN) //请求邮箱
            .requestEmail()
            //.requestIdToken("785251674126-sb77si0i959knqghqpcftobbj0i7pgea.apps.googleusercontent.com")
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun googleLogin() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        // If it does not exist, request login
        if (account == null) {
            // val signInIntent = mGoogleSignInClient!!.signInIntent
            // mActivityLauncher.launch(signInIntent)
        } else {
            userLogin(account)
        }
    }

    private val mActivityLauncher = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            // Callback successful
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            registerAccount(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
        }
    }

    private fun registerAccount(account: GoogleSignInAccount) {
        viewModel.userRegister(
            RegisterRequest(
                google_id = account.id!!,
                email = account.email!!,
                username = account.displayName!!,
                preferences = listOf("", ""),
                latitude = 1.0,
                longitude = 1.0
            )
        )
    }

    private fun userLogin(account: GoogleSignInAccount) {
        // println("--getAccount--->${account.id} -- ${account.idToken} -- ${account.email} -- ${account.displayName}")
        viewModel.userLogin(account.id!!)
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    HelloFigmaTheme {
        Greeting("Android")
    }
}

fun ComponentActivity.enableFullScreen() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.insetsController?.apply {
        hide(WindowInsets.Type.systemBars())
        systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

@Composable
fun FullScreenScreen(activity: ComponentActivity) {
    LaunchedEffect(Unit) {
        activity.enableFullScreen()
    }
}
