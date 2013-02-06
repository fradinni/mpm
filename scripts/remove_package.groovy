import java.util.zip.ZipFile
import java.util.zip.ZipEntry
///////////////////////////////////////////////////////////////////////////////
//
// Remove package from specified profile
//
///////////////////////////////////////////////////////////////////////////////

MinecraftProfile profile = removeParams.profile
MinecraftPackage pkgDescriptor = removeParams.pkgDescriptor
MinecraftPackage parentPkgDescriptor = removeParams.parentPkgDescriptor

def ant = new AntBuilder()

println " -> Removing package '${pkgDescriptor.name}' (${pkgDescriptor.installType})..."

if(pkgDescriptor.installType == "native") {

	// Get profile others native packages
	def resolvedDependencies = []
	profile.dependencies.each { MinecraftPackageDescriptor dependency ->
		
		if(dependency.name != pkgDescriptor.name && dependency.name != parentPkgDescriptor?.name) {
			// Resolve local package
			resolveParams = [packageName: dependency.name, packageVersion: dependency.version, mcversion: dependency.mcversion, localOnly: true]
			ResolvedPackage resolvedPackage = evaluate(new File("scripts/resolve_package.groovy"))
			if(resolvedPackage == null) {
				println " X> Error, unable to find package '${pkgName}'"
				System.exit(1)
			}

			resolvedDependencies.add(resolvedPackage.descriptor)
		}
	}
	def nativeDependencies = resolvedDependencies.findAll { it.installType == "native" }

	// Replace Minecraft's modded JAR by original JAR
	ant.copy(
		file: new File(MPM_PROFILES_BACKUP_DIRECTORY, "bin/minecraft.jar"), 
		toFile: new File(profile.directory, "bin/minecraft.jar"),
		overwrite: true
	)

	// Re-Install other native dependencies by priority
	nativeDependencies?.sort{ it.priority }.each { MinecraftPackage nativeDependency ->
		installParams = [pkgDescriptor: nativeDependency, profile: profile]
		def success = evaluate(new File("scripts/install_package.groovy"))
		if(success) {
			//profileDependencies.add(dependency.descriptor)
			//profile.addDependency(dependency.descriptor)
		} else {
			println " X> Error, unable to re-install dependency '${nativeDependency.name}'"
			return false
		}
	} 
} 
else if(pkgDescriptor.installType == "copy") {

	// Get file to remove
	File fileToRemove = new File(MPM_PROFILES_DIRECTORY, profile.name+"/"+pkgDescriptor.installDir+"/"+pkgDescriptor.packageFileName)
	if(!fileToRemove.exists()) {
		println " X> Unable to find file: ${fileToRemove.absolutePath}"
		return false
	} 

	// Delete file
	ant.delete(file: fileToRemove)
}
else if(pkgDescriptor.installType == "extract") {

	File extractDirectory = new File(MPM_PROFILES_DIRECTORY, profile.name+"/"+pkgDescriptor.installDir)
	File pkgArchive = new File(MPM_REPO_DIRECTORY, pkgDescriptor.packageFileURL)

	if(!pkgArchive.exists()) {
		println " X> Unable to find file: ${pkgArchive.absolutePath}"
		return false
	} 

	// Open archive and retrieve files and directories
	def zipEntries =  []
	ZipFile zip = new ZipFile(pkgArchive)
	Enumeration entries = zip.entries()
	while (entries.hasMoreElements()) {
    	ZipEntry file = entries.nextElement()
    	zipEntries.add(file)
	}

	// Iterate on each file
	zipEntries.each { ZipEntry file ->

		// Delete it from profile
		File fileOnDisk = new File(extractDirectory, file.name)
		if(fileOnDisk.exists()) {
			if(file.name.contains('/')) {
				def parentDirName = file.name.substring(0,file.name.indexOf('/'))
				ant.delete(dir: new File(extractDirectory, parentDirName))
			} else {
				ant.delete(file: fileOnDisk)
			}
		}
	}
}

return true