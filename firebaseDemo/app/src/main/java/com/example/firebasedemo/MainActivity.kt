package com.example.firebasedemo

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
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
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebasedemo.ui.theme.*
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.*

/*
    Author: Joey yang
    Note:
    1. About join firebase into project, you can refer to udemy class.
       Tools > Firebase. (在 Firebase 的 "Dashboard" 可以看到本專案)
    2. Anonymous login official document.
       https://firebase.google.com/docs/auth/android/anonymous-auth#kotlin+ktx
    3. Mail login function:
       auth.createUserWithEmailAndPassword

    Use skill:
    1. Permission
    2. Save file to mobile.
    3. Upload text file and get text.

 */
const val SELECT_FILE_ID: Int = 10
const val SELECT_TEXT_FILE_ID: Int = 11
const val REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE = 102

class MainActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore
    private val mainViewModel by viewModels<MainViewModel>()

    private val requiredPermissions = object : ArrayList<String>() {
        init {
            add("android.permission.WRITE_EXTERNAL_STORAGE")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var ivalue = -1
        when (requestCode) {
            REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE -> {
                var i = 0
                while (i < permissions.size) {
                    if (ivalue == -1) {
                        if (grantResults[i] == PackageManager.PERMISSION_GRANTED && permissions[i] == "android.permission.WRITE_EXTERNAL_STORAGE") {
                            ivalue = 1
                            break
                        }
                    }
                    i++
                }
                if (permissions.isNotEmpty() && ivalue == 1) {
                }
            }
            else -> {
            }
        }
    }

    private fun checkPermissionsThenInitSdk() {
        val requestedPermissions: MutableList<String> = ArrayList()
        for (requiredPermission in this.requiredPermissions) {
            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestedPermissions.add(requiredPermission)
            }
        }
        if (requestedPermissions.size == 0) {
        } else {
            ActivityCompat.requestPermissions(  // 重要
                this,
                requestedPermissions.toTypedArray(), REQUEST_PERMISSIONS_WRITE_EXTERNAL_STORAGE
            )
        }
    }

    //TODO("Need to join APP check, ref: https://www.youtube.com/watch?v=Fjj4fmr2t04&feature=emb_imp_woyt")
    //TODO("Storage rules adjust: ref: udemy firebase class Ch23")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPermissionsThenInitSdk()
        setContent {
            analytics = Firebase.analytics
            auth = Firebase.auth
            //重要,若伺服器選美國,就只要用Firebase.database即可,否則要把資料庫的根網址寫入,如下
            database = Firebase.database("https://myfirebase2022-31c42-default-rtdb.asia-southeast1.firebasedatabase.app")
            storage = Firebase.storage("gs://myfirebase2022-31c42.appspot.com")
            firestore = Firebase.firestore
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
                        LoginScreen02(navController = navController, auth, this@MainActivity)
                    }
                    composable("loginMail_screen") {
                        LoginMailScreen03(navController = navController, auth, this@MainActivity, mainViewModel)
                    }
                    composable("loginNewUser_screen") {
                        //first screen
                        LoginNewUser04(navController = navController, auth, this@MainActivity)
                    }
                    composable("shop_screen_info") {
                        ShopScreen05Show(navController = navController, auth, this@MainActivity, mainViewModel, database)
                    }
                    composable("shop_image_upload") {
                        ShopScreen06ImageUpload(navController = navController, auth, this@MainActivity, mainViewModel, database, storage)
                    }
                    composable("shop_image_upload2") {
                        ShopScreen07ImageUpload2(navController = navController, auth, this@MainActivity, mainViewModel, database, storage)
                    }
                    composable("shop_txt_process") {
                        ShopTxtProcess08(navController = navController,this@MainActivity, mainViewModel)
                    }
                    composable("firebase_db") {
                        FirebaseDB09(navController = navController,this@MainActivity, mainViewModel, firestore)
                    }
                }
            }

            // 取得 Token
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    return@addOnCompleteListener
                }

                val token = task.result

                Log.d("FCMService", "Token is: $token")
                Toast.makeText(baseContext, "Token is: $token", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SELECT_FILE_ID -> if (data != null) {
                //Log.v("TEST", "Choice file: ${data.data?.path}")
                Log.v("TEST", "Choice file: ${File(data.data?.path).name}")

                data.data?.let { uriInfo ->
                    mainViewModel.setReceivePictureUri(uriInfo)
                }
//  Note: Already mark, but still need to study.
//                when(data.data?.scheme) {
//                    ContentResolver.SCHEME_CONTENT -> Log.v("TEST", "Choice file2: ${getContentFileName(data.data!!)}")
//                    else -> Log.v("TEST", "Choice file3: ${data.data?.path?.let(::File)?.name}")
//                }
            }
            SELECT_TEXT_FILE_ID -> if (data != null) {
                Log.v("TEST", "Choice file: ${data.data?.toString()}")
                if(resultCode == RESULT_OK) {
                    data.data?.let { uri ->
                        var fileContent = readTextFile(uri)
                        this@MainActivity.mainViewModel.setTxtFileContent(fileContent)
                        Log.v("TEST", "file content: $fileContent")
                    }

                }
            }
        }
    }

    private fun readTextFile(uri: Uri): String {
        var reader: BufferedReader? = null
        val builder = StringBuilder()
        try {
            reader = BufferedReader(InputStreamReader(contentResolver.openInputStream(uri)))
            var line: String? = ""
            while (reader.readLine().also { line = it } != null) {
                builder.append(line)
            }
            reader.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return builder.toString()
    }

    companion object {
        val TEXT_FILE_SAMPLE = "0,一鴻燒臘,25569779,台北市大同區南京西路232號\n" +
                "1,脆皮鴨腿飯,140\n" +
                "1,玫塊雞腿飯,120\n"
    }
}

@Composable
fun InitialScreen01(navController: NavController) {
    val interactionSourceTest = remember { MutableInteractionSource() }
    val pressState = interactionSourceTest.collectIsPressedAsState()
    val colorMy = if (pressState.value) YellowFFEB3B else Green4CAF50 //Import com.pcp.composecomponent.ui.theme.YellowFFEB3B
    val itemList = listOf("Anonymous login", "e-mail login", "show shop table info", "upload shop image", "upload shop image from mobile", "Shop text info process", "firebase test")

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
                        "upload shop image from mobile" -> navController.navigate("shop_image_upload2")
                        "Shop text info process" -> navController.navigate("shop_txt_process")
                        "firebase test" -> navController.navigate("firebase_db")
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

@Composable
fun ShopScreen07ImageUpload2(navController: NavController, auth: FirebaseAuth, mainActivity: MainActivity, mainViewModel: MainViewModel, database: FirebaseDatabase, storage: FirebaseStorage) {
    val interactionSourceTest = remember { MutableInteractionSource() }
    val pressState = interactionSourceTest.collectIsPressedAsState()
    val borderColor = if (pressState.value) YellowFFEB3B else Green4CAF50 //Import com.pcp.composecomponent.ui.theme.YellowFFEB3B

    val pictureFileUrl by mainViewModel._pictureFieUri.observeAsState(null)

    Column(verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Upload shop image from mobile",
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
                var fileIntent = Intent(Intent.ACTION_GET_CONTENT).setType("*/*")
                val mimeTypes = arrayOf("image/*", "video/*")   //我們只要Image, 但為測試多個可能,就把二個都加入,但實測上發現沒有效果
                fileIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                startActivityForResult(mainActivity, fileIntent, SELECT_FILE_ID, null)
            })
        {
            Text(text = "Upload image from mobile phone")
        }
    }

    pictureFileUrl?.let { uriInfo ->
        var fileName = mainViewModel.getFileName(uriInfo, mainActivity)
        var storageRef = storage.reference.child("images/" + fileName)

        var uploadTask = storageRef.putFile(uriInfo)
        uploadTask.addOnSuccessListener { listener ->
            Toast.makeText(mainActivity, "Upload success", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { listener ->
            Toast.makeText(mainActivity, "Upload fail", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun ShopTxtProcess08(navController: NavController, mainActivity: MainActivity, mainViewModel: MainViewModel) {
    val interactionSourceTest = remember { MutableInteractionSource() }
    val pressState = interactionSourceTest.collectIsPressedAsState()
    val borderColor = if (pressState.value) YellowFFEB3B else Green4CAF50 //Import com.pcp.composecomponent.ui.theme.YellowFFEB3B

    val fileContent by mainViewModel._textFileContent.observeAsState("")
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Button( //Button只是一個容器,裡面要放文字,就是要再加一個Text
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
                    var fileIntent = Intent(Intent.ACTION_GET_CONTENT).setType("text/plain")
                    //val mimeTypes =
                    //    arrayOf("image/*", "video/*")   //我們只要Image, 但為測試多個可能,就把二個都加入,但實測上發現沒有效果
                    //fileIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                    startActivityForResult(mainActivity, fileIntent, SELECT_TEXT_FILE_ID, null)
                })
            {
                Text(text = "Upload txt file")
            }

            Button( //Button只是一個容器,裡面要放文字,就是要再加一個Text
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
                    var inputStream: InputStream = MainActivity.TEXT_FILE_SAMPLE.byteInputStream()
                        //val storeDirectory = mainActivity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) // DCIM folder
                    //val storeDirectory = mainActivity.getExternalFilesDir(Environment.DIRECTORY_DCIM)
                    val storeDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

                        val outputFile = File(storeDirectory, "testing-again2.txt")
                        inputStream.use { input ->
                            val outputStream = FileOutputStream(outputFile)
                            outputStream.use { output ->
                                val buffer = ByteArray(4 * 1024) // buffer size
                                while (true) {
                                    val byteCount = input.read(buffer)
                                    if (byteCount < 0) break
                                    output.write(buffer, 0, byteCount)
                                }
                                output.flush()
                                output.close()
                                Log.v("TEST", "download file success")
                            }
                        }
                })
            {
                Text(text = "Download txt file")
            }
        }
        Text(
            text = fileContent,
            modifier = Modifier.clickable(onClick = {
                navController.navigate("initial_screen")
            })
        )
    }
}

@Composable
fun FirebaseDB09(navController: NavController, mainActivity: MainActivity, mainViewModel: MainViewModel, firestore: FirebaseFirestore) {
    // 集合 (Collection) 可以被視為目錄
    // 文件 (Document) 可視為目錄下的檔案,但是目錄下,可以放檔案,也可以再放一個目錄,所以Document裡也可以再生一些Collection
    // 這樣就跟檔案系統有點像

    val store1 = hashMapOf(
        "addr" to "台北市大同區南京西路232號",
        "id" to "0",
        "name" to "一鴻燒臘",
        "tel" to "25569779"
    )

    val store2 = hashMapOf(
        "addr" to "台北市長安西路256號",
        "id" to "1",
        "name" to "金仙蝦捲飯",
        "tel" to "25595759"
    )

    val interactionSourceTest = remember { MutableInteractionSource() }
    val pressState = interactionSourceTest.collectIsPressedAsState()
    val borderColor = if (pressState.value) YellowFFEB3B else Green4CAF50 //Import com.pcp.composecomponent.ui.theme.YellowFFEB3B

    val fileContent by mainViewModel._textFileContent.observeAsState("")
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Button( //Button只是一個容器,裡面要放文字,就是要再加一個Text
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
                val bentoSite = firestore.collection("bento")
                bentoSite.document("store")
                    //.add(store)
                    .set(store1)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TEST", "DocumentSnapshot added with ID: $documentReference")
                        Toast.makeText(mainActivity, "DocumentSnapshot added with ID: ${documentReference}", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("TEST", "Error adding document", )
                        Toast.makeText(mainActivity, "Error adding document: $e" , Toast.LENGTH_LONG).show()
                    }

                bentoSite.document("store2")
                    //.add(store)
                    .set(store2)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TEST", "DocumentSnapshot added with ID2: $documentReference")
                        Toast.makeText(mainActivity, "DocumentSnapshot added with ID: ${documentReference}", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("TEST", "Error adding document", )
                        Toast.makeText(mainActivity, "Error adding document2: $e" , Toast.LENGTH_LONG).show()
                    }
            })
        {
            Text(text = "Add data to DB")
        }
        Button( //Button只是一個容器,裡面要放文字,就是要再加一個Text
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
                val bentoSite = firestore.collection("bento")
                bentoSite
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            Log.d("Test", "check info: ${document.id} => ${document.data}")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("Test", "Error getting documents: ", exception)
                    }

                for(i in 1..5) {
                    val randoms = (100000..999999).random()
                    Log.v("TEST", "random value $randoms")
                }
            })
        {
            Text(text = "Get data from DB")
        }

        Button( //Button只是一個容器,裡面要放文字,就是要再加一個Text
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
                val bentoSite = firestore.collection("bento").document("store3").collection("storeInfo")
                bentoSite.document("hotel1")
                    .set(store1)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TEST", "DocumentSnapshot added with ID: $documentReference")
                        Toast.makeText(mainActivity, "DocumentSnapshot added with ID: ${documentReference}", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("TEST", "Error adding document", )
                        Toast.makeText(mainActivity, "Error adding document: $e" , Toast.LENGTH_LONG).show()
                    }
                bentoSite.document("hotel2")
                    .set(store2)
                    .addOnSuccessListener { documentReference ->
                        Log.d("TEST", "DocumentSnapshot added with ID: $documentReference")
                        Toast.makeText(mainActivity, "DocumentSnapshot added with ID: ${documentReference}", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("TEST", "Error adding document", )
                        Toast.makeText(mainActivity, "Error adding document: $e" , Toast.LENGTH_LONG).show()
                    }
            })
        {
            Text(text = "Deep study")
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