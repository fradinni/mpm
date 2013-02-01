class LocalRepository {

	private static LocalRepository instance = null
	public File repositoryDir

	/**
	 * Default Constructor
	 */
	private LocalRepository(File mpmLocalDir) {
		repositoryDir = new File(mpmLocalDir,"repo")
		if(!repositoryDir.exists()) {
			repositoryDir.mkdir()
		}
	}


	public File getLocalPackagesXmlFile() {
		return new File(repositoryDir, "packages.xml")
	}


	public List getAvailableLocalPackagesList(nameFilter = null) {

		File xmlFile = getLocalPackagesXmlFile()
		def rootNode = new XmlParser().parse(xmlFile)
		def packageNodes = rootNode.packages.package

		def packagesList = []
		for(int i=0; i<packageNodes.size(); i++) {

			if( nameFilter && packageNodes[i].name.text().toLowerCase().contains(nameFilter.toLowerCase()) ) {
				packagesList.add( new MinecraftPackage(packageNodes[i]) )
			} 
			else if(!nameFilter) {
				packagesList.add( new MinecraftPackage(packageNodes[i]) )
			}
		}

		packagesList = packagesList.sort{ it.name.toLowerCase() }

		return packagesList
	}


	public void displayAvailableLocalPackagesList(nameFilter = null) {
		println "\n[Local] Minecraft available packages list :\n"

		def packagesList = getAvailableLocalPackagesList(nameFilter)
		packagesList.each { p ->
			println " ${p.name}\t\t${p.description}"
		}
	}


	public static LocalRepository getInstance() {
		if(!instance) {
			def userHomeDir = new File(System.getProperty('user.home'))
			def mpmLocalDir = new File(userHomeDir, ".mpm")
			instance = new LocalRepository(mpmLocalDir)
		}

		return instance
	}
}