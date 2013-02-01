class RemoteRepository {

	private static RemoteRepository instance = null
	private String remoteUrl = "http://nfradin.fr/mpm/repo"


	/**
	 * Default Constructor
	 */
	public RemoteRepository() {}


	/**
	 *
	 */
	public File getPackagesXmlFile() {

		// Get remote file bytes
		def xmlUrl = remoteUrl + "/packages.xml"
		def file = new FileOutputStream(xmlUrl.tokenize("/")[-1])
		def out = new BufferedOutputStream(file)
		out << 
		out.close()

		File remoteFile = new URL(xmlUrl).openStream()
		def bytes = remoteFile.bytes
		if(!bytes) {
			println "Unable to read remove xml packages file..."
			return
		}
		
		// Update local file
		//LocalRepository.getInstance().updateLocalPackagesXmlFile(bytes)
	}


	public static RemoteRepository getInstance() {
		if(!instance) {
			instance = new RemoteRepository()
		}

		return instance
	}
}