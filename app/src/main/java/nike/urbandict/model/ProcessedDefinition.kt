package nike.urbandict.model

import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.URLSpan
import nike.urbandict.api.Definition

data class ProcessedDefinition(
    val definition: SpannedString,
    val word: String,
    val thumbsUp: Long,
    val thumbsDown: Long,
    val id: Long
) {
    companion object {
        val BASE_URI = Uri.parse("udn://nike.urbandict/define")

        private val WORD_REGEX = "\\[([^\\[\\]]+)\\]".toPattern()

        fun fromDefinition(definition: Definition): ProcessedDefinition {
            val buffer = StringBuffer()
            val processedDefinition = SpannableStringBuilder()
            val matcher = WORD_REGEX.matcher(definition.definition)
            while (matcher.find()) {
                val word = requireNotNull(matcher.group(1))
                matcher.appendReplacement(buffer, word)
                processedDefinition.append(buffer)
                processedDefinition.setSpan(
                    URLSpan( BASE_URI.buildUpon().appendPath(word).build().toString()),
                    processedDefinition.length - word.length,
                    processedDefinition.length,
                    SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE
                )
                buffer.delete(0, buffer.length)
            }
            matcher.appendTail(buffer)
            processedDefinition.append(buffer)

            return ProcessedDefinition(
                SpannedString(processedDefinition),
                definition.word,
                definition.thumbsUp,
                definition.thumbsDown,
                definition.id
            )
        }
    }
}
