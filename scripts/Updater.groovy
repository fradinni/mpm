class Updater {

	private static Updater instance = null

	/**
	 * Default Constructor
	 */
	private Updater() {}



	/**
	 * Download and update local Packages XML file
	 */
	public static updateAvailablePackages() {
		
		// Get remote file and update local file
		def remoteXmlFile = RemoteRepository.remoteUrl + "/packages.xml"
		def localXmlFile = LocalRepository.getInstance().getLocalPackagesXmlFile()

		try {
			def fos = new FileOutputStream(localXmlFile)
			def out = new BufferedOutputStream(fos)
			out << new URL(remoteXmlFile).openStream()
			out.close()
			println "[Done]"
		} catch (Exception e) {
			println "Unable to update xml packages file...\n${e.message}"
			return
		}
	}


	/**
	 *
	 */
	public static Updater getInstance() {
		if(!instance) {
			instance = new Updater()
		}
		return instance
	}
}