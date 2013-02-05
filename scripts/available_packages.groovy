///////////////////////////////////////////////////////////////////////////////
//
//
//
///////////////////////////////////////////////////////////////////////////////

// Get remote xml file
def xml
try {
	xml = new XmlParser().parse(REMOTE_REPO_URL+'/packages.xml')
} catch (Exception e) {
	println "Unable to connect to remote repository ! Please check you connection...\n"
	System.exit(1)
}

// Check if at least on minecraft version is defined
if(xml.minecraft?.size() < 1) {
	println "No packages available now..."
	System.exit(0)
}

def packages = [:]

if(OPTION_ARGUMENTS?.size()>0) {
	def version = OPTION_ARGUMENTS[0]
	def currentMinecraftVersion = xml.minecraft.find{ it.@version == version }

	if(currentMinecraftVersion != null) {

		def versionPackages = []

		// Iterate on version packages
		for(int packageIndex = 0; packageIndex < currentMinecraftVersion.package.size(); packageIndex++) {

			// Get current package 
			def currentPackage = currentMinecraftVersion.package[packageIndex]

			// Build package descriptor URL
			String packageDescriptorURL = REMOTE_REPO_URL + "/${currentPackage.@mcversion}/${currentPackage.@name}/${currentPackage.@version}/package.xml"

			// Get remote xml file
			def xmlDescriptor
			try {
				xmlDescriptor = new XmlParser().parse(packageDescriptorURL)
			} catch (Exception e) {
				println "Unable to connect to remote repository ! Please check you connection...\n"
				System.exit(1)
			}

			versionPackages.add(new MinecraftPackageDescriptor(xmlDescriptor))
		}

		// Add version packages to packages map
		packages.put(new String(currentMinecraftVersion.'@version'), versionPackages)

		return packages

	}
}

// Iterate on minecraft versions
for(int i=0; i<xml.minecraft.size(); i++) {

	def currentMinecraftVersion = xml.minecraft[i]
	def versionPackages = []

	// Iterate on version packages
	for(int packageIndex = 0; packageIndex < currentMinecraftVersion.package.size(); packageIndex++) {

		// Get current package 
		def currentPackage = currentMinecraftVersion.package[packageIndex]

		// Build package descriptor URL
		String packageDescriptorURL = REMOTE_REPO_URL + "/${currentPackage.@mcversion}/${currentPackage.@name}/${currentPackage.@version}/package.xml"

		// Get remote xml file
		def xmlDescriptor
		try {
			xmlDescriptor = new XmlParser().parse(packageDescriptorURL)
		} catch (Exception e) {
			println "Unable to connect to remote repository ! Please check you connection...\n"
			System.exit(1)
		}

		versionPackages.add(new MinecraftPackageDescriptor(xmlDescriptor))
	}

	// Add version packages to packages map
	packages.put(new String(currentMinecraftVersion.'@version'), versionPackages)
}

return packages