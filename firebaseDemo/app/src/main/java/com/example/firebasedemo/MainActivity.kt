package com.example.firebasedemo

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebasedemo.ui.theme.FirebaseDemoTheme
import com.example.firebasedemo.ui.theme.Green4CAF50
import com.example.firebasedemo.ui.theme.YellowFFEB3B
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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
    private val mainViewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            analytics = Firebase.analytics
            auth = Firebase.auth

            FirebaseDemoTheme {
                val navController = rememberNavController() //Navigation Step2

                NavHost(
                    navController = navController,
                    startDestination = "login_screen"
                ) {
                    composable("login_screen") {
                        //first screen
                        LoginScreen01(navController = navController, auth, this@MainActivity)
                    }
                    composable("loginMail_screen") {
                        //first screen
                        LoginMailScreen02(navController = navController, auth, this@MainActivity, mainViewModel)
                    }
                    composable("loginNewUser_screen") {
                        //first screen
                        LoginNewUser03(navController = navController, auth, this@MainActivity)
                    }
                    composable("shop_screen") {
                        //second screen
                        ShopScreen04(navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen01(navController: NavController, auth: FirebaseAuth, activity: MainActivity) {
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
fun LoginMailScreen02(navController: NavController, auth: FirebaseAuth, activity: MainActivity, mainViewModel: MainViewModel) {
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
fun LoginNewUser03(navController: NavController, auth: FirebaseAuth, activity: MainActivity) {
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
fun ShopScreen04(navController: NavController) {
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


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FirebaseDemoTheme {
        //LoginScreen01(rememberNavController())
    }
}