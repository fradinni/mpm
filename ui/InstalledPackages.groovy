class InstalledPackages {

	static Object[] refresh(profileName, shell) {

		MinecraftProfile activeProfile
		try {
			// Retrieve variables from shell
			def MPM_PROFILES_DIRECTORY = shell.getVariable("MPM_PROFILES_DIRECTORY")
			def MPM_ACTIVE_PROFILE = shell.getVariable("MPM_ACTIVE_PROFILE")

			// Retrieve active profile
			activeProfile = new MinecraftProfile(new File(MPM_PROFILES_DIRECTORY, MPM_ACTIVE_PROFILE.text+".mcp"))
		} catch (Exception e) {
			return null
		}

		return activeProfile.dependencies as Object[]
	}
}