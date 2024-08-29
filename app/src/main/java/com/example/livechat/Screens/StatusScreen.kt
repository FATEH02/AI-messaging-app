package com.example.livechat.Screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.ui.theme.CommonDivider
import com.example.livechat.ui.theme.CommonProgressBar
import com.example.livechat.ui.theme.CommonRow
import com.example.livechat.ui.theme.TitleText
import com.example.livechat.ui.theme.navigateTo

@Composable
fun StatusScreen(navController: NavController, vm: LCViewModel) {

    val inProgress = vm.inProgressStatus
    if (inProgress.value) {
        Log.d("StatusScreen", "inProgressStatus: ${inProgress.value}, status: ${vm.status.value}")

        CommonProgressBar()
    } else {
        val status = vm.status.value
        val userData = vm.userData.value
        val myStatus = status.filter {
            it.user.userId == userData?.userId
        }
        val otherStatus = status.filter {
            it.user.userId != userData?.userId
        }
        val launcher =
            rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    vm.uploadStatus(uri)
                }
            }

        Scaffold(
            floatingActionButton = {
                FAAB {
                    launcher.launch("image/*")
                }
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(it)
                ) {
                    TitleText(txt = "Status")
                    if (status.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "No Statuses available")
                        }
                    } else {
                        if (myStatus.isNotEmpty()) {
                            CommonRow(
                                imageUrl = myStatus[0].user.imageUrl,
                                name = myStatus[0].user.name
                            ) {
                                navigateTo(
                                    navController = navController,
                                    DestinationScreen.SingleStatus.createRoute(myStatus[0].user.userId!!)
                                )
                            }
                            CommonDivider()
                            val uniqueUsers = otherStatus.map { it.user }.toSet().toList()
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(uniqueUsers) { user ->
                                    CommonRow(imageUrl = user.imageUrl, name = user.name) {
                                        navigateTo(
                                            navController = navController,
                                            DestinationScreen.SingleStatus.createRoute(user.userId!!)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    BottomNavigationManu(
                        selectedItem = BottomNavigationItem.STATUSLIST,
                        navController = navController
                    )
                }
            }
        )
        Log.d("StatusScreen", "inProgressStatus: ${inProgress.value}, status: ${vm.status.value}")
    }
}

@Composable
fun FAAB(onFabClick: () -> Unit) {
    FloatingActionButton(
        onClick = onFabClick,
        containerColor = MaterialTheme.colorScheme.secondary,
        shape = CircleShape,
        modifier = Modifier.padding(bottom = 40.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Add Status",
            tint = Color.White
        )
    }
}
