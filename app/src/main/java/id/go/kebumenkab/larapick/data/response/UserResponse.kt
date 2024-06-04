package id.go.kebumenkab.larapick.data.response

import com.google.gson.annotations.SerializedName

data class UserResponse(

	@field:SerializedName("access_token")
	val accessToken: String? = null,

	@field:SerializedName("data")
	val data: User? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("token_type")
	val tokenType: String? = null,

	@field:SerializedName("status")
	val status: Boolean? = null
)

data class User(

	@field:SerializedName("is_admin")
	val isAdmin: Boolean? = null,

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("id_number")
	val idNumber: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("student")
	val student: Student? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("relationship")
	val relationship: String? = null,

	@field:SerializedName("job")
	val job: String? = null,

	@field:SerializedName("email")
	val email: String? = null,

	@field:SerializedName("qr_code")
val qrCode: String? = null,
)

data class Student(

	@field:SerializedName("place_of_birth")
	val placeOfBirth: String? = null,

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("gender")
	val gender: String? = null,

	@field:SerializedName("phone")
	val phone: String? = null,

	@field:SerializedName("date_of_birth")
	val dateOfBirth: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("class")
	val grade: Grade? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("guardians")
	val guardians: List<User?>? = null,
)

data class Grade(

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
