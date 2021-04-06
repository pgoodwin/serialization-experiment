import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes(JsonSubTypes.Type(value = UnionType.Payload::class, name = "payload"),  JsonSubTypes.Type(value = UnionType.Error::class, name = "error"))
sealed class UnionType {
    data class Payload(val message: String = "Hi mom"): UnionType()
    data class Error(val status: String = "LookingBad", val message: String = "hang it up"): UnionType()
}

fun main() {
    val successObject = UnionType.Payload()
    val errorObject = UnionType.Error()

    val mapper = ObjectMapper()
    val successAsJson = mapper.writeValueAsString(successObject)
    val errorAsJson = mapper.writeValueAsString(errorObject)

    println(successAsJson)
    println(errorAsJson)

    val reconstitutedSuccess = mapper.readValue<UnionType>(successAsJson, UnionType::class.java)
    println("reconstitution successful: " + (reconstitutedSuccess == successObject))
}