class LocalRepository {

	private static LocalRepository instance = null
	private File repositoryDir

	/**
	 * Default Constructor
	 */
	private LocalRepository(File mpmLocalDir) {
		repositoryDir = new File(mpmLocalDir,"repo")
		if(!repositoryDir.exists()) {
			repositoryDir.mkdir()
		}
	}


	/**
	 *
	 */
	public void updateLocalPackagesXmlFile(byte[] fileBytes) {
		File localFile = new File(repositoryDir, "packages.xml")
		localFile.setBytes(fileBytes)
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