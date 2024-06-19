package id.go.kebumenkab.larapick.ui.profilestudent

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.go.kebumenkab.larapick.R
import id.go.kebumenkab.larapick.databinding.ActivityProfileStudentBinding
import id.go.kebumenkab.larapick.pref.UserPreference
import id.go.kebumenkab.larapick.util.loadImage

class ProfileStudentActivity : AppCompatActivity() {
    private lateinit var studentName: String
    private lateinit var studentGrade: String
    private lateinit var studentImage: String
    private lateinit var studentGender: String
    private lateinit var studentPlaceDateOfBirth: String
    private lateinit var studentStatus: String
    private lateinit var studentAddress: String
    private lateinit var studentPhone: String
    private lateinit var binding: ActivityProfileStudentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPref()
        setToolbar()
        setDetail()
    }
    private fun setPref() {
        val user = UserPreference.instance(this).getUser()
        if (user != null) {
            studentName = user.data?.student?.name.toString()
            studentGrade = user.data?.student?.grade?.name.toString()
            studentImage = user.data?.student?.image.toString()
            studentGender = user.data?.student?.gender.toString()
            studentPlaceDateOfBirth = "${user.data?.student?.placeOfBirth.toString()}, ${user.data?.student?.dateOfBirth.toString()}"
            studentStatus = user.data?.student?.status.toString()
            studentAddress = user.data?.student?.address.toString()
            studentPhone = user.data?.student?.phone.toString()
        }
    }

    private fun setToolbar() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDetail() {
        binding.apply {
            textName.text = studentName
            textGrade.text = "Kelas.$studentGrade"
            textStudentPlaceDateOfBirth.text = studentPlaceDateOfBirth
            textStudentStatus.text = studentStatus
            textStudentAddress.text = studentAddress
            textStudentPhone.text = studentPhone

            if (studentGender == "male") {
                textStudentGender.text = "Laki-laki"
            } else {
                textStudentGender.text = "Perempuan"
            }

            loadImage(this@ProfileStudentActivity, ivImage, studentImage, studentName)
        }
    }

}