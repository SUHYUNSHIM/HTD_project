package com.example.htd_project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity

import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ApiErrorCode
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.callback.UnLinkResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.OptionalBoolean
import com.kakao.util.exception.KakaoException

class LoginActivity: AppCompatActivity() {

    private var sessionCallback: SessionCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionCallback = SessionCallback()
        Session.getCurrentSession().addCallback(sessionCallback)
        Session.getCurrentSession().checkAndImplicitOpen()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Session.getCurrentSession().removeCallback(sessionCallback)
    }

    private inner class SessionCallback : ISessionCallback {
        override fun onSessionOpened() {
            UserManagement.getInstance().me(object : MeV2ResponseCallback() {
                override fun onFailure(errorResult: ErrorResult?) {
                    val result = errorResult!!.errorCode

                    if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(
                            applicationContext,
                            "네트워크 연결이 불안정합니다. 다시 시도해 주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "로그인 도중 오류가 발생했습니다: " + errorResult.errorMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onSessionClosed(errorResult: ErrorResult) {
                    Toast.makeText(
                        applicationContext,
                        "세션이 닫혔습니다. 다시 시도해 주세요: " + errorResult.errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onSuccess(result: MeV2Response) {
                    var needsScopeAutority = "" // 정보 제공이 허용되지 않은 항목의 이름을 저장하는 변수

                    // 이메일, 성별, 연령대, 생일 정보를 제공하는 것에 동의했는지 체크
                    if (result.kakaoAccount.needsScopeAccountEmail()) {
                        needsScopeAutority = needsScopeAutority + "이메일"
                    }
                    if (result.kakaoAccount.needsScopeGender()) {
                        needsScopeAutority = "$needsScopeAutority, 성별"
                    }
                    if (result.kakaoAccount.needsScopeAgeRange()) {
                        needsScopeAutority = "$needsScopeAutority, 연령대"
                    }
                    if (result.kakaoAccount.needsScopeBirthday()) {
                        needsScopeAutority = "$needsScopeAutority, 생일"
                    }

                    if (needsScopeAutority.length != 0) { // 정보 제공이 허용되지 않은 항목이 있다면 -> 허용되지 않은 항목을 안내하고 회원탈퇴 처리
                        if (needsScopeAutority[0] == ',') {
                            needsScopeAutority = needsScopeAutority.substring(2)
                        }
                        Toast.makeText(
                            applicationContext,
                            needsScopeAutority + "에 대한 권한이 허용되지 않았습니다. 개인정보 제공에 동의해주세요.",
                            Toast.LENGTH_SHORT
                        ).show() // 개인정보 제공에 동의해달라는 Toast 메세지 띄움

                        // 회원탈퇴 처리
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
                                            "오류가 발생했습니다. 다시 시도해 주세요.",
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
                                }

                                override fun onNotSignedUp() {
                                    Toast.makeText(
                                        applicationContext,
                                        "가입되지 않은 계정입니다. 다시 로그인해 주세요.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                override fun onSuccess(result: Long?) {}
                            })
                    } else { // 모든 항목에 동의했다면 -> 유저 정보를 가져와서 MainActivity에 전달하고 MainActivity 실행.
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.putExtra("name", result.nickname)
                        intent.putExtra("profile", result.profileImagePath)

                        if (result.kakaoAccount.hasEmail() == OptionalBoolean.TRUE)
                            intent.putExtra("email", result.kakaoAccount.email)
                        else
                            intent.putExtra("email", "none")
                        if (result.kakaoAccount.hasAgeRange() == OptionalBoolean.TRUE)
                            intent.putExtra("ageRange", result.kakaoAccount.ageRange.value)
                        else
                            intent.putExtra("ageRange", "none")
                        if (result.kakaoAccount.hasGender() == OptionalBoolean.TRUE)
                            intent.putExtra("gender", result.kakaoAccount.gender!!.value)
                        else
                            intent.putExtra("gender", "none")

                        if (result.kakaoAccount.hasBirthday() == OptionalBoolean.TRUE)
                            intent.putExtra("birthday", result.kakaoAccount.birthday)
                        else
                            intent.putExtra("birthday", "none")

                        startActivity(intent)
                        finish()
                    }
                }
            })
        }

        override fun onSessionOpenFailed(e: KakaoException) {
            Toast.makeText(
                applicationContext,
                "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: $e",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

