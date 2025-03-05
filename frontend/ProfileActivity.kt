package com.example.hellofigma

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.hellofigma.data.repository.LoginState
import com.example.hellofigma.ui.theme.HelloFigmaTheme
import com.example.hellofigma.viewmodel.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HelloFigmaTheme {
                //FullScreenScreen(this)
                ProfileScreen()
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val loginState = viewModel.loginState.collectAsState().value

    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current
    val activity = context as Activity

    ProfileContent(viewModel = viewModel, loginState = loginState, activity = activity, logout = {
        coroutineScope.launch {
            viewModel.loginOut()
        }

        GoogleSignIn.getClient(
            activity,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        ).signOut()
    })
}

@Composable
private fun ProfileContent(
    viewModel: ProfileViewModel,
    loginState: LoginState,
    activity: Activity,
    logout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 0.dp)
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFEEEEEE))
    ) {
        if (loginState.isLoggedIn) {
            UserProfileHeader(loginState, logout)
            OtherInformation()

            Spacer(modifier = Modifier.weight(1f))

            LogoutButton2(logout = logout)
        } else {
            UserProfileHeaderByLogin(viewModel, activity)
            OtherInformation()
        }
    }
}

@Composable
private fun UserProfileHeader(
    loginState: LoginState,
    logout: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        modifier = Modifier.padding(8.dp).padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.frame_1_technical_support),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = loginState.userName ?: "",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = loginState.userEmail ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            //EditProfile(navController, viewModel, coroutineScope)
            Spacer(modifier = Modifier.width(3.dp))
            LogoutButton(logout)
        }
    }
}

@Composable
private fun LogoutButton(
    logout: () -> Unit
) {
    IconButton(
        modifier = Modifier.background(Color(200, 230, 250, 255), RoundedCornerShape(50)),
        onClick = logout
    ) {
        Icon(Icons.Default.ExitToApp, null)
    }
}

@Composable
private fun LogoutButton2(
    logout: () -> Unit
) {
    Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 32.dp).padding(bottom = 58.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(62, 150, 230, 255),
            contentColor = Color(10, 60, 100, 255),
            disabledContainerColor = Color.Unspecified,
            disabledContentColor = Color.Unspecified,
        ),
        onClick = logout
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                modifier = Modifier.size(28.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout", fontSize = 20.sp, color = Color.White)
        }
    }
}

@Composable
private fun OtherInformation() {
    val items = listOf(
        "Contact Us" to Icons.Default.Email,
        "Privacy Policy" to Icons.Default.List,
        "Settings" to Icons.Default.Settings
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .padding(top = 32.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = "Other",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp).padding(bottom = 0.dp)
        )

        items.forEach { (title, icon) ->
            OtherCard(title = title, icon = icon)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun OtherCard(title: String, icon: ImageVector) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { /* 处理点击事件 */ },
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun UserProfileHeaderByLogin(
    viewModel: ProfileViewModel,
    activity: Activity
) {
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            viewModel.handleGoogleSignInResult(task)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        modifier = Modifier.padding(8.dp).padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GoogleSignInButton(
                onClick = {
                    val signInIntent = GoogleSignIn.getClient(
                        activity,
                        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build()
                    ).signInIntent
                    googleSignInLauncher.launch(signInIntent)
                }
            )
        }
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_google_icon),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("Sign in with Google", style = MaterialTheme.typography.bodyLarge)
        }
    }
}