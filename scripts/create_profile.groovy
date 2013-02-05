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
	println " -> A Minecraft profile with name '${profileName} already exists..."
} else {

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
	} catch (Exception e) {
		if(profileDir.exists()) {
			new AntBuilder().delete(dir: profileDir)
		}
		if(projectDescriptorFile.exists()) {
			new AntBuilder().delete(file: projectDescriptorFile)
		}
		return null
	}
}

return new MinecraftProfile(new File(MPM_PROFILES_DIRECTORY, "${profileName}.mcp"))