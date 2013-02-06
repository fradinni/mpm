///////////////////////////////////////////////////////////////////////////////
//
// Find package in Local Repository
//
// usage: 
// 
// findParams = [
// 		mcversion: '1.4.7',
//		packageName: 'minecraft-forge',
//		packageVersion: '6.6.0'
// ]
// MinecraftPackageDescriptor descriptor = evaluate(new File("find_local_package.groovy"))
//
///////////////////////////////////////////////////////////////////////////////

// Get params
def mcversion = findParams.mcversion
def pkgName = findParams.packageName
def pkgVersion = findParams.packageVersion

// Build package descriptor file URL
def descriptorURL = "${mcversion}/${pkgName}/${pkgVersion}/package.xml"

// Get local file descriptor
def xml
try {
	xml = new XmlParser().parse(new File(MPM_REPO_DIRECTORY, descriptorURL))
} catch (Exception e) {
	println "Unable to find Minecraft v${mcversion} package '${pkgName}' in local repository...\n${e.message}"
	return null
}

return new MinecraftPackage(xml)