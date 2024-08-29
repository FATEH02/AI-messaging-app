package com.example.livechat.Screens

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.R
import com.example.livechat.ui.theme.CommonImage
import com.example.livechat.ui.theme.CommonProgressBar
import com.example.livechat.ui.theme.CommonRow
import com.example.livechat.ui.theme.TitleText
import com.example.livechat.ui.theme.navigateTo
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ChatListScree(navController: NavController, vm: LCViewModel, uriState: MutableStateFlow<String>, imagePicker: ActivityResultLauncher<PickVisualMediaRequest>) {
    val inProgress = vm.inProcessChats
    if (inProgress.value) {
        CommonProgressBar()
    } else {

        val chats = vm.chats.value
        val userData = vm.userData.value
        val showDialog = remember {
            mutableStateOf(false)
        }
        val onFabClick: () -> Unit = { showDialog.value = true }
        val onDismiss: () -> Unit = { showDialog.value = false }
        val onAddChat: (String) -> Unit = {
            vm.onAddChat(it)
            showDialog.value = false
        }
        Scaffold(
            floatingActionButton = {
                FAB(
                    showDialog = showDialog.value,
                    onFabClick = onFabClick,
                    onDismiss = onDismiss,
                    onAddChat = onAddChat
                )
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)

                ) {
                    TitleText(txt = "Chats")
                    if (vm.chats.value.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Chats Available")
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(75.dp)
                                .clickable {  navigateTo(navController,DestinationScreen.Gemini.route) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val painter = rememberImagePainter(R.drawable.status)

                            Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = Modifier.wrapContentSize(), // Apply the passed modifier
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = "Assistant",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                        }
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(chats){chat ->
                                val chatUser = if (chat.user1.userId == userData?.userId) {
                                    chat.user2
                                } else {
                                    chat.user1
                                }

                                CommonRow(imageUrl = chatUser.imageUrl?:"", name = chatUser.name?:"Unknown") {
                                    chat.chatId?.let {

                                        navigateTo(
                                            navController,
                                            DestinationScreen.SingleChat.createRoute(id = it)
                                        )

                                    }

                                }
                            }
                        }
                    }
                    BottomNavigationManu(
                        selectedItem = BottomNavigationItem.CHATLIST,
                        navController = navController
                    )
                }
            })
    }


}

@Composable
fun FAB(
    showDialog: Boolean,
    onFabClick: () -> Unit,
    onDismiss: () -> Unit,
    onAddChat: (String) -> Unit
) {
    val addChatNumber = remember { mutableStateOf("") }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                onDismiss.invoke()
                addChatNumber.value = ""
            },
            confirmButton = {
                Button(onClick = {
                    onAddChat(addChatNumber.value)
                    onDismiss.invoke() // Add this to close dialog after adding chat
                }) {
                    Text(text = "Add Chat")
                }
            },
            title = { Text(text = "Add Chat") },
            text = {
                OutlinedTextField(
                    value = addChatNumber.value,
                    onValueChange = { addChatNumber.value = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        )
    }

    FloatingActionButton(
        onClick = { onFabClick() },
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(imageVector = Icons.Rounded.Add, contentDescription = null, tint = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
private fun fabPreview() {
    FAB(
        showDialog = false,
        onFabClick = { /*TODO*/ },
        onDismiss = { /*TODO*/ },
        onAddChat = { /*TODO*/ }
    )
}



