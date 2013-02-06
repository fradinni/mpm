import java.util.jar.JarEntry
import java.util.jar.JarFile

///////////////////////////////////////////////////////////////////////////////
//
// Install package in specified profile
//
///////////////////////////////////////////////////////////////////////////////

MinecraftProfile profile = installParams.profile
MinecraftPackage pkgDescriptor = installParams.pkgDescriptor
def ant = new AntBuilder()

println " -> Installing package '${pkgDescriptor.name}' (${pkgDescriptor.installType})..."


/*// Initialize install datas array for this profile
def INSTALL_DATAS = profile.installDatas

// Initialize package install datas
def PKG_INSTALL_DATAS_INDEX = profile.installDatas.packagesInstallDatas.size()+1
def PKG_INSTALL_DATAS = new PackageInstallDatas(pkgDescriptor, PKG_INSTALL_DATAS_INDEX)*/

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

	/*///////////////////////////////////////////////////////////////////////////////
	// Generate INSTALL DATAS

	print " -> Generate package installation datas... "

	// Load Minecraft Jar files
	def jarEntries =  []
	JarFile jar = new JarFile(new File(MPM_PROFILES_BACKUP_DIRECTORY, "bin/minecraft.jar"))
	Enumeration entries = jar.entries()
	while (entries.hasMoreElements()) {
    	JarEntry file = entries.nextElement()
    	PKG_INSTALL_DATAS.addFile(file.name)
    	//jarEntries.add(file)
	}

	println "[Done]"

	// Load mod files
	def modFiles = []
	def modFilesBase = tempJarDir.absolutePath
	tempJarDir.eachFileRecurse { File file ->
		if(file.isFile()) {
			modFiles.add(file.absolutePath.substring(modFilesBase.length()+1))
		}
	}*/

	//new File("modFiles.txt").write(modFiles.join('\n'))


	 /*java.io.File f = new java.io.File(destDir + java.io.File.separator + file.getName());
	    if (file.isDirectory()) { // if its a directory, create it
	    	f.mkdir();
	    	continue;
	    }
	    java.io.InputStream is = jar.getInputStream(file); // get the input stream
	    java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
	    while (is.available() > 0) {  // write contents of 'is' to 'fos'
	    	fos.write(is.read());
	    }
	    fos.close();
	    is.close();*/

	///////////////////////////////////////////////////////////////////////////////

	// Merge package files to JAR and exclude META-INF directory
	def srcMinecraftJAR = new File(MPM_PROFILES_DIRECTORY, profile.name+"/bin/minecraft.jar")
	if(!srcMinecraftJAR.exists()) {
		srcMinecraftJAR = new File(MPM_PROFILES_BACKUP_DIRECTORY, "bin/minecraft.jar")
	}
	ant.zip(destfile: new File(MPM_PROFILES_DIRECTORY, profile.name+"/bin/minecraft.jar.tmp")) {
		zipfileset(src: srcMinecraftJAR) {
			exclude(name: '''**/META-INF/**''')
		}
		fileset(dir: tempJarDir.absolutePath) {
			exclude(name: '''**/META-INF/**''')
		}			
	}

	new AntBuilder().move(file: new File(MPM_PROFILES_DIRECTORY, profile.name+"/bin/minecraft.jar.tmp"), toFile: new File(MPM_PROFILES_DIRECTORY, profile.name+"/bin/minecraft.jar"))
		
	// Delete temp directory and jar file
	ant.delete(dir: tempJarDir)	
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


// ARCHIVE EXTRACTION
else if(pkgDescriptor.installType == "extract") {
	File pkgArchive = new File(MPM_REPO_DIRECTORY, pkgDescriptor.packageFileURL)
	File destDir = new File(MPM_PROFILES_DIRECTORY, profile.name+"/"+pkgDescriptor.installDir)
	if(!destDir.exists()) {
		destDir.mkdir()
	}
	ant.unzip(src: pkgArchive, dest: destDir)
}

// Copy profile's files to Minecraft directory
new AntBuilder().copy(toDir: MINECRAFT_INSTALL_DIR, overwrite: true) {
	fileset(dir: new File(MPM_PROFILES_DIRECTORY, profile.name))
}

/*// Add package's install datas to Profile's install datas
INSTALL_DATAS.addPackageInstallDatas(PKG_INSTALL_DATAS)
INSTALL_DATAS.save()*/

return true