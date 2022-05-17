package com.example.firebasedemo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.firebasedemo.ui.theme.FirebaseDemoTheme
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/*
    Author: Joey yang
    Note:
    1. About join firebase into project, you can refer to udemy class.
       Tools > Firebase. (在 Firebase 的 "Dashboard" 可以看到本專案)
    2. Anonymous login official document.
       https://firebase.google.com/docs/auth/android/anonymous-auth#kotlin+ktx
 */
class MainActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics
    private lateinit var auth: FirebaseAuth

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
                    composable("shop_screen") {
                        //second screen
                        ShopScreen02(navController = navController)
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
            navController.navigate("shop_screen")
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

    //TODO: Need to join viewModel and remember State to fit MVVM style
}

@Composable
fun ShopScreen02(navController: NavController) {
    Text(text = "Second screen",
        modifier = Modifier.clickable(onClick = {
            navController.navigate("login_screen")
        })
    )
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FirebaseDemoTheme {
        //LoginScreen01(rememberNavController())
    }
}