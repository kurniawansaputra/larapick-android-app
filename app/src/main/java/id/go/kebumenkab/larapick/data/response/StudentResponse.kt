package id.go.kebumenkab.larapick.data.response

import com.google.gson.annotations.SerializedName

data class StudentResponse(

	@field:SerializedName("data")
	val data: List<Student?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)