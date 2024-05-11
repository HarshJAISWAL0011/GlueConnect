package com.apexharn.chatapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost

import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.withTransaction
import com.apexharn.Constants
import com.apexharn.Constants.CURRENT_ACTIVITY
import com.apexharn.Constants.CURRENT_ACTIVITY_ID
import com.apexharn.Constants.MY_ID
import com.apexharn.chatapplication.GroupPage.GroupListViewModel
import com.apexharn.chatapplication.GroupPage.GroupVMFactory
import com.apexharn.chatapplication.HomePage.BottomNavItem
import com.apexharn.chatapplication.HomePage.ChannelList
import com.apexharn.chatapplication.HomePage.ChatList
import com.apexharn.chatapplication.HomePage.CreateGroupBottomSheet
import com.apexharn.chatapplication.HomePage.GroupChatList
import com.apexharn.chatapplication.HomePage.StatusChatList
import com.apexharn.chatapplication.HomePage.getBottomNavItems
import com.apexharn.util.Notification.saveIntSharedPref
import com.apexharn.util.Notification.saveListSharedPref
import com.apexharn.chatapplication.PeopleBook.PeopleViewModel
import com.apexharn.chatapplication.PeopleBook.PeopleViewModelFactory
import com.apexharn.chatapplication.Repository.ChannelRepo
import com.apexharn.chatapplication.db.ChatDatabase
import com.apexharn.chatapplication.db.Sender
import com.apexharn.chatapplication.Repository.ConversationRepository
import com.apexharn.chatapplication.Repository.GroupRepo
import com.apexharn.chatapplication.ui.theme.ChatApplicationTheme
import com.apexharn.chatapplication.WebSocket.WebSocketClient
import com.apexharn.chatapplication.adduser.AddUser
import com.apexharn.chatapplication.channel.ChannelVMFactory
import com.apexharn.chatapplication.channel.ChannelViewModel
import com.apexharn.chatapplication.channel.CreateChannelActivity
import com.apexharn.chatapplication.db.channeldb.ChannelDatabase
import com.apexharn.chatapplication.db.groupdb.Group
import com.apexharn.chatapplication.db.groupdb.GroupDatabase
import com.apexharn.chatapplication.db.groupdb.GroupMember
import com.apexharn.chatapplication.firebase.FirestoreDb.addFCMtoken
import com.apexharn.chatapplication.firebase.FirestoreDb.getInitialData
import com.apexharn.chatapplication.firebase.FirestoreDb.getNewMessageFirestore
import com.apexharn.chatapplication.firebase.Listeners.messageListener
import com.apexharn.chatapplication.profile.AccountActivity
import com.apexharn.chatapplication.webRTC.IncomingCallListener
import com.apexharn.chatapplication.webRTC.RTCActivity

import com.apexharn.retrofit.RetrofitBuilder
import com.apexharn.util.CreateGroupData
 import com.apexharn.util.GroupId
import com.apexharn.util.Notification
import com.apexharn.util.SharedPrefConstant.PREF_SETUP_DATA
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity(), IncomingCallListener {

    val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 123
    lateinit var database: ChatDatabase
    lateinit var groupDatabase: GroupDatabase
    lateinit var channelDatabase: ChannelDatabase
    lateinit var peopleViewModel: PeopleViewModel
    lateinit var convRepo: ConversationRepository
    lateinit var groupViewModel: GroupListViewModel
    lateinit var channelViewModel: ChannelViewModel
    lateinit var groupReop: GroupRepo
    lateinit var channelRepository: ChannelRepo
    val REQUEST_MEDIA_PROJECTION = 34;
    private val PERMISSION_REQUEST_CODE = 1001


    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatApplicationTheme {

                val systemUiController = rememberSystemUiController()
                val statusBarColor = colorResource(id = R.color.primary)

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = statusBarColor,
                        darkIcons = false
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = colorResource(id = R.color.background)
                ) {
                    var navController = rememberNavController()
                    Scaffold(
                        bottomBar = {
                            BottomNavigationBar(navController = navController)
                        },
                        topBar = {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color = colorResource(id = R.color.background))
                                    .shadow(
                                        45.dp,
                                        RoundedCornerShape(bottomEnd = 20.dp),
                                        spotColor = (Color.Blue)
                                    ),
                                color = colorResource(id = R.color.primary),

                                shape = RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp)
                            ) {
                                TopBarDesign(navController)
                            }
                        }
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(it)
                        ) {

                            val viewModelFactory = PeopleViewModelFactory(database, convRepo)
                            peopleViewModel = ViewModelProvider(
                                this,
                                viewModelFactory
                            ).get(PeopleViewModel::class.java)

                            val groupVMFactory = GroupVMFactory(groupDatabase, groupReop)
                            groupViewModel = ViewModelProvider(
                                this,
                                groupVMFactory
                            ).get(GroupListViewModel::class.java)

                            val channelVMFactory =
                                ChannelVMFactory(channelDatabase, channelRepository)
                            channelViewModel = ViewModelProvider(
                                this,
                                channelVMFactory
                            ).get(ChannelViewModel::class.java)

                            NavigationHost(navController, this)
                        }
                    }


                }
            }
        }

//        MY_ID = "968";
        CURRENT_ACTIVITY = "MainActivity"
        CURRENT_ACTIVITY_ID = ""
        database = ChatDatabase.getDatabase(this)
        groupDatabase = GroupDatabase.getDatabase(this)
        channelDatabase = ChannelDatabase.getDatabase(this)
        convRepo = ConversationRepository(database)
        groupReop = GroupRepo(groupDatabase)
        channelRepository = ChannelRepo(channelDatabase)


        val sharedPref = getSharedPreferences(Constants.PREF, MODE_PRIVATE)
        val firstTimeDataGot  = sharedPref.getBoolean(PREF_SETUP_DATA, false)


        WebSocketClient.create(applicationContext, convRepo)
        Notification.createChannelId(this)

        GlobalScope.launch {

//            database.senderDao().insertNewSender(Sender(0,"Emulator","Android SDK built for x86", 0))
//            database.senderDao().insertNewSender(Sender(0,"AK47","email896", 0))
////
//            database.messageDao().insertMessage(Message("2345ff","Android SDK built for x86","text","hello",1,2,2))
////
//            groupDatabase.groupDao().insertNewGroup(Group(0,"group2","grp1",0))
//            groupDatabase.groupDao()  .insertNewGroup(Group(0,"Group Name2", "grp2", 2))
//            groupDatabase.groupDao()  .insertNewGroup(Group(0,"Group Name3", "grp3", 2))
////
//            groupDatabase.groupMemberDao().insertNewMember(GroupMember(0,"01","Harsh 1","grp1",0))
//            groupDatabase.groupMemberDao().insertNewMember(GroupMember(0,"001","Harsh 2","grp2",0))
//            groupDatabase.groupMemberDao().insertNewMember(GroupMember(0,"011","Harsh2","grp1",0))
//            groupDatabase.groupMemberDao().insertNewMember(GroupMember(0,"012","Harsh3","grp1",0))
////
//            groupDatabase.groupMessageDao().insertMessage(GroupMessage("0001","01","text","msg",1,1,1,"grp1"))
//            groupDatabase.groupMessageDao().insertMessage(GroupMessage("00011","01","text","msg",1,2,2,"grp2"))
//            groupDatabase.groupMessageDao().insertMessage(GroupMessage("00012","011","text","msg2",1,3,3,"grp1"))
//            groupDatabase.groupMessageDao().insertMessage(GroupMessage("00014","01","text","msg4",1,5,3,"grp1"))
////
////
//            channelDatabase.channelsDao().addNewChannel(Channels(0,"channel 1","011",0,"This is Description of group",100,1,System.currentTimeMillis(),"Education"))
//            channelDatabase.channelsDao().addNewChannel(Channels(0,"My channel name","0117",0,"This is Description of my channel",100,0,System.currentTimeMillis(),"Other"))
//            channelDatabase.channelMsgDao().insertMessage(ChannelMessage("00014","011","text","msg4",1))
//            channelDatabase.channelMsgDao().insertMessage(ChannelMessage("00015","011","text","msg1",2))

            messageListener(baseContext)
            connectFCM()

            if(!firstTimeDataGot){
                getInitialData(MY_ID, baseContext)
                sharedPref.edit().putBoolean(PREF_SETUP_DATA, true).apply()
            }
//            ChannelDatabase.getDatabase(this@MainActivity).channelMsgDao().deleteAllMessageFrom("nbx8nZOOLgOubpJ0I6t8")

//            val messageList = FirestoreDb.getNewChannelChats(
//                "D3yH8ZDzlX4Rn8lzXtfA",
//                1715224789550
//            )
//            println("message list size extracted from remote db = ${messageList.size} && list = ${messageList}")
        }

//        checkPermissions()
        checkStoragePermission()

        CoroutineScope(Dispatchers.IO).launch {
            getNewMessageFirestore(MY_ID, database, baseContext)
            resetNotificationSharedPref()
             createNotificationChannel(this@MainActivity)
        }

     }


    private fun checkPermissions() {
        // Check if permission is not granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            Toast.makeText(this,"Requesting permission",Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_AUDIO
                ),
                PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted
            // Proceed with file operations
            Toast.makeText(this,"permission granted",Toast.LENGTH_LONG).show()

            performFileOperations()
        }
    }

    private fun performFileOperations() {
        // TODO: Implement file operations here

    }

    // Handle permission request result
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permission granted, proceed with file operations
//                performFileOperations()
//            } else {
//                // Permission denied, handle accordingly (e.g., show rationale, disable features)
//                Toast.makeText(this,"  permission denied",Toast.LENGTH_LONG).show()
//
//            }
//        }
//    }

    override fun onDestroy() {
        WebSocketClient.webSocket?.cancel()
        WebSocketClient.webSocket = null
        super.onDestroy()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "incoming_call_channel_id",
                "Incoming Call Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for incoming call notifications"
                // Configure channel settings if needed (e.g., vibration, lights)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun resetNotificationSharedPref() {
        saveIntSharedPref(this, Constants.total_message_pending, 0)
        saveListSharedPref(this, Constants.notif_users_pending, emptyList())
    }


    private fun checkStoragePermission() {
        if ((ContextCompat.checkSelfPermission(this, RTCActivity.READ_STORAGE_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(this, RTCActivity.WRITE_STORAGE_PERMISSION)
                    != PackageManager.PERMISSION_GRANTED)) {
             requestStoragePermission()
        } else {
//            permission already granted
        }
    }

    private fun requestStoragePermission(dialogShown: Boolean = false) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, RTCActivity.READ_STORAGE_PERMISSION) &&
            ActivityCompat.shouldShowRequestPermissionRationale(this, RTCActivity.WRITE_STORAGE_PERMISSION) &&
            ActivityCompat.shouldShowRequestPermissionRationale(this,RTCActivity.POST_NOTIFICATION_PERMISSION ) &&
            !dialogShown) {
             showPermissionRationaleDialog()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(
                RTCActivity.READ_STORAGE_PERMISSION,
                RTCActivity.WRITE_STORAGE_PERMISSION,
                RTCActivity.POST_NOTIFICATION_PERMISSION,
            ), WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
         }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(this)
            .setTitle("Camera And Audio Permission Required")
            .setMessage("This app need the camera and audio to function")
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                requestStoragePermission(true)
            }
            .setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show()
            }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permission granted
                Toast.makeText(this,"permission granted",Toast.LENGTH_LONG).show()

            } else {
                // Permission denied
                Toast.makeText(this,"permission denied",Toast.LENGTH_LONG).show()
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if (::peopleViewModel.isInitialized) {
            CoroutineScope(Dispatchers.IO).launch {
                peopleViewModel.refreshData()
            }
        }
        if (::groupViewModel.isInitialized) {
            CoroutineScope(Dispatchers.IO).launch {
                groupViewModel.refreshData()
            }
        }
        if (::channelViewModel.isInitialized) {
            CoroutineScope(Dispatchers.IO).launch {
                channelViewModel.refreshData()
            }
        }

    }

     object ButtonRippleTheme : RippleTheme {
        @Composable
        override fun defaultColor() = colorResource(id = R.color.primary)

        @Composable
        override fun rippleAlpha(): RippleAlpha = RippleAlpha(0.2f,0.2f,0.2f,0.2f,)
    }


    private fun connectFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result
            CoroutineScope(Dispatchers.IO).launch {
                addFCMtoken(token)
            }
        })
    }


    @Composable
    fun BottomNavigationBar(navController: NavController) {

        BottomNavigation(
            backgroundColor = colorResource(id = R.color.background),
            modifier = Modifier.height(47.dp),
            elevation = 0.dp
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            getBottomNavItems().forEach { item ->
                var icon = if(item.route == ("home")) ImageVector.vectorResource(R.drawable.person) else if(item.route == "group") ImageVector.vectorResource(R.drawable.group_) else ImageVector.vectorResource(R.drawable.community)
//                CompositionLocalProvider(LocalRippleTheme provides RippleCustomTheme) {
                    BottomNavigationItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        icon = {
                            if (currentRoute == item.route) {
                                Card(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colorResource(id = R.color.primary).copy(0.2f)),

                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = colorResource(id = R.color.primary).copy(
                                            0.2f
                                        )
                                    )
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = colorResource(id = R.color.primary),
                                        modifier = Modifier
                                            .padding(
                                                top = 5.dp,
                                                bottom = 5.dp,
                                                start = 11.dp,
                                                end = 11.dp
                                            )
                                            .size(26.dp)

                                    )
                                }
                            } else {
                                Icon(
                                    icon,
                                    contentDescription = null,
                                    tint = colorResource(id = R.color.unselected_bottom_item),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                        },
                        modifier = Modifier.padding(4.dp),

                        )
//                }
            }
        }

    }



    @Composable
    fun NavigationHost(navController: NavHostController, context: Context) {

        NavHost(navController, startDestination = BottomNavItem.Home.route,
            modifier = Modifier.background(colorResource(id = R.color.background)),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                )

            },
            popEnterTransition = {

                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                )

            },
            popExitTransition = {

                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )

            }) {
            composable(BottomNavItem.Home.route) {

                ChatList(peopleViewModel, context)
            }
            composable(BottomNavItem.Group.route) {
                GroupChatList(groupViewModel, context)
            }
            composable(BottomNavItem.Channel.route) {

                ChannelList(channelViewModel, context)
            }
        }
    }

    @Composable
    private fun TopBarDesign(navController: NavHostController) {

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        var showStatus by remember { mutableStateOf(true) }
        var senderList = remember { mutableStateListOf<Sender>() }
        var selectedSenders = remember { mutableStateListOf<Sender>() }
        var showUserListBottomSheet by remember { mutableStateOf(false) }
        val context = LocalContext.current
        var padding = if (showStatus) PaddingValues(
            start = 15.dp,
            top = 10.dp,
            bottom = 25.dp
        ) else PaddingValues(start = 15.dp, top = 30.dp)
        if (currentRoute != BottomNavItem.Home.route) {
            showStatus = false
        } else
            showStatus = true

        if (showUserListBottomSheet)
            CreateGroupBottomSheet(senderList = senderList,
                { showUserListBottomSheet = it },
                {
                    if (selectedSenders.contains(it)) {
                        selectedSenders.remove(it)
                    } else
                        selectedSenders.add(it)

                }, {
                    val list = mutableListOf<String>()
                    selectedSenders.forEach() {
                        list.add(it.email)
                    }
                    list.add(MY_ID)
                    var data = CreateGroupData(it, list, MY_ID)

                    val apiService = RetrofitBuilder.create()
                    apiService.create_group(data).enqueue((object : retrofit2.Callback<GroupId> {
                        override fun onResponse(
                            call: retrofit2.Call<GroupId>,
                            response: retrofit2.Response<GroupId>
                        ) {
                            if (response.isSuccessful) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    response.body()?.id.let { id ->
                                        groupDatabase.groupDao()
                                            .insertNewGroup(Group(0,"", it, id ?: "", 0))

                                        selectedSenders.forEach() {
                                            groupDatabase.withTransaction {
                                                groupDatabase.groupMemberDao()
                                                    .insertNewMember(
                                                        GroupMember(
                                                            0,
                                                            it.email,
                                                            it.name,
                                                            id ?: "",
                                                            0
                                                        )
                                                    )
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Handle error response
                                println("Failed Group create message result " + response.message())
                            }
                        }

                        override fun onFailure(call: retrofit2.Call<GroupId>, t: Throwable) {
                            println("Failed Group create message result " + t.message + " " + call)
                        }
                    }))
                })
        Column() {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 10.dp, top = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val intent = Intent(context, AddUser::class.java)
                    startActivity(intent) },

                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add_user),
                        contentDescription = "",
                        modifier = Modifier
                            .size(21.dp)
                           ,
                        tint= Color.White
                    )
                }

                Text(
                    text = "MESSAGES",
                    modifier = Modifier.fillMaxWidth(0.9f),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontFamily = FontFamily(
                        Font(R.font.josefinsans)
                    )
                )
                if (showStatus)
                    Image(
                        painter = painterResource(id = R.drawable.profile_placeholder),
                        contentDescription = "",
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Gray)
                            .align(Alignment.CenterVertically)
                            .clickable {
                                val intent = Intent(context, AccountActivity::class.java)
                                startActivity(intent)
                            }
                    ) else {
                    IconButton(onClick = {
                        if (currentRoute == BottomNavItem.Group.route) {
                            showUserListBottomSheet = true
                            selectedSenders.clear()
                            CoroutineScope(Dispatchers.IO).launch {
                                database.senderDao().getAllSenders().collect {
                                    senderList.clear()
                                    senderList.addAll(it)
                                }
                            }
                        } else if (currentRoute == BottomNavItem.Channel.route) {
                            intent = Intent(context, CreateChannelActivity::class.java)
                            startActivity(intent)
                        }
                    }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "", tint = Color.White)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showStatus)
                    StatusChatList(LocalContext.current)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onVideoCallReceived(callerId: String, context: Context) {
       // Intent for "Answer" action
    val answerIntent = Intent(this, RTCActivity::class.java)
        answerIntent.action = "ACTION_ANSWER_CALL"
        answerIntent.putExtra("isVideoCall",true);
        answerIntent.putExtra("isJoin",true);
        answerIntent.putExtra("calleeId",MY_ID);
        answerIntent.putExtra("callerId",callerId);
    val answerPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        answerIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )

    // Intent for "Decline" action
    val declineIntent = Intent(this, MainActivity::class.java)
    declineIntent.action = "ACTION_DECLINE_CALL"
    val declinePendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        declineIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
    )

    // Build the notification with actions
    val notification = NotificationCompat.Builder(this, "incoming_call_channel_id")
        .setContentTitle("Incoming Call")
        .setContentText("John Doe is calling")
        .setSmallIcon(R.drawable.ic_call)
        .addAction(R.drawable.ic_call, "Answer", answerPendingIntent)
        .addAction(R.drawable.ic_baseline_call_end_24, "Decline", declinePendingIntent)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(false)
        .build()

    // Show the notification
    val notificationManager = NotificationManagerCompat.from(context)
    notificationManager.notify(1, notification)

    // Schedule automatic dismissal after 30 seconds
    Handler(Looper.getMainLooper()).postDelayed({
        notificationManager.cancel(1) // Remove the notification after 30 seconds
    }, 30000) // 30 seconds delay
}

    override fun onAudioCallReceived(callerId: String, context: Context) {
        TODO("Not yet implemented")
    }


}