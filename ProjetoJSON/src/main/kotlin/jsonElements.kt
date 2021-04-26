import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.test.assertTrue

abstract class JsonElements(name : String) {

    abstract fun accept(v: Visitor)
    abstract fun generate() : Any
}

class JSON(val name : String, var objList: MutableList<JsonObject> = mutableListOf<JsonObject>()) : JsonElements("JSON") {

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

class JsonValue(val name : String, val value : JsonElements) : JsonElements("Value") {

    override fun accept(v: Visitor) {
        v.visit(this)
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
class JsonString(val content: String) : JsonElements("String") {

    override fun accept(v: Visitor) {
        v.visit(this)
    }

    override fun generate(): String {
        return "\"" + content + "\""
    }

}

class JsonNumber(val number : Float) : JsonElements("Number") {

    override fun accept(v: Visitor) {
        v.visit(this)
    }

    override fun generate() : String {
        return "$number"
    }

}

class JsonObject(var listValue : MutableList<JsonValue>) : JsonElements("Object") {

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

class JsonArray(var array : MutableList<JsonValue>) : JsonElements("Array") {

    override fun accept(v: Visitor) {
        v.visit(this)
        array.forEach {
            it.accept(v)
        }
    }

    override fun generate(): String {
        var arrayaux : String = "["
        array.forEach {
            arrayaux += if (it == array.last()){
                "${it.generate()}"
            } else {
                "${it.generate()}" + ", "
            }
        }
        arrayaux += "]"
        return arrayaux
    }

}

class JsonBoolean(val boolean : Boolean) : JsonElements("Boolean") {
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
        override fun visit(jsonObj : JsonObject) { text += printStringObject(jsonObj) }
    }
    json.accept(v)
    return v.text
}

// Função auxiliar para obter todas as strings num objecto
private fun printStringObject(obj : JsonObject) : String{
    val v = object : Visitor{
        var text : String = ""
        override fun visit(jsonValue: JsonValue) {
            when(jsonValue.value){
                is JsonString -> { text += jsonValue.value.generate() + "\n" }
                is JsonArray -> { text += printStringArray(jsonValue.value) }
                is JsonObject -> { text += printStringObject(jsonValue.value) }
            }
        }
    }
    obj.accept(v)
    return v.text
}

// Função auxiliar para obter todas as strings num array
private fun printStringArray(array : JsonArray) : String{
    val v = object : Visitor{
        var text : String = ""
        override fun visit(jsonValue: JsonValue) {
            when(jsonValue.value){
                is JsonString -> { text += jsonValue.value.generate() + "\n" }
                is JsonArray -> { text += printStringArray(jsonValue.value) }
                is JsonObject -> { text += printStringObject(jsonValue.value) }
            }
        }
    }
    array.accept(v)
    return v.text
}

// Procurar um objeto conforme a pesquisa
//TODO Fix some bugs
fun find(json : JsonElements, campo : JsonValue) : String{
    val v = object : Visitor {
        var text : String = ""
        override fun visit(obj : JsonObject) {
            obj.getList().forEach {
                if (it.value == campo.value) {
                    text += obj.generate() + "\n"
                } else if (it.value is JsonObject){
                    find(it, campo)
                } else if (it.value is JsonArray){
                    find(it, campo)
                }
            }
        }
    }
    json.accept(v)
    return v.text
}

// *************************************

interface TypeMapping {
    fun mapType(c: KClass<*>): String
    fun mapObject(o: Any?): JsonObject
}

interface Visitor{
   // fun visit(element : JsonElements) { }
    fun visit(jsonString : JsonString) { }
    fun visit(jsonNumber : JsonNumber) { }
    fun visit(jsonArray : JsonArray) { }
    fun visit(jsonObject : JsonObject) { }
    fun visit(jsonBoolean : JsonBoolean) { }
    fun visit(jsonValue : JsonValue) { }
    fun visit(json : JSON) { }
}

class RefJson : TypeMapping {
    override fun mapType(c: KClass<*>): String =
        when(c){
            Float::class -> "JsonNumber"
            Boolean::class -> "JsonBoolean"
            String::class -> "JsonString"
            MutableList::class -> "JsonArray"

            else -> "Undefined"
        }

    override fun mapObject(o: Any?): JsonObject {
        TODO()
    }
}

class JSONGenerator(var typeMapping: TypeMapping) {

}




fun main(){

    //CRIAR UM JSON SIMPLES
    var list = mutableListOf<JsonValue>()
    var listarray = mutableListOf<JsonValue>()

    val nome = JsonValue("Nome", JsonString("Rafael"))
    val hobbies = JsonValue("Hobbies", JsonString("Viagens e Pesca"))
    val idade = JsonValue("Idade", JsonNumber(28F))
    val carta = JsonValue("Licença", JsonBoolean(true))
    val array = JsonValue("Lista", JsonArray(listarray))

    list.add(nome)
    list.add(hobbies)
    list.add(idade)
    list.add(carta)
    list.add(array)
    listarray.add(nome)
    listarray.add(idade)

    val pessoa1 = JsonObject(list)

    var listObj = mutableListOf<JsonObject>()
    listObj.add(pessoa1)

    val json = JSON("JSON", listObj)

    println(json.generate())
/*
    //JSON MAIS COMPLEXO

    var list = mutableListOf<JsonValue>()
    var listarray = mutableListOf<JsonValue>()
    var listarray2 = mutableListOf<JsonValue>()

    val nome = JsonValue("Nome", JsonString("nome1"))
    val idade = JsonValue("Idade", JsonNumber(30F))
    val adulto = JsonValue("Adulto", JsonBoolean(true))
    val array = JsonValue("Array", JsonArray(listarray))
    val segundoArray = JsonValue("Segundo Array", JsonArray(listarray2))

    list.add(nome)
    list.add(idade)
    list.add(adulto)
    list.add(array)

    listarray.add(nome)
    listarray.add(idade)
    listarray.add(adulto)
    listarray.add(segundoArray)

    listarray2.add(nome)

    // OBJECTO 1
    val pessoa1 = JsonObject(list)

    var list2 = mutableListOf<JsonValue>()

    val nome2 = JsonValue("Nome", JsonString("nome2"))
    val idade2 = JsonValue("Idade", JsonNumber(15F))
    val adulto2 = JsonValue("Adulto", JsonBoolean(false))

    list2.add(nome2)
    list2.add(idade2)
    list2.add(adulto2)

    val pessoa2 = JsonObject(list2)

    // OBJECTO 2
    val pessoaObj = JsonValue("Nome2", pessoa2)
    listarray.add(pessoaObj)

    var listObj = mutableListOf<JsonObject>()
    listObj.add(pessoa1)
    listObj.add(pessoa2)

    val json = JSON("JSON", listObj)
    println(json.generate())

    println("********************************************************************\n")

    println(printStrings(json))

    println("********************************************************************\n")

    println(find(json, nome))
*/


}

















