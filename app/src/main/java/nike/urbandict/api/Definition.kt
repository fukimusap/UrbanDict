package nike.urbandict.api

import com.google.gson.annotations.SerializedName

data class Definition(
    val definition: String,
    val permalink: String,
    val word: String,
    @SerializedName("thumbs_up")
    val thumbsUp: Long,
    @SerializedName("thumbs_down")
    val thumbsDown: Long,
    @SerializedName("written_on")
    val writtenOn: String,
    @SerializedName("defid")
    val id: Long,
    val example: String
)
