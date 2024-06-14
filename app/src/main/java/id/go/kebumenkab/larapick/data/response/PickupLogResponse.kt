package id.go.kebumenkab.larapick.data.response

import com.google.gson.annotations.SerializedName

data class PickupLogResponse(

	@field:SerializedName("data")
	val data: List<PickupLog?>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class Guardian(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class Admin(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)

data class PickupLog(

	@field:SerializedName("note")
	val note: String? = null,

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("student")
	val student: Student? = null,

	@field:SerializedName("admin")
	val admin: Admin? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("pickup_time")
	val pickupTime: String? = null,

	@field:SerializedName("guardian")
	val guardian: Guardian? = null,

	@field:SerializedName("status")
	val status: String? = null
)
