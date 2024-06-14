package id.go.kebumenkab.larapick.ui.pickuplogs

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import id.go.kebumenkab.larapick.data.response.PickupLog
import id.go.kebumenkab.larapick.databinding.ItemRowPickupLogsBinding
import id.go.kebumenkab.larapick.ui.imageview.ImageViewActivity
import id.go.kebumenkab.larapick.util.dateFormatter

class PickupLogsAdapter(private val pickupLogsList: List<PickupLog>): RecyclerView.Adapter<PickupLogsAdapter.ViewHolder>() {
    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

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
                    textGuardian.text = guardian?.name.toString()
                    textAdmin.text = admin?.name
                    textDate.text = dateFormatter(pickupTime.toString())

                    if (note.isNullOrEmpty()) {
                        labelNote.visibility = View.GONE
                        textNote.visibility = View.GONE
                        buttonSeeImage.visibility = View.GONE
                    } else {
                        labelNote.visibility = View.VISIBLE
                        textNote.visibility = View.VISIBLE
                        buttonSeeImage.visibility = View.VISIBLE
                        textNote.text = note
                    }

                    if (status == "on_progress") {
                        buttonConfirm.visibility = View.VISIBLE
                    } else {
                        buttonConfirm.visibility = View.GONE
                    }

                    buttonSeeImage.setOnClickListener {
                        val intent = Intent(itemView.context, ImageViewActivity::class.java)
                        intent.putExtra("imageUrl", image)
                        itemView.context.startActivity(intent)
                    }

                    buttonConfirm.setOnClickListener {
                        onItemClickCallback.onItemClicked(pickupLogsList[holder.adapterPosition])
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = pickupLogsList.size

    interface OnItemClickCallback {
        fun onItemClicked(data: PickupLog)
    }
}