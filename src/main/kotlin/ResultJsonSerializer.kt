import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.github.michaelbull.result.*

class ResultJsonSerializer : JsonSerializer<Result<*, *>>() {
    override fun serialize(value: Result<*, *>?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        if (value != null && gen != null) {
            gen.writeStartObject()
            when (value::class.java) {
                Ok::class.java -> {
                    gen.writeStringField("type", "successResult")
                    gen.writeObjectField("value", value.get())
                }
                Err::class.java -> {
                    gen.writeStringField("type", "errorResult")
                    gen.writeObjectField("error", value.getError())

                }
                else -> TODO("Should not be possible to get here")
            }
            gen.writeEndObject()
            return
        }
        TODO("Need proper error handling for when either value or gen are null (is either possible?)")
    }
}