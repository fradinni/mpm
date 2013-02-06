///////////////////////////////////////////////////////////////////////////////
//
// Create new Minecraft profile
//
///////////////////////////////////////////////////////////////////////////////
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

def profileName = profileParams.name

def projectDescriptorFile = new File(MPM_PROFILES_DIRECTORY, "${profileName}.mcp")
def profileDir = new File(MPM_PROFILES_DIRECTORY, profileName)

if(profileDir.exists()) {
	println " -> A Minecraft profile with name '${profileName}' already exists..."
	return null
} else {

	// Prompt user
	String promptStr = "Create profile with name '${profileName}' ? [y/n]"
	def prompt = System.console().readLine(promptStr)
	if(prompt.toLowerCase() != "y") {
		System.exit(0)
	}

	try {
		// Create profile directory
		profileDir.mkdir();

		// Create project descriptor XML
		def xml = new StreamingMarkupBuilder().bind {
			"mpm-profile"() {
				name(profileName)
				mcversion(getMinecraftVersion())
				dependencies()
			}
		}
		projectDescriptorFile.write(XmlUtil.serialize(xml))

		/*new AntBuilder().copy(toDir: profileDir) {
			fileset(dir: MPM_PROFILES_BACKUP_DIRECTORY)
		}*/

	} catch (Exception e) {
		println e.message
		if(profileDir.exists()) {
			new AntBuilder().delete(dir: profileDir)
		}
		if(projectDescriptorFile.exists()) {
			new AntBuilder().delete(file: projectDescriptorFile)
		}
		return null
	}

	// Prompt user
	/*promptStr = "Set profile '${profileName}' as active ? [y/n]"
	prompt = System.console().readLine(promptStr)
	if(prompt.toLowerCase() != "y") {
		System.exit(0)
	}

	// Active profile
	profileParams = [name: profileName]
	success = evaluate(new File("scripts/set_active_profile.groovy"))
	if(!success) {
		println "Unable to set active profile !"
		System.exit(1)
	} else {
		println " -> Active profile was set to '${profileName}'"
	}*/
}

return new MinecraftProfile(new File(MPM_PROFILES_DIRECTORY, "${profileName}.mcp"))