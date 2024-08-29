package com.example.livechat

import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.example.livechat.Data.CHATS
import com.example.livechat.Data.ChatData
import com.example.livechat.Data.ChatUser
//import com.example.livechat.Data.Chats
import com.example.livechat.Data.Event
import com.example.livechat.Data.MESSAGE
import com.example.livechat.Data.Message
import com.example.livechat.Data.STATUS
import com.example.livechat.Data.Status
import com.example.livechat.Data.USER_NODE
import com.example.livechat.Data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LCViewModel @Inject constructor(

    val auth: FirebaseAuth,
    var db: FirebaseFirestore,
    val storage: FirebaseStorage

) : ViewModel() {


    var inProgress = mutableStateOf(false)//->indicates whether a process is ongoing or not
    var inProcessChats = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Event<String>?>(null)//a state holds events / messages
    var signIn = mutableStateOf(false)//a state idicates if the user is signed in
    var userData = mutableStateOf<UserData?>(null)//a state that holds the userss data retrieved from firestore
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration? = null
    val status = mutableStateOf<List<Status>>(listOf())
    val inProgressStatus = mutableStateOf(false)

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser != null
        currentUser?.uid?.let {
            getUserData(it)
        }
    }

    fun populateMessage(chatId: String) {
        inProgressChatMessage.value = true
        currentChatMessageListener = db.collection(CHATS).document(chatId).collection(MESSAGE)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                }
                if (value != null) {
                    chatMessages.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timestamp }
                    inProgressChatMessage.value = false
                }
            }
    }

    fun DepopulateMessage() {
        chatMessages.value = listOf()
        currentChatMessageListener = null
    }

    fun populateChats() {
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId),
            )
        ).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error)

            }
            if (value != null) {
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProcessChats.value = false
            }
        }


    }

    fun onSendReply(chatID: String, message: String) {
        val time = Calendar.getInstance().time.toString()
        val msg = Message(userData.value?.userId, message, time)
        db.collection(CHATS).document(chatID).collection(MESSAGE).document().set(msg)


    }

    fun signUp(
        name: String,
        number: String,
        email: String,
        password: String
    )//->handles signed up process
    {
        if (name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty())//make sure all fields are fullfied
        {
            handleException(customMessage = "Please Fill All Fields")
            return
        }
        inProgress.value = true
        db.collection(USER_NODE).whereEqualTo("number", number).get()
            .addOnSuccessListener { //check phone number unique or not
                if (it.isEmpty)//if yes try to create new user
                {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        inProgress.value = true
                        if (it.isSuccessful) {
                            signIn.value = true
                            createOrUpdateProfile(name, number)
                            Log.d("TAG", "signUp:User Logged In")

                        } else {
                            handleException(it.exception, customMessage = "Sign Up Failed")
                        }
                    }
                } else//if not then handle exception
                {
                    handleException(customMessage = "number ALready Exist")
                    inProgress.value = false
                }
            }


    }
//function for login

    fun loginIn(email: String, password: String) {
        if (email.isEmpty() or password.isEmpty()) {
            handleException(customMessage = "Please Fill the all Fields")
            return
        } else {
            inProgress.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    signIn.value = true
                    inProgress.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }

                } else {
                    handleException(exception = it.exception, customMessage = "Login failed")
                }

            }


        }


    }

    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri)
        {
            createOrUpdateProfile(imageurl = uri.toString())

        }

    }

    fun uploadImage(uri: Uri, onSuccess: (Uri) -> Unit) {
        inProgress.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener { uri ->
                onSuccess(uri)
                inProgress.value = false // Moved here
            }
        }
            .addOnFailureListener {
                handleException(it)
                inProgress.value = false // Also handle failure case
            }
    }


    fun createOrUpdateProfile(
        name: String? = null,
        number: String? = null,
        imageurl: String? = null
    ) {
        val uid = auth.currentUser?.uid // Get current user's UID
        if (uid == null) {
            handleException(customMessage = "User not logged in")
            return
        }

        val updatedUserData = UserData(
            userId = uid,
            name = name ?: userData.value?.name,
            number = number ?: userData.value?.number,
            imageUrl = imageurl ?: userData.value?.imageUrl
        )

        // Update the user data in Firestore
        inProgress.value = true
        db.collection(USER_NODE).document(uid).set(updatedUserData)
            .addOnSuccessListener {
                inProgress.value = false
                userData.value = updatedUserData // Update local userData state
            }
            .addOnFailureListener { exception ->
                handleException(exception, "Failed to update profile")
            }
    }


    private fun getUserData(uid: String) {//retrives user data from firestore tro getdatastate
        inProgress.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if (error != null) {
                handleException(error, "can not retreive user")
            }
            if (value != null) {
                var user = value.toObject<UserData>()
                userData.value = user
                inProgress.value = false
                populateChats()
                populateStatuses()
            }
        }
    }

    fun handleException(
        exception: Exception? = null,
        customMessage: String = ""
    )//handles exceoption
    {
        Log.e("LiveChatApp", "live chat exception", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage ?: " "
        val message = if (customMessage.isNullOrEmpty()) errorMsg else customMessage
        eventMutableState.value = Event(message)
        inProgress.value = false
    }

    fun logout() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        DepopulateMessage()
        currentChatMessageListener = null
        eventMutableState.value = Event("Logged Out")
    }

    fun onAddChat(number: String) {
        if (number.isEmpty() or !number.isDigitsOnly()) {
            handleException(customMessage = "Number must be contain digit only")
        } else {

            db.collection(CHATS).where(
                Filter.or(

                    Filter.and(
                        Filter.equalTo("user1.number", number),
                        Filter.equalTo("user2.number", userData.value?.number)
                    ),
                    Filter.and(
                        Filter.equalTo("user2.number", userData.value?.number),
                        Filter.equalTo("user1.number", number)
                    )

                )
            ).get().addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection(USER_NODE).whereEqualTo("number", number).get()
                        .addOnSuccessListener {
                            if (it.isEmpty) {
                                handleException(customMessage = "number not found")
                            } else {
                                val chatPartner = it.toObjects<UserData>()[0]
                                val id = db.collection(CHATS).document().id
                                val chat = ChatData(
                                    chatId = id,
                                    ChatUser(
                                        userData.value?.userId,
                                        userData.value?.name,
                                        userData.value?.imageUrl,
                                        userData.value?.number
                                    ),
                                    ChatUser(
                                        chatPartner.userId,
                                        chatPartner.name,
                                        chatPartner.imageUrl,
                                        chatPartner.number
                                    )
                                )
                                db.collection(CHATS).document(id).set(chat)
                            }
                        }
                        .addOnFailureListener {
                            handleException(it)
                        }
                } else {
                    handleException(customMessage = "chat alrady exist")
                }
            }
        }
    }

    fun uploadStatus(uri: Uri) {
        uploadImage(uri) { imageUrl ->
            createStatus(imageUrl.toString())
        }
    }

    fun createStatus(imageUrl: String) {
        val user = userData.value
        if (user != null) {
            val newStatus = Status(
                ChatUser(
                    userId = user.userId,
                    name = user.name,
                    imageUrl = user.imageUrl,
                    number = user.number
                ),
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )
            db.collection(STATUS).document().set(newStatus)
                .addOnSuccessListener {
                    Log.d("createStatus", "Status created successfully")
                }
                .addOnFailureListener { e ->
                    handleException(e)
                }
        } else {
            Log.e("createStatus", "User data is null")
        }
    }

    fun populateStatuses() {
        val timeDelta = 24L * 60 * 60 * 1000 // 24 hours in milliseconds
        val cutOff = System.currentTimeMillis() - timeDelta

        inProgressStatus.value = true

        val currentUserId = userData.value?.userId
        if (currentUserId == null) {
            Log.e("populateStatuses", "User ID is null")
            inProgressStatus.value = false
            return
        }

        db.collection(CHATS)
            .whereGreaterThan("timestamp", cutOff)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    handleException(error)
                    inProgressStatus.value = false
                    return@addSnapshotListener
                }

                val chats = value?.toObjects<ChatData>() ?: emptyList()
                val currentConnections = mutableListOf(currentUserId)
                chats.forEach { chat ->
                    if (chat.user1.userId == currentUserId) {
                        chat.user2.userId?.let { currentConnections.add(it) }
                    } else {
                        chat.user1.userId?.let { currentConnections.add(it) }
                    }
                }

                db.collection(STATUS)
                    .whereIn("user.userId", currentConnections)
                    .addSnapshotListener { statusValue, statusError ->
                        if (statusError != null) {
                            handleException(statusError)
                        } else {
                            status.value = statusValue?.toObjects() ?: emptyList()
                        }
                        inProgressStatus.value = false
                    }
            }
    }}


