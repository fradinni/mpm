class Downloader {

	/**
	 *
	 */
	public static void downloadMinecraftPackage(packageName) {

		// Check if connaction is available
		List packagesList = null
		try {
			Updater.updateAvailablePackages()
			packagesList = LocalRepository.getInstance().getAvailableLocalPackagesList()
		} catch (Exception e) {
			throw new Exception("Unable to connect to remote repository ! Please check your internet connection...")
		}
		
		// Check if package is available in list
		def packageToInstall = packagesList.find { it.name.toLowerCase() == packageName.toLowerCase() }
		if(!packageToInstall) {
			throw new Exception("Unable to find Minecraft package with name: '${packageName}'")
		}

		// Download package's files

	}

}