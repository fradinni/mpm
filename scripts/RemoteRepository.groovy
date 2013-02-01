class RemoteRepository {

	private static RemoteRepository instance = null
	public static String remoteUrl = "http://nfradin.fr/mpm/repo"


	/**
	 * Default Constructor
	 */
	public RemoteRepository() {}


	public List getAvailableRemotePackagesList() {

		def rootNode = new XmlParser().parse(remoteUrl+"/packages.xml")
		def packageNodes = rootNode.packages.package

		def packagesList = []
		for(int i=0; i<packageNodes.size(); i++) {
			packagesList.add( new MinecraftPackage(packageNodes[i]) )
		}

		packagesList = packagesList.sort{ it.name.toLowerCase() }

		return packagesList
	}


	public void displayAvailableRemotePackagesList() {
		println "\n[Remote] Minecraft available packages list:\n"

		def packagesList = getAvailableRemotePackagesList()
		packagesList.each { p ->
			println " ${p.name}\t\t${p.description}"
		}
	}

	public static RemoteRepository getInstance() {
		if(!instance) {
			instance = new RemoteRepository()
		}

		return instance
	}
}