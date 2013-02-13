///////////////////////////////////////////////////////////////////////////////
//
//
//
///////////////////////////////////////////////////////////////////////////////

// SET A GLOBAL VAR
GLOBAL = 1

// Set proxy
//System.properties.putAll( ["http.proxyHost":"proxy.intra.bt.com", "http.proxyPort":"8080"] )

// Set remote repository URL
//REMOTE_REPO_URL = 'C:\\Users\\WAXAYAZ\\Desktop\\MPM Workspace\\Repository'
//REMOTE_REPO_URL = 'C:\\Users\\605418644\\desktop\\MPM Workspace\\Repository'

REMOTE_REPO_URL = 'http://nfradin.fr/mpm/repo'

// Get user directory path
USER_DIRECTOY = new File(System.getProperty('user.home'))

// Set Minecraft install path
MINECRAFT_INSTALL_DIR = new File(System.getenv("APPDATA"), '.minecraft')

// Set MPM directory
MPM_DIRECTORY = new File(USER_DIRECTOY, '.mpm')

// Set Minecraft version file
MINECRAFT_VERSION_FILE = new File(MPM_DIRECTORY, "mcversion")

// Set MPM_REPO directory
MPM_REPO_DIRECTORY = new File(MPM_DIRECTORY, 'repository')

// Set MPM_PROFILES directory
MPM_PROFILES_DIRECTORY = new File(MPM_DIRECTORY, 'profiles')

// Set MPM_PROFILES_BACKUP directory
MPM_PROFILES_BACKUP_DIRECTORY = new File(MPM_PROFILES_DIRECTORY, 'default')

// Set file where active profile is stored
MPM_ACTIVE_PROFILE = new File(MPM_PROFILES_DIRECTORY, "000_active")

///////////////////////////////////////////////////////////////////////////////

backupDefaultMinecraftProfile = { dest ->
	println "-> Backup default Minecraft installation..."
	( new AntBuilder ( ) ).copy(toDir: dest) {
		fileset(dir : MINECRAFT_INSTALL_DIR)
	}
}

getMinecraftVersion = {
	return MINECRAFT_VERSION_FILE.text
}

getJavaVersion = {
	return System.getProperty("java.version")
}

InputStream.metaClass.eachByte = { int len, Closure c ->
	int read = 0
	byte[] buffer = new byte[ len ]
	while( ( read = delegate.read( buffer ) ) > 0 ) {
		c( buffer, read )
	}
}
 
File.metaClass.md5 = { ->
	def digest = java.security.MessageDigest.getInstance("MD5")
	delegate.withInputStream(){ ins ->
		ins.eachByte( 8192 ) { buffer, bytesRead ->
			digest.update( buffer, 0, bytesRead )
		}
	}
	new BigInteger( 1, digest.digest() ).toString( 16 ).padLeft( 32, '0' )
}

initCommonProperties = {

	if(!MPM_DIRECTORY.exists()) {
		MPM_DIRECTORY.mkdir()
	}

	if(!MINECRAFT_VERSION_FILE.exists()) {
		def shell = new GroovyShell() 
		shell.run(new File('scripts/get_mcversion.groovy')) 
	}

	if(!MPM_REPO_DIRECTORY.exists()) {
		MPM_REPO_DIRECTORY.mkdir()
	}

	if(!MPM_PROFILES_DIRECTORY.exists()) {
		MPM_PROFILES_DIRECTORY.mkdir()
	}

	if(!MPM_PROFILES_BACKUP_DIRECTORY.exists()) {
		backupDefaultMinecraftProfile(MPM_PROFILES_BACKUP_DIRECTORY)
	}

	if(!MPM_ACTIVE_PROFILE.exists()) {
		MPM_ACTIVE_PROFILE.write("default")
	}
}

////////////////////////////////////////////////////////////////////////////////
// SCRIPT START 

if(!MINECRAFT_INSTALL_DIR.exists()) {
	println "Unable to find Minecraft installation directory..."
	return
}

if(INIT_COMMON) {
	initCommonProperties() // Initialize Paths for common properties
}
