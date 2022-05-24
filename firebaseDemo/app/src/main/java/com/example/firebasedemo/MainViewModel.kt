package com.example.firebasedemo

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File

class MainViewModel: ViewModel() {
    val _loginUser = MutableLiveData<FirebaseUser>()
       val loginUser: LiveData<FirebaseUser> = _loginUser

    val _shopInfo = MutableLiveData<MutableList<Shop>>()
    val shopInfo: LiveData<MutableList<Shop>> = _shopInfo

    val _pictureFieUri = MutableLiveData<Uri>()
    val pictureFieUri: LiveData<Uri> = _pictureFieUri

    fun updateUserStatus(auth: FirebaseAuth) {
        _loginUser.value = auth.currentUser
    }

    fun authStateListener(auth: FirebaseAuth) {
        auth.addAuthStateListener {
            updateUserStatus(auth)
        }
    }

    fun getShopList(database: FirebaseDatabase) {
        Log.v("TEST", "shop list 001")
        var shopList: MutableList<Shop> = ArrayList()

        database.getReference("bento/store")
            .addListenerForSingleValueEvent(object : ValueEventListener {    //重要
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.v("TEST", "shop list 002")
                    for (snap in snapshot.children) {
                        val name = snap.child("name").value.toString()
                        val address = snap.child("addr").value.toString()
                        val telephone = snap.child("tel").value.toString()
                        val key = snap.key

                        val shop = key?.let { Shop(it.toInt(), address, name, telephone) }
                        shop?.let {
                            shopList.add(it)
                        }
                    }
                    _shopInfo.value = shopList
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.v("TEST", "shop list 003")
                }
            })
    }

    fun setReceivePictureUri(uriInfo: Uri) {
        _pictureFieUri.value = uriInfo
    }

    fun getFileName(uriInfo: Uri, activity: MainActivity): String {
        when(uriInfo.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                Log.v("TEST", "Choice file2: ${getContentFileName(uriInfo, activity)}")
                getContentFileName(uriInfo, activity)?.let { stringInfo ->
                    return stringInfo
                }
                return ""
            }
            else -> {
                Log.v("TEST", "Choice file3: ${uriInfo.path?.let(::File)?.name}")
                uriInfo.path?.let(::File)?.let{ fileInfo ->
                    return fileInfo.name
                }
                return ""
            }
        }
    }

    //Important: Need to learning.
    private fun getContentFileName(uri: Uri, activity: MainActivity): String? = runCatching {
        activity.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            cursor.moveToFirst()
            return@use cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME).let(cursor::getString)
        }
    }.getOrNull()

    companion object {
        val TEXT_EMAIL = 1
        val TEXT_PASSWORD = 2
        val TEXT_NICKNAME = 3
        val TEXT_PASSWORD_CONFIRM = 4
        val TEXT_NEW_ACCOUNT_EMAIL = 5
    }
}