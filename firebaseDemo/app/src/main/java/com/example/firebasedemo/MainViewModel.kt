package com.example.firebasedemo

import androidx.lifecycle.ViewModel

class MainViewModel: ViewModel() {

    companion object {
        val TEXT_EMAIL = 1
        val TEXT_PASSWORD = 2
        val TEXT_NICKNAME = 3
        val TEXT_PASSWORD_CONFIRM = 4
        val TEXT_NEW_ACCOUNT_EMAIL = 5
    }
}