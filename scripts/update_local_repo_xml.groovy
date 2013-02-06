import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

///////////////////////////////////////////////////////////////////////////////
// Script

print "Updating local repository... "

// Retrieve current path
def repository = MPM_REPO_DIRECTORY

// Init xml structure
def xml = new StreamingMarkupBuilder().bind {
	'mpm-packages' {

		// Iterate on Minecraft versions directories
		repository.eachDir { mcVersionDir ->
			minecraft(version: mcVersionDir.name) {

				// Iterate on packages directories for current
				// Minecraft version
				mcVersionDir.eachDir { packageDir ->
					
					// Iterate on each version of current package
					packageDir.eachDir { versionDir ->
						
						// Read current package xml descriptor
						def descriptorFile = new File(versionDir, 'package.xml')
						if(descriptorFile.exists()) {
							def descriptor = new XmlParser().parse(descriptorFile)

							// Build XML node for current package
							'package'(
								name: descriptor.name.text(),
								description: descriptor.description.text(),
								version: descriptor.version.text(),
								mcversion: descriptor.mcversion.text(),
								type: descriptor.type.text()
							)
						}
					}

				}
			}
		}

	}	
}

File outputFile = new File(repository, "packages.xml")
def result = XmlUtil.serialize(xml)
outputFile.write(result)
println "[Done]"
