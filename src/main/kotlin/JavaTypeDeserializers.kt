import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer
import com.fasterxml.jackson.databind.module.SimpleDeserializers
import com.fasterxml.jackson.databind.type.*
import java.io.Serializable

open class JavaTypeDeserializers @JvmOverloads constructor(private val _typeFactory: TypeFactory = TypeFactory.defaultInstance()) :
    SimpleDeserializers(), Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }

    private var _javaTypeMappings = HashMap<JavaType, JsonDeserializer<*>>()

    override fun <T> addDeserializer(forClass: Class<T>, deser: JsonDeserializer<out T>) {
        val key = _javaTypeFor(forClass)
        addDeserializer(key, deser)
    }

    fun <T> addDeserializer(forJavaKey: JavaType, deser: JsonDeserializer<out T?>) {
        _javaTypeMappings[forJavaKey] = deser
        if (forJavaKey.rawClass == Enum::class.java) {
            _hasEnumDeserializer = true
        }
    }

    fun addDeserializersForJavaTypes(desers: Map<JavaType, JsonDeserializer<*>>) {
        for ((javaType, value) in desers) {
            val deser = value as JsonDeserializer<Any>
            addDeserializer(javaType, deser)
        }
    }

    @Throws(JsonMappingException::class)
    override fun findArrayDeserializer(
        type: ArrayType?,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?,
        elementTypeDeserializer: TypeDeserializer?,
        elementDeserializer: JsonDeserializer<*>?
    ): JsonDeserializer<*>? {
        return _javaTypeMappings[type]
    }

    @Throws(JsonMappingException::class)
    override fun findBeanDeserializer(
        type: JavaType?,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?
    ): JsonDeserializer<*>? {
        return _javaTypeMappings[type]
    }

    @Throws(JsonMappingException::class)
    override fun findCollectionDeserializer(
        type: CollectionType?,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?,
        elementTypeDeserializer: TypeDeserializer?,
        elementDeserializer: JsonDeserializer<*>?
    ): JsonDeserializer<*>? {
        return _javaTypeMappings[type]
    }

    @Throws(JsonMappingException::class)
    override fun findCollectionLikeDeserializer(
        type: CollectionLikeType?,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?,
        elementTypeDeserializer: TypeDeserializer?,
        elementDeserializer: JsonDeserializer<*>?
    ): JsonDeserializer<*>? {
        return _javaTypeMappings[type]
    }

    @Throws(JsonMappingException::class)
    override fun findEnumDeserializer(
        type: Class<*>?,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?
    ): JsonDeserializer<*>? {
        if (type == null) {
            return null
        }

        var deser = _javaTypeMappings.get(type)
        if (deser == null) {
            if (_hasEnumDeserializer && type.isEnum) {
                deser = _getDeserializerForClass(Enum::class.java)
            }
        }
        return deser
    }

    @Throws(JsonMappingException::class)
    override fun findTreeNodeDeserializer(
        nodeType: Class<out JsonNode?>?,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?
    ): JsonDeserializer<*>? {
        return _getDeserializerForClass(nodeType)
    }

    @Throws(JsonMappingException::class)
    override fun findReferenceDeserializer(
        refType: ReferenceType?,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?,
        contentTypeDeserializer: TypeDeserializer?,
        contentDeserializer: JsonDeserializer<*>?
    ): JsonDeserializer<*>? {
        return _javaTypeMappings[refType]
    }

    @Throws(JsonMappingException::class)
    override fun findMapDeserializer(
        type: MapType?,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?,
        keyDeserializer: KeyDeserializer?,
        elementTypeDeserializer: TypeDeserializer?,
        elementDeserializer: JsonDeserializer<*>?
    ): JsonDeserializer<*>? {
        return _javaTypeMappings[type]
    }

    @Throws(JsonMappingException::class)
    override fun findMapLikeDeserializer(
        type: MapLikeType?,
        config: DeserializationConfig?,
        beanDesc: BeanDescription?,
        keyDeserializer: KeyDeserializer?,
        elementTypeDeserializer: TypeDeserializer?,
        elementDeserializer: JsonDeserializer<*>?
    ): JsonDeserializer<*>? {
        return _javaTypeMappings[type]
    }

    override fun hasDeserializerFor(config: DeserializationConfig?, valueType: Class<*>): Boolean {
        return _javaTypeMappings.containsKey(_javaTypeFor(valueType))
    }

    private fun <T> _javaTypeFor(forClass: Class<T>): JavaType {
        return _typeFactory.constructSimpleType(forClass, arrayOfNulls(0))
    }

    private fun _getDeserializerForClass(forClass: Class<*>?): JsonDeserializer<*>? {
        return when {
            forClass != null -> _javaTypeMappings[_javaTypeFor(forClass)]
            else -> null
        }
    }
}