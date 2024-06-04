package id.go.kebumenkab.larapick.ui.pickuplogs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import id.go.kebumenkab.larapick.data.retrofit.ApiConfig
import id.go.kebumenkab.larapick.R
import id.go.kebumenkab.larapick.data.response.PickupLogs
import id.go.kebumenkab.larapick.data.response.PickupLogsResponse
import id.go.kebumenkab.larapick.databinding.ActivityPickupLogsBinding
import id.go.kebumenkab.larapick.databinding.LayoutDialogFilterByMonthBinding
import id.go.kebumenkab.larapick.pref.UserPreference
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PickupLogsActivity : AppCompatActivity() {
    private val months = arrayOf(
        "Januari" to "01",
        "Februari" to "02",
        "Maret" to "03",
        "April" to "04",
        "Mei" to "05",
        "Juni" to "06",
        "Juli" to "07",
        "Agustus" to "08",
        "September" to "09",
        "Oktober" to "10",
        "November" to "11",
        "Desember" to "12"
    )

    private var selectedMonth: String? = getDefaultMonthValue()
    private var selectedYear: String? = getDefaultYearValue()
    private lateinit var token: String

    private lateinit var binding: ActivityPickupLogsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPickupLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setPref()
        setToolbar()
        optionMenu()
        swipeRefresh()
        getPickupLogs()
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

    private fun optionMenu() {
        binding.apply {
            topAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menuFilter -> {
                        filterByMonth()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getPickupLogs() {
        setLoading(true)
        val client = ApiConfig.getApiService().getPickupLogs("Bearer $token", selectedYear!!.toInt(), selectedMonth!!.toInt())
        client.enqueue(object : retrofit2.Callback<PickupLogsResponse> {
            override fun onResponse(call: Call<PickupLogsResponse>, response: Response<PickupLogsResponse>) {
                setLoading(false)
                binding.swipeRefresh.isRefreshing = false
                if (response.isSuccessful) {
                    val status = response.body()?.status
                    val data = response.body()?.data

                    if (status == true) {
                        val pickupLogsAdapter = PickupLogsAdapter(data as ArrayList<PickupLogs>)
                        binding.rvLog.adapter = pickupLogsAdapter
                        binding.rvLog.setHasFixedSize(true)

                        if (pickupLogsAdapter.itemCount == 0) {
                            setRecycleView(false)
                        } else {
                            setRecycleView(true)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PickupLogsResponse>, t: Throwable) {
                setLoading(false)
                binding.swipeRefresh.isRefreshing = false
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun getDefaultMonthValue(): String {
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
        return months[currentMonth].second
    }

    private fun getDefaultYearValue(): String {
        return SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
    }

    private fun filterByMonth() {
        val binding: LayoutDialogFilterByMonthBinding = LayoutDialogFilterByMonthBinding.inflate(layoutInflater)
        val builder: AlertDialog.Builder = AlertDialog.Builder(layoutInflater.context)
        builder.setView(binding.root)
        val dialog: AlertDialog = builder.create()
        binding.apply {
            // month
            val monthAdapter = ArrayAdapter(this@PickupLogsActivity, R.layout.list_item, months.map { it.first })
            binding.autoMonth.setAdapter(monthAdapter)

            val defaultMonthPosition = months.indexOfFirst { it.second == selectedMonth }.let { if (it == -1) 0 else it }
            binding.autoMonth.setText(monthAdapter.getItem(defaultMonthPosition).toString(), false)

            autoMonth.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedMonth = months[position].second
            }

            // year
            val years = ArrayList<String>()
            val thisYear: Int = Calendar.getInstance().get(Calendar.YEAR)
            for (i in 2024..thisYear) {
                years.add(i.toString())
            }

            val yearAdapter = ArrayAdapter(this@PickupLogsActivity, R.layout.list_item, years)
            binding.autoYear.setAdapter(yearAdapter)

            val defaultYearPosition = years.indexOf(selectedYear).let { if (it == -1) 0 else it }
            binding.autoYear.setText(yearAdapter.getItem(defaultYearPosition).toString(), false)

            autoYear.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedYear = years[position]
            }

            buttonBack.setOnClickListener {
                dialog.dismiss()
            }

            buttonFilter.setOnClickListener {
                getPickupLogs()
                dialog.dismiss()
            }
        }
        dialog.setCancelable(true)
        dialog.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun swipeRefresh() {
        binding.apply {
            swipeRefresh.setOnRefreshListener {
                getPickupLogs()
                labelEmptyData.visibility = View.GONE
                rvLog.visibility = View.GONE
            }
        }
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

    companion object {
        private val TAG = PickupLogsActivity::class.java.simpleName
    }
}