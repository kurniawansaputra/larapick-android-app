package id.go.kebumenkab.larapick.data.response

import com.google.gson.annotations.SerializedName

data class GradeResponse(

	@field:SerializedName("data")
	val data: List<Grade?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)
