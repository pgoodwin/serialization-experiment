import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result

data class MultiValuePayload(val first: String = "First value", val second: String = "Second value")
data class MultiValueError(val code: String, val reason: String, val status: String, val message: String)

fun main() {
    // The values we'll be serializing
    val successObject = Ok(MultiValuePayload(second = "some other value"))
    val errorObject = Err("No bueno")
    val multiValueErrorObject = Err(MultiValueError("invalidState", "insufficientdata", "418", "I'm goin' home"))

    val resultArray = arrayOf(successObject, errorObject)

    // Initialize the mapper with our serializers
    val mapper = ObjectMapper()
    // Define types in a way that Jackson understands. This is a downside of Java's type erasure
    val multivaluePayloadResultType = mapper.typeFactory.constructParametricType(
        Result::class.java,
        MultiValuePayload::class.java,
        String::class.java
    )
    val multiValueErrorResultType = mapper.typeFactory.constructParametricType(
        Result::class.java,
        String::class.java,
        MultiValueError::class.java
    )

    val resultSerializer = ResultSerializer()
    val deserializers = JavaTypeDeserializers(mapper.typeFactory)
    deserializers.addDeserializer(
        multivaluePayloadResultType,
        ResultDeserializer(
            MultiValuePayload::class.java,
            String::class.java
        )
    )
    deserializers.addDeserializer(
        multiValueErrorResultType,
        ResultDeserializer(
            String::class.java,
            MultiValueError::class.java
        )
    )
    val kotlinModule = KotlinModule()
    kotlinModule.setDeserializers(deserializers)
    kotlinModule.addSerializer(Result::class.java, resultSerializer)
    mapper.registerModule(kotlinModule)


    // Serialize and deserialize the array
    val arrayAsJson = mapper.writeValueAsString(resultArray)
    println("Array of results as Json: $arrayAsJson")

    val resultArrayType = mapper.typeFactory.constructArrayType(multivaluePayloadResultType)
    val reconstitutedArray = mapper.readValue<Array<Result<MultiValuePayload, String>>>(arrayAsJson, resultArrayType)
    println("reconstitution successful: " + (reconstitutedArray[0] == successObject && reconstitutedArray[1] == errorObject))

    // Serialize and deserialize the other error object
    val anotherErrorJson = mapper.writeValueAsString(multiValueErrorObject)
    println("A different error json: $anotherErrorJson")

    val anotherReconstitutedError =
        mapper.readValue<Result<String, MultiValueError>>(anotherErrorJson, multiValueErrorResultType)
    println("reconstitution successful: " + (anotherReconstitutedError == multiValueErrorObject))
}
