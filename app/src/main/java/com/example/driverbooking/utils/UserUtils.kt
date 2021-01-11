package com.example.driverbooking.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import com.example.driverbooking.Common
import com.example.driverbooking.model.TokenModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

object UserUtils {
    fun updateUser(
        view: View?,
        updateData : Map<String,Any>
    ){
        FirebaseDatabase.getInstance().getReference(Common.DRIVER_INFO_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .updateChildren(updateData)
            .addOnFailureListener { e ->
                Snackbar.make(view!!,e.message!!,Snackbar.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                Snackbar.make(view!!,"Update Information Success",Snackbar.LENGTH_SHORT).show()
            }
    }

    fun updateToken(context: Context, token: String) {
        val tokenModel  = TokenModel()
        tokenModel.token = token
        FirebaseDatabase.getInstance().getReference(Common.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(tokenModel)
            .addOnFailureListener { e ->
                Toast.makeText(context,e.message,Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
            }
    }
}