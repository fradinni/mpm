///////////////////////////////////////////////////////////////////////////////
//
// Find package in Remote Repository
//
// usage: 
// 
// findParams = [
// 		mcversion: '1.4.7',
//		packageName: 'minecraft-forge',
//		packageVersion: '6.6.0'
// ]
// MinecraftPackageDescriptor descriptor = evaluate(new File("find_remote_package.groovy"))
//
///////////////////////////////////////////////////////////////////////////////

// Get params
def mcversion = findParams.mcversion
def pkgName = findParams.packageName
def pkgVersion = findParams.packageVersion

// If only package name is specified
if(mcversion && pkgName && !pkgVersion) {
	// Get remote xml file
	def xmlPkgList
	try {
		xmlPkgList = new XmlParser().parse(REMOTE_REPO_URL+'/packages.xml')
	} catch (Exception e) {
		println "Unable to connect to remote repository ! Please check you connection...\n"
		System.exit(1)
	}

	def minecraftVersionNode = xmlPkgList.minecraft.find{ it.@version == mcversion }
	if(!minecraftVersionNode) {
		return null
	}

	def availablePkgVersions = minecraftVersionNode.findAll{ it.@name == pkgName }.sort{ it.@version }.reverse()
	if(!availablePkgVersions || availablePkgVersions.size() == 0) {
		return null
	}

	def resolvedPackage = availablePkgVersions[0]
	mcversion = resolvedPackage.@mcversion
	pkgname = resolvedPackage.@name
	pkgVersion = resolvedPackage.@version
}

// Build package descriptor file URL
def descriptorURL = "${mcversion}/${pkgName}/${pkgVersion}/package.xml"

// Get local file descriptor
def xml
try {
	if(REMOTE_REPO_URL.startsWith("http")) {
		xml = new XmlParser().parse(REMOTE_REPO_URL + "/" + descriptorURL)
	} else {
		xml = new XmlParser().parse(new File(REMOTE_REPO_URL, descriptorURL))
	}
} catch (Exception e) {
	//println "Unable to find Minecraft v${mcversion} package '${pkgName}' in remote reposirtory..."
	return null
}

return new MinecraftPackage(xml)