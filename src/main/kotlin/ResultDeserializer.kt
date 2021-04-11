import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.io.SerializedString
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

class ResultDeserializer(
    private val successType: Class<*>,
    private val errorType: Class<*>
) : JsonDeserializer<Result<*, *>>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?): Result<*, *> {
        if (parser != null) {
            if (parser.isExpectedStartObjectToken) {
                parser.clearCurrentToken()
                parser.nextFieldName(SerializedString("type"))
                parser.nextToken()
                when (parser.valueAsString) {
                    "successResult" -> {
                        parser.nextFieldName(SerializedString("value"))
                        parser.nextToken()
                        val value = parser.readValueAs(successType)
                        parser.nextToken()
                        return Ok(value)
                    }
                    "errorResult" -> {
                        parser.nextFieldName(SerializedString("error"))
                        parser.nextToken()
                        val value = parser.readValueAs(errorType)
                        parser.nextToken()
                        return Err(value)
                    }
                    else -> TODO("Need proper error handling for incorrectly formatted Json")
                }
            }
        }
        TODO("Need proper error handling for parser being null or in incorrect state")
    }
}