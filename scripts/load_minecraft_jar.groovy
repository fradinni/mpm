import java.util.jar.JarInputStream
import java.util.jar.JarEntry
///////////////////////////////////////////////////////////////////////////////
//
//
//
///////////////////////////////////////////////////////////////////////////////
INIT_COMMON = 0
evaluate(new File("_global.groovy"))

// Get groovy root class loader
def rootLoader = this.class.classLoader.rootLoader

// Add minecraft JAR to loader classpath
File minecraftJAR = new File(MINECRAFT_INSTALL_DIR.absolutePath + "/bin/minecraft.jar")
if(!minecraftJAR.exists()) {
	println "Unable to load minecraft.jar !"
	return null
}
rootLoader.addURL(minecraftJAR.toURI().toURL())

// Load class containing Minecraft version
B = Class.forName('b', true, rootLoader)
def version = B.newInstance().a
