package com.example.livechat.Screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.livechat.Data.UserData
import com.example.livechat.DestinationScreen
import com.example.livechat.LCViewModel
import com.example.livechat.ui.theme.CommonDivider
import com.example.livechat.ui.theme.CommonImage
import com.example.livechat.ui.theme.CommonProgressBar
import com.example.livechat.ui.theme.navigateTo
import com.google.android.gms.common.internal.service.Common


@Composable
fun ProfileScreen(navController: NavController, vm: LCViewModel) {
    val inProgress = vm.inProgress.value

    if (inProgress) {
        CommonProgressBar()
    } else {
        val userData = vm.userData.value
        var name by rememberSaveable { mutableStateOf(userData?.name?:"") }
        var number by rememberSaveable { mutableStateOf(userData?.number?:"") }


        Column(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())) {

            ProfileContent(
                modifier = Modifier.fillMaxWidth(),
                vm = vm,
                name = name, // Replace with actual name
                number = number, // Replace with actual number
                onNameChange = {name=it},
                onNumberChange = {number=it},
                onSave = {
                    vm.createOrUpdateProfile(
                        name=name,number=number
                    )
                },
                onBack = {
                    navigateTo(navController,DestinationScreen.ChatList.route)
                },
                onLogOut = {
                  vm.logout()
                    navigateTo(navController,DestinationScreen.Login.route)

                }
            )

            Spacer(modifier = Modifier.weight(1f))

            BottomNavigationManu(
                selectedItem = BottomNavigationItem.PROFILE,
                navController = navController
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    modifier: Modifier,
    vm: LCViewModel,
    name: String,
    number: String,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onLogOut: () -> Unit
) {
    Column(modifier = modifier) {
        val imageUri = vm.userData.value?.imageUrl

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Back", Modifier.clickable { onBack() })
            Text(text = "Save", Modifier.clickable { onSave() })
        }

        ProfileImage(imageUrl = imageUri, vm = vm)

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Name", Modifier.width(100.dp))
            TextField(
                value = name,
                onValueChange = onNameChange,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Number", Modifier.width(100.dp))
            TextField(
                value = number,
                onValueChange = onNumberChange,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            )
        }

        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            horizontalArrangement = Arrangement.Center) {
            Text(text = "LogOut", Modifier.clickable { onLogOut() })
        }
    }
}

@Composable
fun ProfileImage(imageUrl: String?, vm: LCViewModel) {
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let { vm.uploadProfileImage(uri) }
    }

    Box(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .clickable { launcher.launch("image/*") }) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
            ) {
                CommonImage(data = imageUrl)
            }
            Text(text = "Change Profile Picture")
        }
        if (vm.inProgress.value) {
            CommonProgressBar()
        }
    }
}
