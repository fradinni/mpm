import groovy.xml.XmlUtil
///////////////////////////////////////////////////////////////////////////////
//
//
//
///////////////////////////////////////////////////////////////////////////////

def pkgToDownload = downloadParams.packageToDownload
def fileURL = REMOTE_REPO_URL + "/${pkgToDownload.packageFileURL}"
def destPath = MPM_REPO_DIRECTORY.absolutePath + "/${pkgToDownload.packageFileURL}"
def destDescriptor = MPM_REPO_DIRECTORY.absolutePath + "/${pkgToDownload.packageDescriptorURL}"
try {

	// Download package file
	println " -> Downloading package '${pkgToDownload.name}', please wait..."
	if(fileURL.startsWith("http")) {
		new File(destPath.substring(0, destPath.lastIndexOf('/'))).mkdirs()
		def file = new FileOutputStream(new File(destPath))
	    def out = new BufferedOutputStream(file)
	    out << new URL(fileURL).openStream()
	    out.close()
	} else {	
		( new AntBuilder ( ) ).copy(file: new File(fileURL) , tofile: new File(destPath))
	}

	// Save package xml descriptor
	new File(destDescriptor).write(XmlUtil.serialize(pkgToDownload.xml))

	// Add package to local repo packages list
	evaluate(new File("scripts/update_local_repo_xml.groovy"))

} catch (Exception e) {
	println e.message
	return null
}

return new MinecraftPackage(new XmlParser().parse(new File(destDescriptor)))