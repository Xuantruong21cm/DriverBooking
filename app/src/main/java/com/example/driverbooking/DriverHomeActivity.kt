package com.example.driverbooking

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.example.driverbooking.utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.nav_header_main.view.*
import java.lang.StringBuilder
import java.net.URI
import java.util.*
import kotlin.collections.HashMap

class DriverHomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navController: NavController
    private lateinit var img_avatar: ImageView
    private var imageURI: Uri? = null
    private lateinit var waitingDialog: AlertDialog
    private lateinit var storageReference: StorageReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_home)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        init()
    }

    private fun init() {
        storageReference = FirebaseStorage.getInstance().getReference()

        waitingDialog = AlertDialog.Builder(this@DriverHomeActivity)
            .setMessage("Waiting...")
            .setCancelable(false).create()

        navView.setNavigationItemSelectedListener {
            if (it.itemId == R.id.nav_sign_out) {
                val builder = AlertDialog.Builder(this@DriverHomeActivity)
                builder.setTitle("Sign Out")
                    .setMessage("Do you really want to sign out")
                    .setNegativeButton("CANCEL",
                        { dialogInterface, i -> dialogInterface.dismiss() })
                    .setPositiveButton("SIGN OUT") { dialogInterface, i ->
                        FirebaseAuth.getInstance().signOut()
                        val intent =
                            Intent(this@DriverHomeActivity, SplashScreenActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }.setCancelable(false)
                val dialog = builder.create()
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(resources.getColor(android.R.color.black))
                }
                dialog.show()
            }
            true
        }
        val headerView = navView.getHeaderView(0)
        var txt_name = headerView.txt_name
        var txt_phone = headerView.txt_phone
        var txt_star = headerView.txt_star
        img_avatar = headerView.img_avatar

        txt_name.text = Common.buildWelcomeMessage()
        txt_phone.text = Common.currentUser!!.numberPhone
        txt_star.text = StringBuilder().append(Common.currentUser!!.rating)

        if (Common.currentUser != null && Common.currentUser!!.avatar != null
            && !TextUtils.isEmpty(Common.currentUser!!.avatar)
        ) {
            Glide.with(this@DriverHomeActivity).load(Common.currentUser!!.avatar)
                .into(img_avatar)
        }

        img_avatar.setOnClickListener {
            var intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.data != null) {
                imageURI = data.data
                img_avatar.setImageURI(imageURI)
                showDialogUpload()
            }
        }
    }

    private fun showDialogUpload() {
        val builder = AlertDialog.Builder(this@DriverHomeActivity)
        builder.setTitle("Change Avatar")
            .setMessage("Do you really want to change Avatar")
            .setNegativeButton("CANCEL", { dialogInterface, i -> dialogInterface.dismiss() })
            .setPositiveButton("CHANGE") { dialogInterface, i ->

                if (imageURI != null){
                    waitingDialog.show()
                    val avatarFolder = storageReference.child("avatar/"+FirebaseAuth.getInstance().currentUser!!.uid)
                    avatarFolder.putFile(imageURI!!)
                        .addOnFailureListener{e ->
                            Snackbar.make(drawerLayout,e.message!!,Snackbar.LENGTH_LONG).show()
                            waitingDialog.dismiss()
                        }.addOnCompleteListener{task ->
                            if (task.isSuccessful){
                                avatarFolder.downloadUrl.addOnSuccessListener { uri ->
                                    var update_data = HashMap<String,Any>()
                                    update_data.put("avatar",uri.toString())
                                    UserUtils.updateUser(drawerLayout,update_data)
                                }
                            }
                            waitingDialog.dismiss()
                        }
                        .addOnProgressListener {
                            task ->
                            val progress = (100*task.bytesTransferred / task.totalByteCount)
                            waitingDialog.setMessage(StringBuilder("Uploading: ").append(progress).append("%"))

                        }
                }

            }.setCancelable(false)
        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(this@DriverHomeActivity,android.R.color.holo_red_dark))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(this@DriverHomeActivity,android.R.color.black))
        }
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.driver_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    companion object {
        val PICK_IMAGE_REQUEST = 7272
    }
}