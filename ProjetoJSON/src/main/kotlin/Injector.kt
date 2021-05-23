import java.io.File
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.jvm.isAccessible

@Target(AnnotationTarget.PROPERTY)
annotation class Inject

@Target(AnnotationTarget.PROPERTY)
annotation class InjectAdd

class InjectorAdd{

    companion object{

        val map : MutableMap<String, MutableList<KClass<*>>> = mutableMapOf()
        val list = mutableListOf<KClass<*>>()

        init{
            val scanner = Scanner(File("di.properties"))
            while(scanner.hasNextLine()){
                val line = scanner.nextLine()
                val parts = line.split("=")
                val actionParts = parts[1].split(",")
                actionParts.forEach {
                    list.add(Class.forName(it).kotlin)
                }
                map[parts[0]] = list
            }
            scanner.close()
        }

        fun <T:Any> create(type : KClass<T>) : T{
            val o : T = type.createInstance()
            type.declaredMemberProperties.forEach {
                val prop = it
                if( it.hasAnnotation<Inject>()){
                    it.isAccessible = true
                    val key = type.simpleName + "." + it.name
                    val obj = map[key]!![0].createInstance()
                    (it as KMutableProperty<*>).setter.call(o,obj)
                }
                if( it.hasAnnotation<InjectAdd>()){
                    it.isAccessible = true
                    val key = type.simpleName + "." + it.name
                        map[key]!!.forEach {
                            if (it != map[key]!![0]) {
                                val obj = it.createInstance()
                                (prop.getter.call(o) as MutableList<Any>).add(obj)
                            }
                        }
                }
            }
            return o
        }
    }
}
