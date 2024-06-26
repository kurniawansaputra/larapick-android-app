package id.go.kebumenkab.larapick.ui.resultscan

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import id.go.kebumenkab.larapick.R
import id.go.kebumenkab.larapick.data.response.DefaultResponse
import id.go.kebumenkab.larapick.data.response.UserResponse
import id.go.kebumenkab.larapick.data.retrofit.ApiConfig
import id.go.kebumenkab.larapick.databinding.ActivityResultScanBinding
import id.go.kebumenkab.larapick.pref.UserPreference
import id.go.kebumenkab.larapick.ui.imageview.ImageViewActivity
import id.go.kebumenkab.larapick.ui.main.MainActivity
import id.go.kebumenkab.larapick.util.loadImage
import id.go.kebumenkab.larapick.util.reduceFileImage
import id.go.kebumenkab.larapick.util.setLoading
import id.go.kebumenkab.larapick.util.uriToFile
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.await
import kotlin.properties.Delegates

class ResultScanActivity : AppCompatActivity() {
    private lateinit var qrCode: String
    private lateinit var token: String
    private lateinit var studentName: String
    private lateinit var studentGrade: String
    private lateinit var studentImage: String
    private lateinit var guardianName: String
    private lateinit var guardianGender: String
    private lateinit var guardianJob: String
    private lateinit var guardianIdNumber: String
    private lateinit var guardianRelationship: String
    private lateinit var guardianAddress: String
    private lateinit var guardianPhone: String
    private lateinit var guardianImage: String
    private var guardianId by Delegates.notNull<Int>()
    private var studentId by Delegates.notNull<Int>()
    private var adminId by Delegates.notNull<Int>()
    private var currentImageUri: Uri? = null
    private var note: String? = null

    private lateinit var binding: ActivityResultScanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityResultScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPref()
        setToolbar()
        getStudentByQrCode()
    }

    private fun setPref() {
        val user = UserPreference.instance(this).getUser()
        if (user != null) {
            token = user.accessToken.toString()
            adminId = user.data?.id!!
        }
    }

    private fun setToolbar() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun getStudentByQrCode() {
        qrCode = intent.getStringExtra("qrCode").toString()

        binding.apply {
            setLoading(true)
            val client = ApiConfig.getApiService().getGuardianByQrCode("Bearer $token", qrCode)
            client.enqueue(object : retrofit2.Callback<UserResponse> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    setLoading(false)

                    if (response.isSuccessful) {
                        val status = response.body()?.status
                        val data = response.body()?.data

                        if (status == true) {
                            containerStudentFound.visibility = View.VISIBLE
                            constraintLayout.visibility = View.VISIBLE
                            labelStudentNotFound.visibility = View.GONE

                            studentName = data?.student?.name.toString()
                            studentGrade = data?.student?.grade?.name.toString()
                            studentImage = data?.student?.image.toString()

                            guardianName = data?.name.toString()
                            guardianGender = data?.gender.toString()
                            guardianJob = data?.job.toString()
                            guardianIdNumber = data?.idNumber.toString()
                            guardianRelationship = data?.relationship.toString()
                            guardianAddress = data?.address.toString()
                            guardianPhone = data?.phone.toString()
                            guardianImage = data?.image.toString()

                            studentId = data?.student?.id!!
                            guardianId = data.id!!

                            textStudentName.text = studentName
                            textStudentGrade.text = "Kelas.$studentGrade"
                            loadImage(this@ResultScanActivity, ivStudentImage, studentImage, studentName)

                            if (guardianGender == "male") {
                                textGuardianGender.text = "Laki-laki"
                            } else {
                                textGuardianGender.text = "Perempuan"
                            }

                            textGuardianName.text = guardianName
                            textGuardianJob.text = guardianJob
                            textGuardianIdNumber.text = guardianIdNumber
                            textGuardianRelationship.text = guardianRelationship
                            textGuardianAddress.text = guardianAddress
                            textGuardianPhone.text = guardianPhone
                            loadImage(this@ResultScanActivity, ivGuardianImage, guardianImage, guardianName)

                            setListener()
                        } else {
                            containerStudentFound.visibility = View.GONE
                            constraintLayout.visibility = View.GONE
                            labelStudentNotFound.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    setLoading(false)
                    Log.d(TAG, "onFailure: ${t.message}")
                }
            })
        }
    }

    private fun setListener() {
        binding.apply {
            buttonConfirm.setOnClickListener {
                confirm()
            }

            ivStudentImage.setOnClickListener {
                val intent = Intent(this@ResultScanActivity, ImageViewActivity::class.java)
                intent.putExtra("imageUrl", studentImage)
                startActivity(intent)
            }

            ivGuardianImage.setOnClickListener {
                val intent = Intent(this@ResultScanActivity, ImageViewActivity::class.java)
                intent.putExtra("imageUrl", guardianImage)
                startActivity(intent)
            }
        }
    }

    private fun confirm() {
        val imageFile = currentImageUri?.let { uri ->
            uriToFile(uri, this).reduceFileImage()
        }

        Log.d("Image File", "showImage: ${imageFile?.path}")

        setLoading(this, true)

        val requestBodyStudentId = studentId.toString().toRequestBody("text/plain".toMediaType())
        val requestBodyGuardianId = guardianId.toString().toRequestBody("text/plain".toMediaType())
        val requestBodyAdminId = adminId.toString().toRequestBody("text/plain".toMediaType())
        val requestBodyStatus = "done".toRequestBody("text/plain".toMediaType())
        val requestBodyNote = note?.toRequestBody("text/plain".toMediaType())

        val requestImageFile = imageFile?.asRequestBody("image/jpeg".toMediaType())
        val multipartBodyImage = requestImageFile?.let {
            MultipartBody.Part.createFormData(
                "image",
                imageFile.name,
                it
            )
        }

        lifecycleScope.launch {
            try {
                val apiService = ApiConfig.getApiService()
                val successResponse = apiService.pickup(
                    "Bearer $token",
                    requestBodyStudentId,
                    requestBodyGuardianId,
                    requestBodyAdminId,
                    requestBodyStatus,
                    requestBodyNote,
                    multipartBodyImage
                ).await()

                Toast.makeText(
                    this@ResultScanActivity,
                    successResponse.message,
                    Toast.LENGTH_SHORT
                ).show()
                setLoading(this@ResultScanActivity, false)
                goToMain()
            } catch (e: HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                val errorResponse = Gson().fromJson(errorBody, DefaultResponse::class.java)
                Toast.makeText(
                    this@ResultScanActivity,
                    errorResponse.message,
                    Toast.LENGTH_SHORT
                ).show()
                setLoading(this@ResultScanActivity, false)
            }
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
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

    companion object {
        private val TAG = ResultScanActivity::class.java.simpleName
    }
}