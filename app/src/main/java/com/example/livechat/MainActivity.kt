package com.example.livechat

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.livechat.Screens.LoginScreen
import com.example.livechat.ui.theme.LiveChatTheme
import androidx.navigation.compose.composable
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.ahmedapps.geminichatbot.all.ChatUiEvent
import com.ahmedapps.geminichatbot.all.ChatViewModel
import com.example.livechat.Data.ChatUser
import com.example.livechat.Screens.ChatListScree
import com.example.livechat.Screens.ProfileScreen
import com.example.livechat.Screens.SignUpScreen
import com.example.livechat.Screens.SingleStatusScreen
import com.example.livechat.Screens.SinlgeChatScreen
import com.example.livechat.Screens.StatusScreen
import com.example.livechat.all.ChatScreen
import com.example.livechat.ui.theme.CommonImage
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

sealed class DestinationScreen(var route: String) {
    object SignUp : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")
    object ChatList : DestinationScreen("chatList")
    object SingleChat : DestinationScreen("singleChat/{chatId}") {
        fun createRoute(id: String) = "singleChat/$id"
    }
    object StatusList : DestinationScreen("statusList")
    object SingleStatus : DestinationScreen("singleStatus/{userId}") {
        fun createRoute(userId: String) = "singleStatus/$userId"
    }
    object Gemini : DestinationScreen("gemini")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val uriState = MutableStateFlow("")

    private val imagePicker =
        registerForActivityResult<PickVisualMediaRequest, Uri?>(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                uriState.update { uri.toString() }
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiveChatTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ChatAppNavigation(modifier = Modifier.padding(innerPadding))
//                    ChatScreen(uriState, imagePicker)

                }
            }
        }
    }

    @Composable
    fun ChatAppNavigation(modifier: Modifier = Modifier) {
        val navController = rememberNavController()
        val vm = hiltViewModel<LCViewModel>()
        NavHost(navController = navController, startDestination = DestinationScreen.SignUp.route, modifier = modifier) {
            composable(DestinationScreen.SignUp.route) {
                SignUpScreen(navController, vm)
            }
            composable(DestinationScreen.Login.route) {
                LoginScreen(navController, vm)
            }
            composable(DestinationScreen.ChatList.route) {
                ChatListScree(navController, vm,uriState,imagePicker)
            }
            composable(DestinationScreen.SingleChat.route) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")
                chatId?.let {
                    SinlgeChatScreen(navController, vm, chatId)
                }
            }
            composable(DestinationScreen.StatusList.route) {
                StatusScreen(navController, vm)
            }
            composable(DestinationScreen.Profile.route) {
                ProfileScreen(navController, vm)
            }
            composable(DestinationScreen.SingleStatus.route)
            {
                val userId = it.arguments?.getString("userId")
                userId?.let {
                    SingleStatusScreen(navController,vm,it)
                }

            }
            composable(DestinationScreen.Gemini.route)
            {
                ChatScreen(uriState,imagePicker)
            }


        }
    }
}




