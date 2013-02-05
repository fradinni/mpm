///////////////////////////////////////////////////////////////////////////////
//
//
//
///////////////////////////////////////////////////////////////////////////////

def activeProfileName = MPM_ACTIVE_PROFILE.text

def DEFAULT_PROFILE_DIR = new File(MPM_PROFILES_DIRECTORY, "default")
def CURRENT_PROFILE_DIR = new File(MPM_PROFILES_DIRECTORY, activeProfileName)


def getDefaultFile = { String relativePath, File file ->
	return new File(DEFAULT_PROFILE_DIR.absolutePath + "/" + relativePath + file.name)
}

def getProfileFile = { String relativePath, File file ->
	return new File(CURRENT_PROFILE_DIR.absolutePath + "/" + relativePath + file.name)
}


def ANT = new AntBuilder()

//
// Method - Iterate on each Minecraft file
//
backupMinecraftInstallModifications = { File parentDirectory, relativePath = "", files = [:] ->

	// Parse parent directory's files
	parentDirectory.eachFile { File file ->

		if(file.isFile()) {
			def defaultFile = getDefaultFile(relativePath, file)
			def profileFile = getProfileFile(relativePath, file)

			/*println "Default file: ${defaultFile.absolutePath}"
			println "Profile file: ${profileFile.absolutePath}\n"*/

			// If file not exist in profile and default
			if(!defaultFile.exists() && !profileFile.exists()) {
				// Backup file
				files.put(file.absolutePath, profileFile.absolutePath)
			}
			// If file exists in default and profile
			else if(defaultFile.exists() && profileFile.exists()) {
				// If checksum is different
				if(file.md5() != defaultFile.md5() && file.md5() != profileFile.md5() ) {
					// Backup file
					files.put(file.absolutePath, profileFile.absolutePath)
				}
			}

			// If file exits in default but not in profile
			else if(defaultFile.exists() && !profileFile.exists()) {
				// If checksum is different
				if(file.md5() != defaultFile.md5()) {
					// Backup file
					files.put(file.absolutePath, profileFile.absolutePath)
				}
			}
			// If file exits in profile but not in default
			else if(!defaultFile.exists() && profileFile.exists()) {
				// If checksum is different
				if(file.md5() != profileFile.md5()) {
					// Backup file
					files.put(file.absolutePath, profileFile.absolutePath)
				}
			}
		} 

		else if(file.isDirectory()){
			def defaultDir = getDefaultFile(relativePath, file)
			def profileDir = getProfileFile(relativePath, file)

			if(file.name != "bin") {
				if(!defaultDir.exists() && !profileDir.exists()) {
					profileDir.mkdirs()
				}

				// Iterate on folder's files
				files = backupMinecraftInstallModifications(file, file.name+"/", files)
			}
			
		}
	}

	return files
}

// Backup minecraft install directory modifications
backupMinecraftInstallModifications(MINECRAFT_INSTALL_DIR)