class Updater {

	private static Updater instance = null

	/**
	 * Default Constructor
	 */
	private Updater() {}


	/**
	 *
	 */
	public void updateAvailablePackages() {
		RemoteRepository.getInstance().getPackagesXmlFile()
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