import java.util.Properties

class Properties {

    private val CONFIG_PATH = "config.properties"
    private val CREDENTIALS_PATH = "credentials.properties"


    private val properties = Properties()

    init {
        for (path in arrayOf(CONFIG_PATH, CREDENTIALS_PATH)) {
            val file = this::class.java.classLoader.getResourceAsStream(path)
            if (file == null) {
                throw IOException(
                    "Necessary file is missing: $path"
                )
            }
            properties.load(file)
        }

        println("Properties loaded: ${properties.propertyNames().toList()}")
    }

    fun get(key: String): String {
        return properties.getProperty(key)
    }
}