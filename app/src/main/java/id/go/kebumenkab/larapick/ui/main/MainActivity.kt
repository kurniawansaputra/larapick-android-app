package id.go.kebumenkab.larapick.ui.main

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.go.kebumenkab.larapick.data.retrofit.ApiConfig
import id.go.kebumenkab.larapick.R
import id.go.kebumenkab.larapick.data.response.DefaultResponse
import id.go.kebumenkab.larapick.data.response.PickupLog
import id.go.kebumenkab.larapick.data.response.PickupLogResponse
import id.go.kebumenkab.larapick.data.response.UserResponse
import id.go.kebumenkab.larapick.databinding.ActivityMainBinding
import id.go.kebumenkab.larapick.databinding.LayoutDialogQrCodeBinding
import id.go.kebumenkab.larapick.pref.UserPreference
import id.go.kebumenkab.larapick.ui.auth.login.LoginActivity
import id.go.kebumenkab.larapick.ui.changepassword.ChangePasswordActivity
import id.go.kebumenkab.larapick.ui.otherpickup.OtherPickupActivity
import id.go.kebumenkab.larapick.ui.pickuplogs.PickupLogsActivity
import id.go.kebumenkab.larapick.ui.pickuplogs.PickupLogsAdapter
import id.go.kebumenkab.larapick.ui.profile.ProfileActivity
import id.go.kebumenkab.larapick.ui.profilestudent.ProfileStudentActivity
import id.go.kebumenkab.larapick.ui.scan.ScanActivity
import id.go.kebumenkab.larapick.util.loadImage
import id.go.kebumenkab.larapick.util.setLoading
import retrofit2.Call
import retrofit2.Response
import java.time.LocalDate
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private var guardianId by Delegates.notNull<Int>()
    private lateinit var guardianName: String
    private lateinit var studentImage: String
    private lateinit var studentName: String
    private lateinit var studentClass: String
    private var studentId by Delegates.notNull<Int>()
    private lateinit var studentStatus: String
    private lateinit var guardianQrCode: String
    private var isAdmin by Delegates.notNull<Boolean>()
    private lateinit var token: String

    private lateinit var bitmap: Bitmap

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPref()
        initUi()
        optionMenu()
        setListener()
        setDetail()
    }

    private fun setPref() {
        val user = UserPreference.instance(this).getUser()
        if (user != null) {
            guardianName = user.data?.name.toString()
            studentImage = user.data?.student?.image.toString()
            studentName = user.data?.student?.name.toString()
            studentClass = user.data?.student?.grade?.name.toString()
            guardianQrCode = user.data?.qrCode.toString()
            isAdmin = user.data?.isAdmin ?: false
            token = user.accessToken.toString()
            studentId = user.data?.student?.id ?: 0
            guardianId = user.data?.id ?: 0
            studentStatus = user.data?.student?.status.orEmpty()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initUi() {
        binding.apply {
            if (isAdmin) {
                containerAdmin.visibility = View.VISIBLE
                containerUser.visibility = View.GONE

                swipeRefresh.isEnabled = false
            } else {
                containerAdmin.visibility = View.GONE
                containerUser.visibility = View.VISIBLE

                swipeRefresh.isEnabled = true
                getPickupLogs()
                swipeRefresh()
            }
        }
    }

    private fun setListener() {
        binding.apply {
            if (studentStatus == "active") {
                buttonShowQRCode.setOnClickListener {
                    dialogShowQrCode()
                }
            } else {
                buttonShowQRCode.isEnabled = false
            }

            buttonProfileStudent.setOnClickListener {
                val intent = Intent(this@MainActivity, ProfileStudentActivity::class.java)
                startActivity(intent)
            }

            buttonScanQrCode.setOnClickListener {
                val intent = Intent(this@MainActivity, ScanActivity::class.java)
                startActivity(intent)
            }

            buttonFormPickup.setOnClickListener {
                val intent = Intent(this@MainActivity, OtherPickupActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun optionMenu() {
        binding.apply {
            if (isAdmin) {
                topAppBar.menu.findItem(R.id.menuPickupLogs).isVisible = false
            }

            topAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menuLogout -> {
                        logout()
                        true
                    }
                    R.id.menuChangePassword -> {
                        val intent = Intent(this@MainActivity, ChangePasswordActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.menuPickupLogs -> {
                        val intent = Intent(this@MainActivity, PickupLogsActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.menuProfile -> {
                        val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDetail() {
        binding.apply {
            textName.text = "Hai, $guardianName!"
            textStudentName.text = studentName
            textStudentGrade.text = "Kelas.$studentClass"

            loadImage(this@MainActivity, ivStudentImage, studentImage, studentName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("UNCHECKED_CAST")
    private fun getPickupLogs() {
        val currentDate = LocalDate.now()
        val year = currentDate.year
        val month = currentDate.monthValue

        Log.d(TAG, "getPickupLogs: $year $month")

        setLoading(true)
        val client = ApiConfig.getApiService().getPickupLogs("Bearer $token", studentId,  year, month, "on_progress")
        client.enqueue(object : retrofit2.Callback<PickupLogResponse> {
            override fun onResponse(call: Call<PickupLogResponse>, response: Response<PickupLogResponse>) {
                setLoading(false)
                binding.swipeRefresh.isRefreshing = false
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val data = response.body()?.data

                    if (status == true) {
                        val pickupLogsAdapter = PickupLogsAdapter(data as ArrayList<PickupLog>)
                        binding.rvLog.adapter = pickupLogsAdapter
                        binding.rvLog.setHasFixedSize(true)

                        if (pickupLogsAdapter.itemCount == 0) {
                            setRecycleView(false)
                        } else {
                            setRecycleView(true)
                        }

                        pickupLogsAdapter.setOnItemClickCallback(object : PickupLogsAdapter.OnItemClickCallback {
                            override fun onItemClicked(data: PickupLog) {
                                updatePickupStatus(data)
                            }
                        })
                    }
                }
            }

            override fun onFailure(call: Call<PickupLogResponse>, t: Throwable) {
                setLoading(false)
                binding.swipeRefresh.isRefreshing = false
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun updatePickupStatus(pickupLog: PickupLog) {
        setLoading(this, true)
        val client = ApiConfig.getApiService().updatePickupStatus("Bearer $token", pickupLog.id!!, guardianId, "done")
        client.enqueue(object : retrofit2.Callback<DefaultResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<DefaultResponse>, response: Response<DefaultResponse>) {
                setLoading(this@MainActivity, false)
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val message = response.body()?.message

                    if (status == true) {
                        getPickupLogs()
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<DefaultResponse>, t: Throwable) {
                setLoading(this@MainActivity, false)
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun dialogShowQrCode() {
        val binding: LayoutDialogQrCodeBinding = LayoutDialogQrCodeBinding.inflate(
            LayoutInflater.from(this))
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setView(binding.root)
        val dialog: AlertDialog = builder.create()
        binding.apply {
            val qrgEncoder = QRGEncoder(guardianQrCode, null, QRGContents.Type.TEXT, 450)

            try {
                // Getting QR-Code as Bitmap
                bitmap = qrgEncoder.getBitmap(0)
                // Setting Bitmap to ImageView
                ivQrCode.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.v(TAG, e.toString())
            }

            ivBack.setOnClickListener {
                dialog.dismiss()
            }
        }
        dialog.setCancelable(true)
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun logout() {
        setLoading(this, true)
        val client = ApiConfig.getApiService().logout("Bearer $token")
        client.enqueue(object : retrofit2.Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                setLoading(this@MainActivity, false)
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val message = response.body()?.message

                    if (status == true) {
                        UserPreference.instance(this@MainActivity).deleteAll()
                        goToLogin()
                    } else {
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.d(TAG, "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                setLoading(this@MainActivity, false)
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun setRecycleView(condition: Boolean) {
        binding.apply {
            if (condition) {
                rvLog.visibility = View.VISIBLE
                labelEmptyData.visibility = View.GONE
            } else {
                rvLog.visibility = View.GONE
                labelEmptyData.visibility = View.VISIBLE
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun swipeRefresh() {
        binding.apply {
            swipeRefresh.setOnRefreshListener {
                getPickupLogs()
                labelEmptyData.visibility = View.GONE
                rvLog.visibility = View.GONE
            }
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}