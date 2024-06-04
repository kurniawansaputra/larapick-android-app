package id.go.kebumenkab.larapick.ui.auth.login

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
import id.go.kebumenkab.larapick.data.response.UserResponse
import id.go.kebumenkab.larapick.databinding.ActivityLoginBinding
import id.go.kebumenkab.larapick.pref.UserPreference
import id.go.kebumenkab.larapick.ui.main.MainActivity
import id.go.kebumenkab.larapick.util.createTextWatcher
import id.go.kebumenkab.larapick.util.setLoading
import retrofit2.Call
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        checkIsLogin()
        setListener()
    }

    private fun checkIsLogin() {
        val isLogin = UserPreference.instance(this).isLogin()
        if (isLogin) {
            goToMain()
        }
    }

    private fun setListener() {
        binding.apply {
            buttonLogin.setOnClickListener {
                editEmail.addTextChangedListener(textWatcherEmail)
                editPassword.addTextChangedListener(textWatcherPassword)

                email = binding.editEmail.text.toString().trim()
                password = binding.editPassword.text.toString().trim()

                val isEmailValid = validateEmail()
                val isPasswordValid = validatePassword()

                if (isEmailValid && isPasswordValid) {
                    login()
                }
            }
        }
    }

    private fun login() {
        setLoading(this, true)
        val client = ApiConfig.getApiService().login(email, password)
        client.enqueue(object : retrofit2.Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                setLoading(this@LoginActivity, false)
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val message = response.body()?.message

                    if (status == true) {
                        val user = response.body()
                        if (user != null) {
                            UserPreference.instance(this@LoginActivity).setUser(user)
                        }

                        goToMain()
                    } else {
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                setLoading(this@LoginActivity, false)
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    private fun validateEmail(): Boolean {
        binding.apply {
            return if (editEmail.text!!.isEmpty()) {
                containerEmail.error = "Email harus diisi"
                false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(editEmail.text.toString()).matches()) {
                containerEmail.error = "Email tidak valid"
                false
            } else {
                containerEmail.error = null
                true
            }
        }
    }

    private fun validatePassword(): Boolean {
        binding.apply {
            return if (editPassword.text!!.isEmpty()) {
                containerPassword.error = "Password harus diisi"
                false
            } else {
                containerPassword.error = null
                true
            }
        }
    }

    private val textWatcherEmail: TextWatcher = createTextWatcher(::validateEmail)

    private val textWatcherPassword: TextWatcher = createTextWatcher(::validatePassword)

    companion object {
        private val TAG = LoginActivity::class.java.simpleName
    }
}