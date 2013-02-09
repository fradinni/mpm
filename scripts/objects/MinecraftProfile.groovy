import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

class MinecraftProfile {

	def file
	def profileXml
	//ProfileInstallDatas installDatas

	List<MinecraftPackageDescriptor> dependencies = []

	public MinecraftProfile() {}

	public MinecraftProfile(file) {
		this.file = file
		this.profileXml = new XmlSlurper().parse(file)
		//this.installDatas = new ProfileInstallDatas(this)
		// Load dependencies descriptors
		loadDependencies()
	}

	private void loadDependencies() {
		if(this.profileXml.dependencies?.package?.size() > 0) {
			for(int i=0; i<this.profileXml.dependencies?.package?.size(); i++) {
				MinecraftPackageDescriptor dep = new MinecraftPackageDescriptor(this.profileXml.dependencies?.package[i])
				dependencies.add(dep)
			}
		}
	}

	public String getName() {
		return profileXml.name.text()
	}

	/*public ProfileInstallDatas getInstallDatas() {
		return new ProfileInstallDatas(this)
	}*/

	public String getMinecraftVersion() {
		return profileXml.mcversion.text()
	}

	public boolean hasDependency(dependency) {
		return ( dependencies.find{ it.name == dependency.name } != null )
	}

	public void addDependency(dependency) {
		if(!hasDependency(dependency)) {
			dependencies.add(dependency)
			println " -> Dependency '${dependency.name}' added to profile."
		} 
	}	

	public void removeDependency(dependency) {
		def dep = dependencies.find { it.name == dependency.name }
		dependencies.remove(dep)
	}
	
	public String toString() {
		return name
	}

	public void save() {
		def xml = new StreamingMarkupBuilder().bind {
			"mpm-profile"() {
				name(this.name)
				mcversion(getMinecraftVersion())
				dependencies() {
					this.dependencies.sort{ it.priority }.each { dependency ->
						'package'(
							name: dependency.name, 
							version: dependency.version, 
							mcversion: dependency.mcversion, 
							type: dependency.type,
							priority: dependency.priority
						)
					}
				}
			}
		}
		
		file.write(XmlUtil.serialize(xml))
	}


	public File getDirectory() {
		return new File(file.absolutePath.substring(0, file.absolutePath.lastIndexOf('.')))
	}
}