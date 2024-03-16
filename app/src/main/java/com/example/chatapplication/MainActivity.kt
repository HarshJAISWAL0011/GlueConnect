package com.example.chatapplication

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost

import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.room.withTransaction
import com.example.Constants
import com.example.chatapplication.GroupPage.GroupChatList
import com.example.chatapplication.GroupPage.GroupListViewModel
import com.example.chatapplication.GroupPage.GroupVMFactory
import com.example.chatapplication.HomePage.BottomNavItem
import com.example.chatapplication.HomePage.ChatList
import com.example.chatapplication.HomePage.StatusChatList
import com.example.chatapplication.HomePage.getBottomNavItems
import com.example.util.Notification.saveIntSharedPref
import com.example.util.Notification.saveListSharedPref
import com.example.chatapplication.PeopleBook.PeopleViewModel
import com.example.chatapplication.PeopleBook.PeopleViewModelFactory
import com.example.chatapplication.db.ChatDatabase
import com.example.chatapplication.db.Sender
import com.example.chatapplication.Repository.ConversationRepository
import com.example.chatapplication.Repository.GroupRepo
import com.example.chatapplication.ui.theme.ChatApplicationTheme
import com.example.chatapplication.WebSocket.WebSocketClient
import com.example.chatapplication.db.groupdb.Group
import com.example.chatapplication.db.groupdb.GroupDatabase
import com.example.chatapplication.db.groupdb.GroupMember
import com.example.chatapplication.db.groupdb.GroupMessage
import com.example.chatapplication.firebase.FirestoreDb.getNewMessageFirestore
import com.example.util.Notification
import com.example.util.SendersWithLastMessage
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 123
    lateinit  var database: ChatDatabase
    lateinit  var groupDatabase: GroupDatabase
    lateinit var viewModelFactory: PeopleViewModelFactory
    lateinit  var peopleViewModel: PeopleViewModel
    lateinit  var convRepo: ConversationRepository
    lateinit  var groupViewModel: GroupListViewModel
    lateinit  var groupReop: GroupRepo

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
                            Surface( modifier = Modifier
                                .fillMaxWidth()
                                .background(color = colorResource(id = R.color.background))
                                .shadow(
                                    45.dp,
                                    RoundedCornerShape(bottomEnd = 20.dp),
                                    spotColor = (Color.Blue)
                                ),
                                color = colorResource(id = R.color.primary),

                                shape = RoundedCornerShape(bottomEnd = 20.dp, bottomStart = 20.dp)) {
                                TopBarDesign()
                            }
                        }
                    ) {
                        Surface(modifier = Modifier
                            .fillMaxSize()
                            .padding(it) ){

                             viewModelFactory = PeopleViewModelFactory(database,convRepo)
                             peopleViewModel = ViewModelProvider( this,viewModelFactory).get(PeopleViewModel::class.java)

                            var groupVMFactory = GroupVMFactory(groupDatabase,groupReop)
                            groupViewModel = ViewModelProvider( this,groupVMFactory).get(GroupListViewModel::class.java)

                            NavigationHost(navController,this)
                        }
                    }

                }
            }
        }

        database = ChatDatabase.getDatabase(this)
        groupDatabase = GroupDatabase.getDatabase(this)
        convRepo = ConversationRepository(database)
        groupReop = GroupRepo(groupDatabase)



        WebSocketClient.create(applicationContext,convRepo)
        connectFCM()
        Notification.createChannelId(this)

        GlobalScope.launch {

//
            val time = System.currentTimeMillis()
//            groupDatabase.withTransaction {
//                for (i in 0..3) {

//                    groupDatabase.groupDao()
//                        .insertNewGroup(Group(0,"Group Name1", "grp1", 2))
//            groupDatabase.groupDao()  .insertNewGroup(Group(0,"Group Name2", "grp2", 2))
//            groupDatabase.groupDao()  .insertNewGroup(Group(0,"Group Name3", "grp3", 2))
////                }
////            }
//            groupDatabase.groupMemberDao().insertNewMember(GroupMember(0,"01","Harsh","grp1",0))
//            groupDatabase.groupMemberDao().insertNewMember(GroupMember(0,"01","Harsh","grp2",0))
//            groupDatabase.groupMemberDao().insertNewMember(GroupMember(0,"011","Harsh2","grp1",0))
//            groupDatabase.groupMemberDao().insertNewMember(GroupMember(0,"012","Harsh3","grp1",0))
//
//            groupDatabase.groupMessageDao().insertMessage(GroupMessage("0001","01","text","msg",1,1,1,"grp1"))
//            groupDatabase.groupMessageDao().insertMessage(GroupMessage("00011","01","text","msg",1,2,2,"grp2"))
//            groupDatabase.groupMessageDao().insertMessage(GroupMessage("00012","01","text","msg2",1,3,3,"grp1"))
//            groupDatabase.groupMessageDao().insertMessage(GroupMessage("00014","01","text","msg4",1,5,3,"grp1"))
//            groupDatabase.groupMessageDao().insertMessage(GroupMessage("00013","011","text","msg3",1,4,4,"grp1"))
        }


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            getNewMessageFirestore("968", database)
        }

        resetNotificationSharedPref()
        requestStoragePermission()
    }

    private fun resetNotificationSharedPref() {
        saveIntSharedPref(this,Constants.total_message_pending,0)
        saveListSharedPref(this,Constants.notif_users_pending, emptyList())
    }

    fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with file operations
            } else {
                // Permission denied, handle accordingly
            }
        }
    }


    override fun onResume() {
        super.onResume()
        if(::peopleViewModel.isInitialized){
            CoroutineScope(Dispatchers.IO).launch {
                peopleViewModel.refreshData()

                println("TEst : resume ran")
            }
        }
    }

    private fun connectFCM(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
            val token = task.result

            // Log and toast
            val msg = token
            println("firebase token =$msg")
        })
    }

    override fun onDestroy() {
        super.onDestroy()
//        okHttpClient.dispatcher.executorService.shutdown()
    }


@Composable
fun BottomNavigationBar(navController: NavController) {

    BottomNavigation(backgroundColor = colorResource(id = R.color.background),
        modifier = Modifier.height(47.dp),
        elevation = 0.dp) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        getBottomNavItems().forEach { item ->

            BottomNavigationItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                icon = {
                    if(currentRoute == item.route){
                        Card(modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorResource(id = R.color.primary)),
                            elevation = CardDefaults.cardElevation(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.primary))){
                            Icon(item.icon, contentDescription = null, tint = colorResource(id = R.color.white),
                                modifier = Modifier.padding(4.dp))
                        }
                    }else{
                        Icon(item.icon, contentDescription = null, tint = colorResource(id = R.color.unselected_bottom_item),
                            modifier = Modifier.size(24.dp ))
                    }

                       },
                modifier = Modifier.padding(4.dp),

            )
        }
    }

}

@Composable
fun NavigationHost(navController: NavHostController, context: Context ) {

    NavHost(navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {

            ChatList(peopleViewModel,context)
        }
        composable(BottomNavItem.Group.route) {
            GroupChatList(groupViewModel,context)
        }
        composable(BottomNavItem.Profile.route) { /* Profile Screen UI */ }
    }
}

@Composable
private fun TopBarDesign(){
    Column (){
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, end = 10.dp, top = 15.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = R.drawable.drawer), contentDescription ="", modifier = Modifier.size(30.dp) )
            Text(text = "MESSAGES", modifier =Modifier.fillMaxWidth(0.9f), textAlign = TextAlign.Center , fontFamily = FontFamily(
                Font(R.font.josefinsans)
            ) )
            Image(painter = painterResource(id = R.drawable.profile_placeholder), contentDescription ="", modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Gray)
                .align(Alignment.CenterVertically)
            )
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(start = 15.dp, top = 30.dp, bottom = 25.dp),
            verticalAlignment = Alignment.CenterVertically) {
            StatusChatList()
        }
    }
}
}