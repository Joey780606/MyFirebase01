package com.example.firebasedemo

import android.os.Bundle
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
import com.google.firebase.ktx.Firebase

/*
    Author: Joey yang
    Note:
    1. About join firebase into project, you can refer to udemy class.
 */
class MainActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            analytics = Firebase.analytics
            FirebaseDemoTheme {
                val navController = rememberNavController() //Navigation Step2

                NavHost(
                    navController = navController,
                    startDestination = "login_screen"
                ) {
                    composable("login_screen") {
                        //first screen
                        LoginScreen01(navController = navController)
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
fun LoginScreen01(navController: NavController) {
    Text(text = "First screen",
        modifier = Modifier.clickable(onClick = {
            navController.navigate("shop_screen")
        })
    )
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
        LoginScreen01(rememberNavController())
    }
}