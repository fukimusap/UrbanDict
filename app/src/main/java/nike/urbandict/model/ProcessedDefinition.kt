package nike.urbandict.model

import android.text.SpannedString

data class ProcessedDefinition(
    val definition: SpannedString,
    val word: String,
    val thumbsUp: Long,
    val thumbsDown: Long,
    val id: Long
)
