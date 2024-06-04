package id.go.kebumenkab.larapick.ui.pickuplogs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.go.kebumenkab.larapick.data.response.PickupLogs
import id.go.kebumenkab.larapick.databinding.ItemRowPickupLogsBinding
import id.go.kebumenkab.larapick.util.dateFormatter

class PickupLogsAdapter(private val pickupLogsList: List<PickupLogs>): RecyclerView.Adapter<PickupLogsAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemRowPickupLogsBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemRowPickupLogsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(pickupLogsList[position]) {
                binding.apply {
                    textGuardian.text = guardian?.name
                    textAdmin.text = admin?.name
                    textDate.text = dateFormatter(pickupTime.toString())
                }
            }
        }
    }

    override fun getItemCount(): Int = pickupLogsList.size
}