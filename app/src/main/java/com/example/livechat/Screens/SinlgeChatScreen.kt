package com.example.livechat.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.livechat.Data.Message
import com.example.livechat.LCViewModel
import com.example.livechat.R
import com.example.livechat.ui.theme.CommonDivider
import com.example.livechat.ui.theme.CommonImage


@Composable
fun SinlgeChatScreen(navController: NavController, vm: LCViewModel, chatId: String) {

    var reply by rememberSaveable {
        mutableStateOf("")
    }
    val onSendReplay = {
        vm.onSendReply(chatId, reply)
        reply = " "
    }
    var chatMessages = vm.chatMessages
    val myUser = vm.userData.value
    var currentChat = vm.chats.value.first { it.chatId == chatId }
    LaunchedEffect(key1 = Unit) {
        vm.populateMessage(chatId)
    }
    BackHandler {
        vm.DepopulateMessage()
    }
    val chatUser =
        if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1
    Column() {
        ChatHeader(name = chatUser.name ?: "", imageUrl = chatUser.imageUrl ?: "") {
            navController.popBackStack()
            vm.DepopulateMessage()
        }
        MessageBox(
            modifier = Modifier.weight(1f),
            chatMessage = chatMessages.value,
            currentUserId = myUser?.userId ?: ""
        )
        ReplyBox(reaply = reply, onReplayChange = { reply = it }, onSendReplay = onSendReplay)
    }
}

@Composable
fun MessageBox(modifier: Modifier, chatMessage: List<Message>, currentUserId: String) {
    LazyColumn(modifier = modifier) {

        items(chatMessage)
        { msg ->
            val alignment = if (msg.sendBy == currentUserId) Alignment.End else Alignment.Start
            val color = if (msg.sendBy == currentUserId) Color(0xFF68C400) else Color(0xFFC0C0C0)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = alignment
            ) {
                Text(
                    text = msg.message ?: "",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(12.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }


        }


    }


}


@Composable
fun ChatHeader(name: String, imageUrl: String, onBackClicked: () -> Unit) {
    Row(
    ) {

        Icon(Icons.Rounded.ArrowBack, contentDescription = null, modifier = Modifier
            .clickable {
                onBackClicked.invoke()
            }
            .padding(8.dp))
        CommonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(text = name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))

    }

}

@Composable
fun ReplyBox(reaply: String, onReplayChange: (String) -> Unit, onSendReplay: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextField(value = reaply, onValueChange = onReplayChange, maxLines = 3)

            Icon(painter = painterResource(id = R.drawable.send),contentDescription = null, modifier = Modifier.clickable { onSendReplay() }.size(50.dp) )
//            Icon(painter = , )

//            Button(onClick = {
//                onSendReplay()
//            }) {
//                Text(text )
//            }
        }


    }
}


@Preview(showBackground = true)
@Composable
private fun pre() {
//    SinlgeChatScreen(navController = {}, vm = LCViewModel())
//    ChatHeader(name = "", imageUrl = "",{})
}


