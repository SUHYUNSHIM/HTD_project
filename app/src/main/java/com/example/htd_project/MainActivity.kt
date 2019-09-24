package com.example.htd_project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.kakao.network.ApiErrorCode
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.LogoutResponseCallback
import com.kakao.usermgmt.callback.UnLinkResponseCallback

class MainActivity : AppCompatActivity() {


    internal var strEmail:String?= null
    internal var strAgeRange:String? = null
    internal var strGender :String?= null
    internal var strBirthday :String? = null

    internal var strNickname: String? = null
    internal var strProfile: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvNickname = findViewById<TextView>(R.id.tvNickname)
        val ivProfile = findViewById<ImageView>(R.id.ivProfile)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnSignout = findViewById<Button>(R.id.btnSignout)
        val tvEmail = findViewById<TextView>(R.id.tvEmail);
        val tvAgeRange = findViewById<TextView>(R.id.tvAgeRange);
        val tvGender = findViewById<TextView>(R.id.tvGender);
        val tvBirthday = findViewById<TextView>(R.id.tvBirthday);


        val intent = intent
        strNickname = intent.getStringExtra("name")
        strProfile = intent.getStringExtra("profile")
        //이메일, 나잇대, 성별, 생일을 intent에서 가져와서 각 String에 저장함
        strEmail = intent.getStringExtra("email")
        strAgeRange = intent.getStringExtra("ageRange")
        strGender = intent.getStringExtra("gender")
        strBirthday = intent.getStringExtra("birthday")


        tvNickname.text = strNickname
        Glide.with(this).load(strProfile).into(ivProfile)
        // ImageView에 strProfile의 URL에 해당하는 이미지를 표시해준다.

        //받아온 정보를 각 TextView에 표시함
        tvEmail.text = strEmail
        tvAgeRange.text = strAgeRange
        tvGender.text = strGender
        tvBirthday.text =strBirthday



        btnLogout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                Toast.makeText(applicationContext, "정상적으로 로그아웃되었습니다.", Toast.LENGTH_SHORT).show()

                UserManagement.getInstance().requestLogout(object : LogoutResponseCallback() {
                    override fun onCompleteLogout() {
                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                    }
                })
            }
        })

        btnSignout.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage("탈퇴하시겠습니까?")
                    .setPositiveButton("네") { dialog, which ->
                        UserManagement.getInstance()
                            .requestUnlink(object : UnLinkResponseCallback() {
                                override fun onFailure(errorResult: ErrorResult?) {
                                    val result = errorResult!!.errorCode

                                    if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                                        Toast.makeText(
                                            applicationContext,
                                            "네트워크 연결이 불안정합니다. 다시 시도해 주세요.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            applicationContext,
                                            "회원탈퇴에 실패했습니다. 다시 시도해 주세요.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onSessionClosed(errorResult: ErrorResult) {
                                    Toast.makeText(
                                        applicationContext,
                                        "로그인 세션이 닫혔습니다. 다시 로그인해 주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent =
                                        Intent(this@MainActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                                override fun onNotSignedUp() {
                                    Toast.makeText(
                                        applicationContext,
                                        "가입되지 않은 계정입니다. 다시 로그인해 주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent =
                                        Intent(this@MainActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }

                                override fun onSuccess(result: Long?) {
                                    Toast.makeText(
                                        applicationContext,
                                        "회원탈퇴에 성공했습니다.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent =
                                        Intent(this@MainActivity, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            })

                        dialog.dismiss()
                    }
                    .setNegativeButton("아니요") { dialog, which -> dialog.dismiss() }.show()
            }
        })
    }
}

