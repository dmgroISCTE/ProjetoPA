import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

abstract class JsonElements() {

    abstract fun accept(v: Visitor)
    abstract fun generate() : Any
}

class JSON(var objList: MutableList<JsonObject> = mutableListOf<JsonObject>()) : JsonElements() {

    override fun accept(v: Visitor) {
        v.visit(this)
        objList.forEach {
            it.accept(v)
        }
    }

    override fun generate(): Any {
        var text : String = ""
        objList.forEach {
            text += it.generate() + "\n"
        }
        return text
    }
}

class JsonValue(val name : String, val value : JsonElements?) : JsonElements() {

    override fun accept(v: Visitor) {
        v.visit(this)
        value?.accept(v)
        v.endVisit(this)
    }

    override fun generate(): String {
        var elementValue: String
        when (value) {
            is JsonString -> {
                elementValue = value.generate()
                return "\"$name\" : $elementValue "
            }
            is JsonBoolean -> {
                elementValue = value.generate()
                return "\"$name\" : $elementValue "
            }
            is JsonNumber -> {
                elementValue = value.generate()
                return "\"$name\" : $elementValue "
            }
            is JsonObject -> {
                elementValue = value.generate()
                return "\"$name\" : $elementValue "
            }
            is JsonArray -> {
                elementValue = value.generate()
                return "\"$name\" : $elementValue "
            }
        }
        return ""
    }

}
class JsonString(val content: String) : JsonElements() {

    override fun accept(v: Visitor) {
        v.visit(this)
    }

    override fun generate(): String {
        return "\"" + content + "\""
    }

}

class JsonNumber(val number : Number) : JsonElements() {

    override fun accept(v: Visitor) {
        v.visit(this)
    }

    override fun generate() : String {
        return "$number"
    }

}

class JsonObject(var listValue : MutableList<JsonValue>) : JsonElements() {

    fun addValue(value: JsonValue) {
        listValue.add(value)
    }

    fun getList(): MutableList<JsonValue> {
        return listValue
    }

    override fun accept(v: Visitor) {
        v.visit(this)
        listValue.forEach {
            it.accept(v)
        }
        v.endVisit(this)
    }


    override fun generate(): String {
        var objgenerate = "{\n"
        listValue.forEach {
            objgenerate += if (it == listValue.last()){
                " " + "${it.generate()}\n"
            } else {
                " " + "${it.generate()} ,\n"
            }
        }
        objgenerate += "}"
        return objgenerate
    }

}

class JsonArray(var array: MutableList<JsonValue>) : JsonElements() {

    override fun accept(v: Visitor) {
        v.visit(this)
        array.forEach {
            it.accept(v)
        }
        v.endVisit(this)
    }

    override fun generate(): String {
        var arrayaux : String = "[\n"
        array.forEach {
            arrayaux += if (it == array.last()){
                "${it.value?.generate()}"
            } else {
                "${it.value?.generate()}" + ",\n "
            }
        }
        arrayaux += "\n]"
        return arrayaux
    }

}

class JsonBoolean(val boolean : Boolean) : JsonElements() {
    override fun accept(v: Visitor) {
        v.visit(this)
    }

    override fun generate(): String {
        return "$boolean"
    }

}
// ************ Funções *************

// Imprimir todas as strings num JSON
fun printStrings(json : JsonElements) : String{
    val v = object : Visitor{
        var text : String = ""
        override fun visit(jsonObj : JsonValue) {
            when(jsonObj.value){
                is JsonString -> { text += jsonObj.value.generate() + "\n" }
                is JsonArray -> { text += printStrings(jsonObj.value) }
                is JsonObject -> { text += printStrings(jsonObj.value) }
            }
        }
    }
    json.accept(v)
    return v.text
}


// Procurar um objeto conforme a pesquisa
fun find(json : JsonElements, campo : JsonValue) : String{
    val v = object : Visitor {
        var text : String = ""
        override fun visit(obj : JsonObject) {
            obj.getList().forEach {
                when (it.value){
                    campo.value -> { text += obj.generate() + "\n" }
                    is JsonObject -> { find(it, campo) }
                    is JsonArray -> { find(it, campo) }
                }
            }
        }
    }
    json.accept(v)
    return v.text
}

// *************************************


interface Visitor{
    fun visit(jsonString : JsonString) { }
    fun visit(jsonNumber : JsonNumber) { }
    fun visit(jsonArray : JsonArray) { }
    fun visit(jsonObject : JsonObject) { }
    fun visit(jsonBoolean : JsonBoolean) { }
    fun visit(jsonValue : JsonValue) { }
    fun visit(json : JSON) { }
    fun endVisit(jsonObject: JsonObject) { }
    fun endVisit(jsonArray: JsonArray) { }
    fun endVisit(jsonValue: JsonValue) { }
}


fun convertTo(o: Any): JsonElements? {
    var swap : JsonElements? = null
    if (o::class.isData){
        swap = convertDataClass(o)
    } else {
        when (o) {
            is Number -> swap = JsonNumber(o)
            is Boolean -> swap = JsonBoolean(o)
            is String -> swap = JsonString(o)
            is Collection<*> -> swap = convertArrayTo(o)
            is Map<*, *> -> swap = convertMapTo(o)
            //o::class.isData -> swap = convertDataClass(o) Dentro do When não funciona?

        }
    }
    return swap
}

fun convertArrayTo(array: Collection<*>): JsonArray {

    var list = mutableListOf<JsonValue>()
    var toAdd : JsonValue
    array.forEach {
        toAdd = JsonValue("", convertTo(it!!) )
        list.add(toAdd)
    }

    return JsonArray(list)
}

fun convertMapTo(map: Map<*,*>): JsonObject{

    var list = mutableListOf<JsonValue>()
    var toAdd : JsonValue

    map.forEach { (x, y) -> toAdd = JsonValue(x as String, convertTo(y!!))
            list.add(toAdd)
    }

    return JsonObject(list)
}


fun convertDataClass(d: Any) : JsonObject{
    var list = mutableListOf<JsonValue>()

    d::class.declaredMemberProperties.forEach {
        val value = JsonValue(it.name, convertTo(it.call(d)!!))
        list.add(value)
    }

    return JsonObject(list)
}


enum class PersonSign {
    Aries, Taurus, Gemini, Cancer;

    companion object {
        fun values() = PersonSign.values()
    }
}


@Target(AnnotationTarget.PROPERTY)
annotation class Name
@Target(AnnotationTarget.PROPERTY)
annotation class Age
@Target(AnnotationTarget.PROPERTY)
annotation class Hobbies
@Target(AnnotationTarget.PROPERTY)
annotation class License


data class Person(@Name val personName : String, @Age val number: Int, @Hobbies val hobby: String, @License val license: Boolean)


fun main() {

    //CRIAR UM JSON SIMPLES
    val list = mutableListOf<JsonValue>()
    val mapa = mutableMapOf<String, Any>()

    val nome = JsonValue("Nome", convertTo("Eu"))
    val hobbies = JsonValue("Hobbies", convertTo("Viagens e Pesca"))
    val idade = JsonValue("Idade", convertTo(28))
    val carta = JsonValue("Licença", convertTo(true))

    mapa["Nome"] = "Diogo"
    mapa["Idade"] = 28
    mapa["Lista"] = listOf("Nao sei", true)
    val objeto = JsonValue("MapaObjeto", convertMapTo(mapa))
    val person = Person("Diogo", 22, "Viajar", true)

    val personJson = JsonValue("Pessoa", convertTo(person))

    val listaTeste = listOf("Diogo", 22)
    val array = JsonValue("Lista", convertTo(listaTeste))


    list.add(nome)
    list.add(hobbies)
    list.add(idade)
    list.add(carta)
    list.add(array)
    list.add(objeto)
    list.add(personJson)

    val pessoa1 = JsonObject(list)

    var listObj = mutableListOf<JsonObject>()
    listObj.add(pessoa1)
    listObj.add(convertDataClass(person))

    val json = JSON(listObj)


    println(json.generate())

    //JsonTree().open(json) Fase 3
    val w = InjectorAdd.create(JsonTree::class) // substituir por criacao com injecao - Fase 4
    w.open(json)



}

















