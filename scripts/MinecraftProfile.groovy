import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

class MinecraftProfile {

	def file
	def profileXml

	List<MinecraftPackageDescriptor> dependencies = []

	public MinecraftProfile() {}

	public MinecraftProfile(file) {
		this.file = file
		this.profileXml = new XmlSlurper().parse(file)
		
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

	public File getInstallDatas() {
		def installDatasFile = new File(MPM_PROFILES_DIRECTORY, name + "/pkg-install-datas.xml")
		if(!installDatasFile.exists()) {
			installDatasFile = new ProfileInstallDatas(installDatasFile)
		}
		return installDatasFile
	}

	public String getMinecraftVersion() {
		return profileXml.mcversion.text()
	}

	public boolean hasDependency(MinecraftPackageDescriptor dependency) {
		return ( dependencies.find{ it.name == dependency.name } != null )
	}

	public void addDependency(MinecraftPackageDescriptor dependency) {
		if(!hasDependency(dependency)) {
			dependencies.add(dependency)
			println " -> Dependency '${dependency.name}' added to profile."
		} 
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
					this.dependencies.each { dependency ->
						'package'(
							name: dependency.name, 
							version: dependency.version, 
							mcversion: dependency.mcversion, 
							type: dependency.type
						)
					}
				}
			}
		}
		
		file.write(XmlUtil.serialize(xml))
	}


	public static MinecraftProfile getProfileForName(String profileName) {
		return new MinecraftProfile(new File(MPM_PROFILES_DIRECTORY, profileName+".mcp"))
	}
}