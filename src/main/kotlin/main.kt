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
    val errorPoly = UnionType(error = errorObject)
    val mapper = ObjectMapper()
    val successAsJson = mapper.writeValueAsString(successUnion)
    val errorAsJson = mapper.writeValueAsString(errorPoly)

    println(successAsJson)
    println(errorAsJson)

    val reconstitutedSuccess = mapper.readValue<UnionType>(successAsJson, UnionType::class.java)
    println("reconstitution successful: " + (reconstitutedSuccess == successUnion))
}