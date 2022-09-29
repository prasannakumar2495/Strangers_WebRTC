package com.prasannakumar.strangerswebrtc.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.prasannakumar.strangerswebrtc.databinding.ActivityConnectingBinding

class Connecting : AppCompatActivity() {
    private lateinit var binding: ActivityConnectingBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var circularImage: ShapeableImageView
    private var isOkay = false
    private var userName: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectingBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        circularImage = binding.profileImage
        mAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        val currentUser = mAuth.currentUser

        Glide.with(this).load(currentUser?.photoUrl).into(circularImage)

        userName = mAuth.uid.toString()

        db.reference.child("users")
            .orderByChild("status").equalTo("0")
            .limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.childrenCount > 0) {
                        //Room Available
                        isOkay = true

                        for (childSnap in snapshot.children) {
                            db.reference.child("users")
                                .child(childSnap.key!!)
                                .child("incoming")
                                .setValue(userName)
                            db.reference.child("users")
                                .child(childSnap.key!!)
                                .child("status")
                                .setValue("1")

                            val incoming =
                                childSnap.child("incoming").value.toString()
                            val createdBy =
                                childSnap.child("createdBy").value.toString()
                            val isAvailable: Boolean =
                                childSnap.child("isAvailable").value as Boolean
                            val intent =
                                Intent(this@Connecting, Call::class.java)
                            intent.putExtra("userName", userName)
                            intent.putExtra("incoming", incoming)
                            intent.putExtra("createdBy", createdBy)
                            intent.putExtra("isAvailable", isAvailable)
                            startActivity(intent)

                        }
                        Log.d("Room Creation", "Room Available")
                    } else {
                        //Room Not Available
                        val room = HashMap<String, Any>()
                        room["incoming"] = userName!!
                        room["createdBy"] = userName
                        room["isAvailable"] = true
                        room["status"] = "0"

                        db.reference.child("users")
                            .child(userName)
                            .setValue(room)
                            .addOnSuccessListener {
                                db.reference.child("users")
                                    .child(userName)
                                    .addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.child("status").exists()) {
                                                if (snapshot.child("status").value == "1") {
                                                    if (isOkay) {
                                                        return
                                                    }
                                                    isOkay = true

                                                    val incoming =
                                                        snapshot.child("incoming").value.toString()
                                                    val createdBy =
                                                        snapshot.child("createdBy").value.toString()
                                                    val isAvailable: Boolean =
                                                        snapshot.child("isAvailable").value as Boolean
                                                    val intent =
                                                        Intent(this@Connecting, Call::class.java)
                                                    intent.putExtra("userName", userName)
                                                    intent.putExtra("incoming", incoming)
                                                    intent.putExtra("createdBy", createdBy)
                                                    intent.putExtra("isAvailable", isAvailable)
                                                    startActivity(intent)
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }
                                    })
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

    }
}