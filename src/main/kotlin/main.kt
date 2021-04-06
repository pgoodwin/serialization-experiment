import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper

data class Payload(val message: String = "Hi mom")
data class Error(val status: String = "LookingBad", val message: String = "hang it up")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UnionType(val payload: Payload? = null, val error: Error? = null)

fun main() {
    val successObject = Payload()
    val errorObject = Error()
    val successUnion = UnionType(payload = successObject)
    val failureUnion = UnionType(error = errorObject)
    val mapper = ObjectMapper()
    println(mapper.writeValueAsString(successUnion))
    println(mapper.writeValueAsString(failureUnion))
}