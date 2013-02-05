///////////////////////////////////////////////////////////////////////////////
//
// Install package in specified profile
//
///////////////////////////////////////////////////////////////////////////////

MinecraftProfile profile = installParams.profile
MinecraftPackage pkgDescriptor = installParams.pkgDescriptor
def ant = new AntBuilder()

println " -> Installing package '${pkgDescriptor.name}' (${pkgDescriptor.installType})..."
// NATIVE INSTALLATION
if(pkgDescriptor.installType == "native") {

	File pkgArchive = new File(MPM_REPO_DIRECTORY, pkgDescriptor.packageFileURL)
	if(!pkgArchive.exists()) {
		println " X> Error, unable to find archive for packge '${pkgDescriptor.name}'"
		return false
	}


	// Create a temp directory to build new minecraft.jar
	File tempJarDir = new File(MPM_PROFILES_DIRECTORY, profile.name+"/_temp")
	if(tempJarDir.exists()) {
		ant.delete(dir: tempJarDir)
	}
	tempJarDir.mkdir()

	// Unzip package archive in temp dir
	ant.unzip(  src: pkgArchive.absolutePath,
	            dest: tempJarDir.absolutePath,
	            overwrite:"true")
			
	// Merge package files to JAR and exclude META-INF directory
	ant.zip(destfile: new File(MPM_PROFILES_DIRECTORY, profile.name+"/bin/minecraft.jar.tmp")) {
		zipfileset(src: new File(MPM_PROFILES_BACKUP_DIRECTORY, "bin/minecraft.jar")) {
			exclude(name: '''**/META-INF/**''')
		}
		fileset(dir: tempJarDir.absolutePath) {
			exclude(name: '''**/META-INF/**''')
		}			
	}

	new AntBuilder().move(file: new File(MPM_PROFILES_DIRECTORY, profile.name+"/bin/minecraft.jar.tmp"), toFile: new File(MPM_PROFILES_DIRECTORY, profile.name+"/bin/minecraft.jar"))
		
	// Delete temp directory and jar file
	ant.delete(dir: tempJarDir)
	//delete(file: new File(MPM_PROFILES_DIRECTORY, profile.name+"/minecraft.jar.tmp"))

	def newJar = new File(MPM_PROFILES_DIRECTORY, profile.name+"/bin/minecraft.jar")
	def oldJar = new File(MPM_PROFILES_BACKUP_DIRECTORY, "bin/minecraft.jar")

	println oldJar.size()
	println newJar.size()

	if(newJar.size() == oldJar.size()) {
		println " X> Error during patch of minecraft.jar ! NO PANIC :) Just try again..."
		return false
	}
	
} 

// DIRECTORY INSTALLATION
else if(pkgDescriptor.installType == "copy") {
	File pkgArchive = new File(MPM_REPO_DIRECTORY, pkgDescriptor.packageFileURL)
	File destDir = new File(MPM_PROFILES_DIRECTORY, profile.name+"/"+pkgDescriptor.installDir)
	if(!destDir.exists()) {
		destDir.mkdir()
	}
	ant.copy(file: pkgArchive, toDir: destDir)
}

// Copy profile's files to Minecraft directory
new AntBuilder().copy(toDir: MINECRAFT_INSTALL_DIR, overwrite: true) {
	fileset(dir: new File(MPM_PROFILES_DIRECTORY, profile.name))
}

return true