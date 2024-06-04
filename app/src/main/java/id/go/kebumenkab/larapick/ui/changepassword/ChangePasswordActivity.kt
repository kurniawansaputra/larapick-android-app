package id.go.kebumenkab.larapick.ui.changepassword

import android.content.Intent
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.go.kebumenkab.larapick.data.retrofit.ApiConfig
import id.go.kebumenkab.larapick.R
import id.go.kebumenkab.larapick.data.response.DefaultResponse
import id.go.kebumenkab.larapick.databinding.ActivityChangePasswordBinding
import id.go.kebumenkab.larapick.pref.UserPreference
import id.go.kebumenkab.larapick.ui.auth.login.LoginActivity
import id.go.kebumenkab.larapick.util.createTextWatcher
import id.go.kebumenkab.larapick.util.setLoading
import retrofit2.Call
import retrofit2.Response

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var oldPassword: String
    private lateinit var newPassword: String
    private lateinit var token: String

    private lateinit var binding: ActivityChangePasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPref()
        setToolbar()
        setListener()
    }

    private fun setPref() {
        val user = UserPreference.instance(this).getUser()
        if (user != null) {
            token = user.accessToken.toString()
        }
    }

    private fun setToolbar() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun setListener() {
        binding.apply {
            buttonUpdatePassword.setOnClickListener {
                editOldPassword.addTextChangedListener(textWatcherOldPassword)
                editNewPassword.addTextChangedListener(textWatcherNewPassword)

                oldPassword = binding.editOldPassword.text.toString().trim()
                newPassword = binding.editNewPassword.text.toString().trim()

                val isOldPasswordValid = validateOldPassword()
                val isNewPasswordValid = validateNewPassword()

                if (isOldPasswordValid && isNewPasswordValid) {
                    updatePassword()
                }
            }
        }
    }

    private fun updatePassword() {
        setLoading(this, true)
        val client = ApiConfig.getApiService().updatePassword("Bearer $token", oldPassword, newPassword)
        client.enqueue(object : retrofit2.Callback<DefaultResponse> {
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                setLoading(this@ChangePasswordActivity, false)
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val message = response.body()?.message

                    if (status == true) {
                        UserPreference.instance(this@ChangePasswordActivity).deleteAll()
                        Toast.makeText(this@ChangePasswordActivity, "Password berhasil diperbarui", Toast.LENGTH_SHORT).show()
                        goToLogin()
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                setLoading(this@ChangePasswordActivity, false)
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun validateOldPassword(): Boolean {
        binding.apply {
            return if (editOldPassword.text!!.isEmpty()) {
                containerOldPassword.error = "Password lama harus diisi"
                false
            } else {
                containerOldPassword.error = null
                true
            }
        }
    }

    private fun validateNewPassword(): Boolean {
        binding.apply {
            return if (editNewPassword.text!!.isEmpty()) {
                containerNewPassword.error = "Password baru harus diisi"
                false
            } else {
                containerNewPassword.error = null
                true
            }
        }
    }

    private val textWatcherOldPassword: TextWatcher = createTextWatcher(::validateOldPassword)

    private val textWatcherNewPassword: TextWatcher = createTextWatcher(::validateNewPassword)

    companion object {
        private val TAG = ChangePasswordActivity::class.java.simpleName
    }
}