package id.go.kebumenkab.larapick.ui.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.go.kebumenkab.larapick.R
import id.go.kebumenkab.larapick.databinding.ActivityProfileBinding
import id.go.kebumenkab.larapick.pref.UserPreference
import id.go.kebumenkab.larapick.util.loadImage
import kotlin.properties.Delegates

class ProfileActivity : AppCompatActivity() {
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var image: String
    private lateinit var guardianGender: String
    private lateinit var guardianJob: String
    private lateinit var guardianIdNumber: String
    private lateinit var guardianRelationship: String
    private lateinit var guardianAddress: String
    private lateinit var guardianPhone: String
    private var isAdmin by Delegates.notNull<Boolean>()

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPref()
        setToolbar()
        initUi()
        setDetail()
    }

    private fun setPref() {
        val user = UserPreference.instance(this).getUser()
        if (user != null) {
            name = user.data?.name.toString()
            email = user.data?.email.toString()
            image = user.data?.image.toString()
            guardianIdNumber = user.data?.idNumber.toString()
            guardianGender = user.data?.gender.toString()
            guardianJob = user.data?.job.toString()
            guardianRelationship = user.data?.relationship.toString()
            guardianAddress = user.data?.address.toString()
            guardianPhone = user.data?.phone.toString()
            isAdmin = user.data?.isAdmin!!
        }
    }

    private fun setToolbar() {
        binding.apply {
            topAppBar.setNavigationOnClickListener {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun initUi() {
        binding.apply {
            if (isAdmin) {
                containerGuardian.visibility = View.GONE
            } else {
                containerGuardian.visibility = View.VISIBLE
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDetail() {
        binding.apply {
            textName.text = name
            textEmail.text = email
            textGuardianIdNumber.text = guardianIdNumber
            textGuardianJob.text = guardianJob
            textGuardianRelationship.text = guardianRelationship
            textGuardianAddress.text = guardianAddress
            textGuardianPhone.text = guardianPhone

            if (guardianGender == "male") {
                textGuardianGender.text = "Laki-laki"
            } else {
                textGuardianGender.text = "Perempuan"
            }

            loadImage(this@ProfileActivity, ivImage, image, name)
        }
    }
}