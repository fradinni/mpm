///////////////////////////////////////////////////////////////////////////////
//
// Delete Minecraft profile
//
///////////////////////////////////////////////////////////////////////////////

def profileName = profileParams.name

try {

	// Check if profile directory exists
	def profileDir = new File(MPM_PROFILES_DIRECTORY, profileName)
	if(profileDir.exists()) {
		def prompt = System.console().readLine("> Profile '${profileName}' will be deleted ! Are you sure ? [y/n] ")
		if(prompt.toLowerCase() != 'y') {
			println " -> Cancelled."
			System.exit(1)
		}
	} else {
		println " -> Profile '${profileName}' was not found !"
		System.exit(1)
	}

	// If profile to delete is active profile, set active to default
	if(MPM_ACTIVE_PROFILE.text == profileName) {
		println " -> Active profile will be deleted.\n -> Set active profile to 'default'."
		MPM_ACTIVE_PROFILE.write("default")
	}

	// Delete profile directory
	new AntBuilder().delete(dir: profileDir)

	// Delete profile config file
	def profileConf = new File(MPM_PROFILES_DIRECTORY, "${profileName}.mcp")
	new AntBuilder().delete(file: profileConf)
} catch (Exception e) {
	println e.message
	return false
}

return true
