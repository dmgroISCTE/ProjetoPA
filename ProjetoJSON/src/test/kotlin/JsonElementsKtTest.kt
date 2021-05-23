import org.junit.Test

import org.junit.Assert.*

class JsonElementsKtTest {

    @Test
    fun generateJSON() {

        var list = mutableListOf<JsonValue>()
        var listarray = mutableListOf<JsonValue>()

        val nome = JsonValue("Nome", JsonString("Rafael"))
        val hobbies = JsonValue("Hobbies", JsonString("Viagens e Pesca"))
        val idade = JsonValue("Idade", JsonNumber(22F))
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

        val json = JSON(listObj)

        assertEquals( "{\n" +
                " \"Nome\" : \"Rafael\"  ,\n" +
                " \"Hobbies\" : \"Viagens e Pesca\"  ,\n" +
                " \"Idade\" : 22.0  ,\n" +
                " \"Licença\" : true  ,\n" +
                " \"Lista\" : [\"Nome\" : \"Rafael\" , \"Idade\" : 22.0 ] \n" +
                "}\n" , json.generate())


    }


    @Test
    fun printStrings() {

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

        val json = JSON(listObj)


        kotlin.test.assertEquals("\"nome1\"\n\"nome1\"\n\"nome1\"\n\"nome2\"\n\"nome2\"\n" , printStrings(json))

    }
}