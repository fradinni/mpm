USER_DIRECTOY = new File(System.getProperty('user.home'))
MPM_DIRECTORY = new File(USER_DIRECTOY, '.mpm')
MINECRAFT_INSTALL_DIR = new File(System.getenv("APPDATA"), '.minecraft')
MINECRAFT_VERSION_FILE = new File(MPM_DIRECTORY, "mcversion")

// Get groovy root class loader
def rootLoader = this.class.classLoader.rootLoader


File minecraftJAR = new File(MINECRAFT_INSTALL_DIR.absolutePath + "/bin/minecraft.jar")
if(!minecraftJAR.exists()) {
	println "Unable to load minecraft.jar !"
	return null
}

// Add minecraft JAR to loader classpath
rootLoader.addURL(minecraftJAR.toURI().toURL())

// Load class containing Minecraft version
B = Class.forName('b', true, rootLoader)

def version = B.newInstance().a()

MINECRAFT_VERSION_FILE.write(version)