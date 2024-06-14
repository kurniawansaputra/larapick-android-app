package id.go.kebumenkab.larapick.ui.otherpickup

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import id.go.kebumenkab.larapick.R
import id.go.kebumenkab.larapick.data.response.DefaultResponse
import id.go.kebumenkab.larapick.data.response.GradeResponse
import id.go.kebumenkab.larapick.data.response.StudentResponse
import id.go.kebumenkab.larapick.data.retrofit.ApiConfig
import id.go.kebumenkab.larapick.databinding.ActivityOtherPickupBinding
import id.go.kebumenkab.larapick.pref.UserPreference
import id.go.kebumenkab.larapick.ui.main.MainActivity
import id.go.kebumenkab.larapick.util.createTextWatcher
import id.go.kebumenkab.larapick.util.getImageUri
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

class OtherPickupActivity : AppCompatActivity() {
    private lateinit var token: String
    private var gradeId: MutableList<Int> = mutableListOf()
    private var gradeName: MutableList<String> = mutableListOf()
    private var selectedGradeId: String? = null
    private var studentId: MutableList<Int> = mutableListOf()
    private var studentName: MutableList<String> = mutableListOf()
    private var selectedStudentId: String? = null
    private var adminId: Int = 0
    private lateinit var note: String
    private var currentImageUri: Uri? = null
    private var guardianId: Int? = null
    private lateinit var binding: ActivityOtherPickupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOtherPickupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPref()
        setToolbar()
        getGrades()
        setListener()
    }

    private fun setPref() {
        val user = UserPreference.instance(this).getUser()
        if (user != null) {
            token = user.accessToken.toString()
            adminId = user.data?.id ?: 0
        }

        Log.d(TAG, "setPref: $adminId")
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
            ivPhoto.setOnClickListener {
                startCamera()
            }

            buttonSubmit.setOnClickListener {
                editNote.addTextChangedListener(textWatcherNote)
                note = editNote.text.toString()

                val isGradeValid = validateGradeSpinner()
                val isStudentValid = validateStudentSpinner()
                val isNoteValid = validateNote()

                if (isGradeValid && isStudentValid && isNoteValid) {
                    pickup()
                }
            }
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri.let {
            Log.d("Image URI", "showImage: $it")
            binding.ivPhoto.setImageURI(it)
            binding.ivCamera.visibility = View.GONE
        }
    }

    private fun getGrades() {
        setProgressBar(true)
        val client = ApiConfig.getApiService().getGrades("Bearer $token")
        client.enqueue(object : retrofit2.Callback<GradeResponse> {
            override fun onResponse(call: Call<GradeResponse>, response: Response<GradeResponse>) {
                setProgressBar(false)
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val data = response.body()?.data

                    if (status == true) {
                        gradeName.clear()
                        gradeId.clear()
                        if (data != null) {
                            for (i in data.indices) {
                                gradeName = data.map {
                                    it?.name.toString()
                                }.toMutableList()
                                gradeId = data.map {
                                    it?.id!!
                                }.toMutableList()
                            }
                        }

                        setGradeSpinner()
                    }
                }
            }

            override fun onFailure(call: Call<GradeResponse>, t: Throwable) {
                setProgressBar(false)
                binding.containerMain.visibility = View.GONE
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun setGradeSpinner() {
        binding.apply {
            val tpsAdapter = ArrayAdapter(this@OtherPickupActivity, R.layout.list_item, gradeName)
            autoGrade.setAdapter(tpsAdapter)

            autoGrade.setOnItemClickListener { _, _, position, _ ->
                selectedGradeId = gradeId[position].toString()
                containerGrade.error = null
                Log.d(TAG, "setGradeSpinner: $selectedGradeId")

                autoStudent.setText("")
                selectedStudentId = ""

                getStudentsByGrade()
            }
        }
    }

    private fun getStudentsByGrade() {
        val client = ApiConfig.getApiService().getStudentByGrade("Bearer $token", selectedGradeId!!.toInt())
        client.enqueue(object : retrofit2.Callback<StudentResponse> {
            override fun onResponse(call: Call<StudentResponse>, response: Response<StudentResponse>) {
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val data = response.body()?.data

                    if (status == true) {
                        studentName.clear()
                        studentId.clear()
                        if (data != null) {
                            for (i in data.indices) {
                                studentName = data.map {
                                    it?.name.toString()
                                }.toMutableList()
                                studentId = data.map {
                                    it?.id!!
                                }.toMutableList()
                            }
                        }
                    }

                    setStudentSpinner()
                }
            }

            override fun onFailure(call: Call<StudentResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun setStudentSpinner() {
        binding.apply {
            val studentAdapter = ArrayAdapter(this@OtherPickupActivity, R.layout.list_item, studentName)
            autoStudent.setAdapter(studentAdapter)

            autoStudent.setOnItemClickListener { _, _, position, _ ->
                selectedStudentId = studentId[position].toString()
                containerStudent.error = null

                Log.d(TAG, "setStudentSpinner: $selectedStudentId")
            }
        }
    }

    private fun pickup() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")

            setLoading(this@OtherPickupActivity, true)

            val requestBodyStudentId = selectedStudentId.toString().toRequestBody("text/plain".toMediaType())
            val requestBodyGuardianId = guardianId?.toString()?.toRequestBody("text/plain".toMediaType())
            val requestBodyAdminId = adminId.toString().toRequestBody("text/plain".toMediaType())
            val requestBodyStatus = "on_progress".toRequestBody("text/plain".toMediaType())
            val requestBodyNote = note.toRequestBody("text/plain".toMediaType())

            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBodyImage = requestImageFile.let {
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
                        this@OtherPickupActivity,
                        successResponse.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    setLoading(this@OtherPickupActivity, false)

                    goToMain()

                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, DefaultResponse::class.java)
                    Toast.makeText(
                        this@OtherPickupActivity,
                        errorResponse.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    setLoading(this@OtherPickupActivity, false)
                }
            }
        } ?: Toast.makeText(
            this@OtherPickupActivity,
            "Ambil foto terlebih dahulu",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun validateGradeSpinner(): Boolean {
        binding.apply {
            return if (autoGrade.text.isEmpty()) {
                containerGrade.error = "Kelas harus dipilih"
                false
            } else {
                containerGrade.error = null
                true
            }
        }
    }

    private fun validateStudentSpinner(): Boolean {
        binding.apply {
            return if (autoStudent.text.isEmpty()) {
                containerStudent.error = "Siswa harus dipilih"
                false
            } else {
                containerStudent.error = null
                true
            }
        }
    }

    private val textWatcherNote: TextWatcher = createTextWatcher(::validateNote)

    private fun validateNote(): Boolean {
        binding.apply {
            return if (editNote.text!!.isEmpty()) {
                containerNote.error = "Catatan harus diisi"
                false
            } else {
                containerNote.error = null
                true
            }
        }
    }

    private fun setProgressBar(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                containerMain.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                containerMain.visibility = View.VISIBLE
            }
        }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    companion object {
        private val TAG = OtherPickupActivity::class.java.simpleName
    }
}