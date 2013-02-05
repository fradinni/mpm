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

defaultProfileDir = new File(MPM_PROFILES_DIRECTORY, "default")
activeProfileDir = new File(MPM_PROFILES_DIRECTORY, activeProfile)

// Apply Diff
fileToCopy = [:]
def applyDiff (srcDir, path) {

	// Iterate on each file
	srcDir.eachFile { file ->
		if(file.isFile() && file.name != "minecraft.jar") {
			def defaultFile = new File(defaultProfileDir, path+"/"+file.name)
			def profileFile = new File(activeProfileDir, path+"/"+file.name)

			// If file doesn't exists in profile or his md5 is different, replace it in profile
			if(  (!defaultFile.exists() && ( !profileFile.exists() || (profileFile.md5() != file.md5()) )) ||
					(defaultFile.exists() && !profileFile.exists() && (defaultFile.md5() != file.md5()) ) ||
					(defaultFile.exists() && profileFile.exists() && (profileFile.md5() != file.md5())) ) {
				fileToCopy.put(file.absolutePath, profileFile.absolutePath)
			}
		}
	}

	// Iterate on each directory
	srcDir.eachDir { dir ->
		def defaultDirectory = new File(defaultProfileDir.absolutePath+"/"+path+"/"+dir.name)
		def profileDirectory = new File(activeProfileDir.absolutePath+"/"+path+"/"+dir.name)

		// If directory not exists in defaut and profile
		if(!defaultDirectory.exists() && !profileDirectory.exists()) {
			// Create it
			profileDirectory.mkdir()
		}

		// applyDiff on this sub directory
		applyDiff(new File(MINECRAFT_INSTALL_DIR.absolutePath + path + "/" + dir.name), path+"/"+dir.name)
	}
}

if(activeProfile != "default") {	
	applyDiff(MINECRAFT_INSTALL_DIR, "")
	fileToCopy.each { key, value ->
		new AntBuilder().copy(file: key, toFile: value, overwrite: true)
	}
}

// Delete original mineraft dir
def ant = new AntBuilder()
ant.delete(dir: MINECRAFT_INSTALL_DIR)

ant.copy(toDir: MINECRAFT_INSTALL_DIR, overwrite: true) {
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