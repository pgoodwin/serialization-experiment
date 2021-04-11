import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes(JsonSubTypes.Type(value = UnionType.Payload::class, name = "payload"),  JsonSubTypes.Type(value = UnionType.Error::class, name = "error"))
sealed class UnionType {
    data class Payload(val message: String = "Hi mom"): UnionType()
    data class Error(val status: String = "LookingBad", val message: String = "hang it up"): UnionType()
}

data class MultiValuePayload(val first: String = "First value", val second: String = "Second value")

fun main() {
    resultExperiment()
    println()
    unionTypeExperiment()
}

fun resultExperiment() {
    // The values we'll be serializing
    val successObject = Ok(MultiValuePayload(second = "some other value"))
    val errorObject = Err("No bueno")
    // Doing it this way causes multiple instances to be deserialized which forces the deserializer to
    // correctly consume all the tokens to avoid breaking the deserialization of downstream objects
    val resultArray = arrayOf(successObject, errorObject)

    // Initialize the mapper with our serializers
    val resultSerializer = ResultJsonSerializer()
    val resultDeserializer = ResultDeserializer(
        MultiValuePayload::class.java,
        String::class.java
    )

    val kotlinModule = KotlinModule()
        .addSerializer(Result::class.java, resultSerializer)
        .addDeserializer(Result::class.java, resultDeserializer)

    val mapper = ObjectMapper().registerModule(kotlinModule)

    // Serialize
    val arrayAsJson = mapper.writeValueAsString(resultArray)
    println("Array of results as Json: $arrayAsJson")

    // Define types in a way that Jackson understands. This is the downside of Java's type erasure
    val resultType = mapper.typeFactory.constructParametricType(
        Result::class.java,
        MultiValuePayload::class.java,
        String::class.java
    ) // means: Result<MultiValuePayload, String>>
    val resultArrayType = mapper.typeFactory.constructArrayType(resultType) // means: Array<Result<MultiValuePayload, String>>

    // Deserialize
    val reconstitutedArray = mapper.readValue<Array<Result<MultiValuePayload, String>>>(arrayAsJson, resultArrayType)
    println("reconstitution successful: " + (reconstitutedArray[0] == successObject && reconstitutedArray[1] == errorObject))
}

private fun unionTypeExperiment() {
    val successObject = UnionType.Payload()
    val errorObject = UnionType.Error()

    val mapper = ObjectMapper()
    val successAsJson = mapper.writeValueAsString(successObject)
    val errorAsJson = mapper.writeValueAsString(errorObject)

    println("Success as Json: $successAsJson")
    println("Error as Json: $errorAsJson")

    val reconstitutedSuccess = mapper.readValue<UnionType>(successAsJson, UnionType::class.java)
    println("reconstitution successful: " + (reconstitutedSuccess == successObject))
}