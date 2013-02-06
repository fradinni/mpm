///////////////////////////////////////////////////////////////////////////////
//
// Set active Minecraft profile
//
///////////////////////////////////////////////////////////////////////////////

def profileName = profileParams.name
def activeProfile = MPM_ACTIVE_PROFILE.text

def profileDir = new File(MPM_PROFILES_DIRECTORY, profileName)
if(!profileDir.exists()) {
	println " -> No Minecraft profile with name '${profileName}'..."
	return false
}

def noBackup = false

if(OPTION_ARGUMENTS.size() == 2 && OPTION_ARGUMENTS[1] == "--no-backup") {
	noBackup = true
}

if(activeProfile != "default" && !noBackup) {	
	def filesToCopy = evaluate(new File("scripts/backup_active_profile.groovy"))
	filesToCopy?.each { key, value ->
		new AntBuilder().copy(file: key, toFile: value, overwrite: true)
	}
}

// Delete original mineraft dir
def ant = new AntBuilder()
ant.delete(dir: MINECRAFT_INSTALL_DIR)

ant.copy(toDir: MINECRAFT_INSTALL_DIR) {
	fileset(dir: new File(MPM_PROFILES_DIRECTORY, 'default'))
}

if(profileName != 'default') {
	ant.copy(toDir: MINECRAFT_INSTALL_DIR, overwrite: true) {
		fileset(dir: new File(MPM_PROFILES_DIRECTORY, profileName))
	}	
}

// Save active profile
MPM_ACTIVE_PROFILE.write(profileName)

return true