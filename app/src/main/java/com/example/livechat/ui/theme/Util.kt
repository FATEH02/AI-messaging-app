package com.example.livechat.ui.theme

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.R

fun navigateTo(navController: NavController, route: String) {

    navController.navigate(route)
    {
        popUpTo(route)//remove from navigation stack
        launchSingleTop = true //is already awailable the take this and put at top of the stack
    }

}

@Composable
fun CommonProgressBar() {
    Row(modifier = Modifier
        .alpha(0.5f)
        .background(Color.LightGray)
        .clickable(enabled = false) {}
        .fillMaxSize(),

        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
    }


}

@Composable
fun CheckSignedIn(vm: LCViewModel, navController: NavController) {
    val alreadySignIn = remember { mutableStateOf(false) }

    val signIn = vm.signIn.value

    if (signIn && !alreadySignIn.value) {
        alreadySignIn.value = true

        navController.navigate(DestinationScreen.ChatList.route)
        {
            popUpTo(0)//go to 0th screen of navigation stack
        }
    }


}

@Composable
fun CommonDivider() {
    Divider(
        color = Color.LightGray,
        thickness = 1.dp,
        modifier = Modifier
            .alpha(0.3f)
            .padding(top = 8.dp, bottom = 8.dp)
    )


}

@Composable
fun CommonImage(
    data: String?,
    modifier: Modifier = Modifier.wrapContentSize(),
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current

    val imageRequest = ImageRequest.Builder(context)
        .data(data)
        .crossfade(true) // Optional: Adds a crossfade animation
        .placeholder(R.drawable.img)
        .error(R.drawable.img)
        .build()

    val painter = rememberImagePainter(imageRequest)

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier, // Apply the passed modifier
        contentScale = contentScale
    )
}


@Composable
fun TitleText(txt: String) {
    Text(
        text = txt,
        fontWeight = FontWeight.Bold,
        fontSize = 35.sp,
        modifier = Modifier.padding(8.dp)
    )


}

@Composable
fun CommonRow(imageUrl: String?, name: String?, onItemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(75.dp)
            .clickable { onItemClick.invoke() },
        verticalAlignment = Alignment.CenterVertically
    ) {

        CommonImage(
            data = imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    Color.Red
                )
        )
        Text(
            text = name?:"----",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )

    }

}


@Preview(showBackground = true)
@Composable
private fun hello() {

    CommonRow(imageUrl = "", name ="fateh" ) {
        
    }
}

