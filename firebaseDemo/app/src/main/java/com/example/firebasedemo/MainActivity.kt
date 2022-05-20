package com.example.firebasedemo

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebasedemo.ui.theme.*
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage

/*
    Author: Joey yang
    Note:
    1. About join firebase into project, you can refer to udemy class.
       Tools > Firebase. (在 Firebase 的 "Dashboard" 可以看到本專案)
    2. Anonymous login official document.
       https://firebase.google.com/docs/auth/android/anonymous-auth#kotlin+ktx
    3. Mail login function:
       auth.createUserWithEmailAndPassword

 */
class MainActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val mainViewModel by viewModels<MainViewModel>()

    //TODO("Need to join APP check, ref: https://www.youtube.com/watch?v=Fjj4fmr2t04&feature=emb_imp_woyt")
    //TODO("Storage rules adjust: ref: udemy firebase class Ch23")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            analytics = Firebase.analytics
            auth = Firebase.auth
            //重要,若伺服器選美國,就只要用Firebase.database即可,否則要把資料庫的根網址寫入,如下
            database = Firebase.database("https://myfirebase2022-31c42-default-rtdb.asia-southeast1.firebasedatabase.app")
            storage = Firebase.storage("gs://myfirebase2022-31c42.appspot.com")

            FirebaseDemoTheme {
                val navController = rememberNavController() //Navigation Step2

                NavHost(
                    navController = navController,
                    startDestination = "initial_screen"
                ) {
                    composable("initial_screen") {
                        //first screen
                        InitialScreen01(navController = navController)
                    }
                    composable("login_screen") {
                        //first screen
                        LoginScreen02(navController = navController, auth, this@MainActivity)
                    }
                    composable("loginMail_screen") {
                        //first screen
                        LoginMailScreen03(navController = navController, auth, this@MainActivity, mainViewModel)
                    }
                    composable("loginNewUser_screen") {
                        //first screen
                        LoginNewUser04(navController = navController, auth, this@MainActivity)
                    }
                    composable("shop_screen_info") {
                        //second screen
                        ShopScreen05Show(navController = navController, auth, this@MainActivity, mainViewModel, database)
                    }
                    composable("shop_image_upload") {
                        //second screen
                        ShopScreen06ImageUpload(navController = navController, auth, this@MainActivity, mainViewModel, database, storage)
                    }
                }
            }
        }
    }

}

@Composable
fun InitialScreen01(navController: NavController) {
    val interactionSourceTest = remember { MutableInteractionSource() }
    val pressState = interactionSourceTest.collectIsPressedAsState()
    val colorMy = if (pressState.value) YellowFFEB3B else Green4CAF50 //Import com.pcp.composecomponent.ui.theme.YellowFFEB3B
    val itemList = listOf("Anonymous login", "e-mail login", "show shop table info", "upload shop image")

    Column(verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Function list")

        for(info in itemList) {
            OutlinedButton(
                onClick = {
                    when (info) {
                        "Anonymous login" -> navController.navigate("login_screen")
                        "e-mail login" -> navController.navigate("loginMail_screen")
                        "show shop table info" -> navController.navigate("shop_screen_info")
                        "upload shop image" -> navController.navigate("shop_image_upload")
                    }
                },
                modifier = Modifier.fillMaxWidth(1f),
                enabled = true,
                interactionSource = interactionSourceTest,
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                ),     //注意,這跟官方的文件寫的不太一樣,但是不會有錯誤發生,反而用官方的寫會有錯誤發生
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(
                    width = 10.dp,
                    brush = Brush.horizontalGradient(listOf(Purple700, PinkE91E63))
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = colorMy,
                    contentColor = Teal200
                ),
            ) {
                Text(
                    color = Purple500,
                    text = info
                )
            }
        }
    }


}

@Composable
fun LoginScreen02(navController: NavController, auth: FirebaseAuth, activity: MainActivity) {
    Text(text = "First screen",
        modifier = Modifier.clickable(onClick = {
            navController.navigate("loginMail_screen")
        })
    )
    auth.signInAnonymously()
        .addOnCompleteListener(activity) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("TAG", "signInAnonymously:success")
                val user = auth.currentUser
                Toast.makeText(activity.baseContext, "signInAnonymously:success, ${user.toString()}",
                    Toast.LENGTH_SHORT).show()
                //updateUI(user)
            } else {
                // If sign in fails, display a message to the user.
                Log.w("TAG", "signInAnonymously:failure", task.exception)
                Toast.makeText(activity.baseContext, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
                //updateUI(null)
            }
        }

    //TODO("Need to join viewModel and remember State to fit MVVM style")
}

@Composable
fun LoginMailScreen03(navController: NavController, auth: FirebaseAuth, activity: MainActivity, mainViewModel: MainViewModel) {
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }

    val interactionSourceTest = remember { MutableInteractionSource() }
    val pressState = interactionSourceTest.collectIsPressedAsState()
    val borderColor = if (pressState.value) YellowFFEB3B else Green4CAF50 //Import com.pcp.composecomponent.ui.theme.YellowFFEB3B

    val loginUser by mainViewModel._loginUser.observeAsState(null)

    var loginUserMail = ""
    var nickName = ""

    mainViewModel.authStateListener(auth)
    mainViewModel.updateUserStatus(auth)
    loginUser?.let { user ->
        user.email?.let { mail ->
            loginUserMail = mail
        }
        user.displayName?.let { name ->
            nickName = name
        }
    }
    if(loginUser == null)
        Log.v("Test", "LoginUser 00 = null")
    else
        Log.v("Test", "LoginUser 01 = ${loginUser!!.toString()},  ${loginUser!!.email}")
    //TODO("Need to modify UI more beautiful")
    Column(verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Second screen: Login mail screen",
            modifier = Modifier.clickable(onClick = {
                navController.navigate("login_screen")
            })
        )
        TextFieldShow(MainViewModel.TEXT_EMAIL, emailText) { info -> emailText = info }
        TextFieldShow(MainViewModel.TEXT_PASSWORD, passwordText) { info -> passwordText = info }
        Button( //Button只是一個容器,裡面要放文字,就是要再加一個Text
            modifier = Modifier.fillMaxHeight(0.5f),
            //enabled = false,
            enabled = true, //如果 enabled 設為false, border, interactionSource就不會有變化
            interactionSource = interactionSourceTest,
            elevation = ButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp,
                disabledElevation = 2.dp
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(5.dp, color = borderColor),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White,
                contentColor = Color.Red),
            contentPadding = PaddingValues(4.dp, 3.dp, 2.dp, 1.dp),
            onClick = { if(loginUserMail == "") {
                if(emailText.isEmpty()) {
                    Toast.makeText(activity, "Please enter account", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if(passwordText.isEmpty()) {
                    Toast.makeText(activity, "Please enter password", Toast.LENGTH_LONG).show()
                    return@Button
                }
                auth.signInWithEmailAndPassword(emailText,passwordText)
                    .addOnFailureListener { exception ->
                        Toast.makeText(activity, "Login error: ${exception.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } else {
                auth.signOut()
            } })
            {
                Text(text = if(loginUserMail == "") "Login" else "Logout")
            }

        Button( //Button只是一個容器,裡面要放文字,就是要再加一個Text
            modifier = Modifier.fillMaxHeight(0.5f),
            //enabled = false,
            enabled = true, //如果 enabled 設為false, border, interactionSource就不會有變化
            interactionSource = interactionSourceTest,
            elevation = ButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp,
                disabledElevation = 2.dp
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(5.dp, color = borderColor),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White,
                contentColor = Color.Red),
            contentPadding = PaddingValues(4.dp, 3.dp, 2.dp, 1.dp),
            onClick = { navController.navigate("loginNewUser_screen") }) {
            Text(text = "Create new account") }

        Text(text = if(loginUserMail == "") "Status: not login" else "Status: $loginUserMail login, nick name = $nickName")
    }
}

@Composable
fun LoginNewUser04(navController: NavController, auth: FirebaseAuth, activity: MainActivity) {
    var emailText by remember { mutableStateOf("") }
    var nickNameText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var passwordConfirmText by remember { mutableStateOf("") }

    val interactionSourceTest = remember { MutableInteractionSource() }
    val pressState = interactionSourceTest.collectIsPressedAsState()
    val borderColor = if (pressState.value) YellowFFEB3B else Green4CAF50 //Import com.pcp.composecomponent.ui.theme.YellowFFEB3B

    //TODO("Need to modify UI more beautiful")
    Column(verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Login new user screen",
            modifier = Modifier.clickable(onClick = {
                navController.navigate("login_screen")
            })
        )
        TextFieldShow(MainViewModel.TEXT_NEW_ACCOUNT_EMAIL , emailText) { info -> emailText = info }
        TextFieldShow(MainViewModel.TEXT_NICKNAME, nickNameText) { info -> nickNameText = info }
        TextFieldShow(MainViewModel.TEXT_PASSWORD, passwordText) { info -> passwordText = info }
        TextFieldShow(MainViewModel.TEXT_PASSWORD_CONFIRM, passwordConfirmText) { info -> passwordConfirmText = info }
        Button( //Button只是一個容器,裡面要放文字,就是要再加一個Text
            modifier = Modifier.fillMaxHeight(0.5f),
            //enabled = false,
            enabled = true, //如果 enabled 設為false, border, interactionSource就不會有變化
            interactionSource = interactionSourceTest,
            elevation = ButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp,
                disabledElevation = 2.dp
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(5.dp, color = borderColor),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White,
                contentColor = Color.Red),
            contentPadding = PaddingValues(4.dp, 3.dp, 2.dp, 1.dp),
            onClick = {
                if(emailText.isEmpty()) {
                    Toast.makeText(activity, "Please enter account", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if(nickNameText.isEmpty()) {
                    Toast.makeText(activity, "Please enter nickName", Toast.LENGTH_LONG).show()
                    return@Button
                }
                if(passwordText != passwordConfirmText) {
                    Toast.makeText(activity, "Password not match", Toast.LENGTH_LONG).show()
                    return@Button
                }
                auth.createUserWithEmailAndPassword(emailText, passwordText).addOnCompleteListener() { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity, "Account Created!", Toast.LENGTH_LONG).show()
                        var user = auth.currentUser
                        var request = UserProfileChangeRequest.Builder().setDisplayName(nickNameText).build()
                        user?.let { userInfo ->
                            userInfo.updateProfile(request)
                            //要做這是因為上面的寫入,會有時間差,所以回到前頁時,DisplayName還不會被寫入,作者建議就重新再登入一次看看
                            //但目前試還是有問題,要再改
                            auth.signOut()
                            auth.signInWithEmailAndPassword(emailText, passwordText)
                        }
                        navController.navigate("loginMail_screen")
                    }
                }.addOnFailureListener() { task ->
                    Toast.makeText(activity, "Fail! ${task.message}", Toast.LENGTH_LONG).show()
                }
            })
        {
            Text(text = "Create account")
        }
    }
}

@Composable
fun ShopScreen05Show(navController: NavController, auth: FirebaseAuth, activity: MainActivity, mainViewModel: MainViewModel, database: FirebaseDatabase) {
    // 1.先確認有無e-mail login
    val loginUser by mainViewModel._loginUser.observeAsState(null)

    //var loginUserMail = ""
    //var nickName = ""
    val shopInfo by mainViewModel._shopInfo.observeAsState(null)
    mainViewModel.authStateListener(auth)
    mainViewModel.getShopList(database)

    Column(verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Shop info list",
            modifier = Modifier.clickable(onClick = {
                navController.navigate("login_screen")
            })
        )
        //Text(text = { if(loginUser == null) "Shop info list" else "bbb" })
        Text(text =
            if(loginUser == null) {
                "Not login, no shop info" }
            else {
                if(shopInfo == null)
                    "Login but no shop data"
                else "Login and have shop data"
            })
        LazyColumn(Modifier.fillMaxWidth()) {
            shopInfo?.let { info ->
                items(info.size) {
                    for(shopData in info) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(modifier = Modifier.fillMaxWidth(0.4f), text = "店名: ${shopData.name}")
                            Text(modifier = Modifier.fillMaxWidth(0.6f), text = "電話: ${shopData.telephone}")
                        }
                        Text(modifier = Modifier.fillMaxWidth(), text = "地址: ${shopData.address}")
                        Divider(
                            color = Color.Green,
                            thickness = 1.dp,
                            startIndent = 50.dp)
                    }
                }
            }
        }
    }
/*  The process of write to real time database, need to join a new button.
    Log.v("TEST", "shop list 001")
    //database.getReference("bento/store")

    var mRef = database.reference.child("bento").child("store")
    Log.v("TEST", "shop list 001 ${mRef.toString()}")
    var msgRef = mRef.push()
    var msg = HashMap<String, String>()
    msg.put("addr", "aaaaaa")
    msg.put("id", "1")
    msg.put("name", "bbb")
    msg.put("tel", "12345678")
    msgRef.setValue(msg)
    Log.v("TEST", "shop list 002")

 */
}

@Composable
fun TextFieldShow(from: Int, value: String, valueAlter: (info: String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { valueAlter(it) },
//        modifier = Modifier
//            .fillMaxWidth()
//            .onGloballyPositioned { coordinates ->  //重要
//                textfieldSize = coordinates.size.toSize()
//            },
//        enabled = true,
//        readOnly = true,
//        textStyle = TextStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
//        label = { Text("OutlinedTextField & DropdownMenu")},
        placeholder = {
            when (from) {
                MainViewModel.TEXT_EMAIL -> Text("E-mail:")
                MainViewModel.TEXT_PASSWORD -> Text("Password:")
                MainViewModel.TEXT_NICKNAME -> Text("Nick name:")
                MainViewModel.TEXT_PASSWORD_CONFIRM -> Text("confirm password")
                MainViewModel.TEXT_NEW_ACCOUNT_EMAIL -> Text("new account(Email)")
            }
        },
//        leadingIcon = {    //重要
//            Icon(icon2, "contentDescription",
//                Modifier.clickable { expanded = !expanded })
//        },
//        trailingIcon = {    //重要
//            Icon(icon, "contentDescription",
//                Modifier.clickable { expanded = !expanded })
//        },
//        isError = false,    //指示是否text fields的目前值是有錯的,若true, label, bottom indicator和 trailingIcon 預設都顯示錯誤的顏色
//        visualTransformation = PasswordVisualTransformation(), //可看原始碼
//        keyboardOptions = keyboardOption,
//        keyboardActions = keyboardAction,
//        singleLine = false,
//        maxLines = 2,
//        interactionSource = interactionSourceTest,
//        shape = RoundedCornerShape(8.dp),
//        colors = TextFieldDefaults.outlinedTextFieldColors(
//            backgroundColor = PinkE91E63),
    )
}

@Composable
fun ShopScreen06ImageUpload(navController: NavController, auth: FirebaseAuth, mainActivity: MainActivity, mainViewModel: MainViewModel, database: FirebaseDatabase, storage: FirebaseStorage) {
    val interactionSourceTest = remember { MutableInteractionSource() }
    val pressState = interactionSourceTest.collectIsPressedAsState()
    val borderColor = if (pressState.value) YellowFFEB3B else Green4CAF50 //Import com.pcp.composecomponent.ui.theme.YellowFFEB3B

    Column(verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Upload shop image",
            modifier = Modifier.clickable(onClick = {
                navController.navigate("initial_screen")
            })
        )
        Button( //Button只是一個容器,裡面要放文字,就是要再加一個Text
            modifier = Modifier.fillMaxHeight(0.5f),
            //enabled = false,
            enabled = true, //如果 enabled 設為false, border, interactionSource就不會有變化
            interactionSource = interactionSourceTest,
            elevation = ButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 8.dp,
                disabledElevation = 2.dp
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(5.dp, color = borderColor),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.White,
                contentColor = Color.Red
            ),
            contentPadding = PaddingValues(4.dp, 3.dp, 2.dp, 1.dp),
            onClick = {
                var file = Uri.parse("android.resource://com.example.firebasedemo/drawable/image_lanlan")
                //var imagePath = "file://sdcard/Download/53473827_354059921901700_4038650457431651202_n.jpg"
                //var file = Uri.parse(imagePath)
                //Log.v("TEST", "file: ${file.toString()}")
                var storageRef = storage.reference.child("images/test01.jpg")

                var uploadTask = storageRef.putFile(file)
                uploadTask.addOnSuccessListener { listener ->
                    Toast.makeText(mainActivity, "Upload success", Toast.LENGTH_LONG).show()
                }.addOnFailureListener { listener ->
                    Toast.makeText(mainActivity, "Upload fail", Toast.LENGTH_LONG).show()
                }
            })
        {
            Text(text = "Upload image")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FirebaseDemoTheme {
        //LoginScreen01(rememberNavController())
    }
}