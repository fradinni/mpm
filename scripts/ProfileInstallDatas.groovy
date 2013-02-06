import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

class ProfileInstallDatas {

	MinecraftProfile profile
	File file
	def xml

	def packagesInstallDatas = []

	public ProfileInstallDatas(MinecraftProfile profile) {
		this.profile = profile
		this.file = new File(profile.directory, "install-datas.xml")
		if(!file.exists()) {
			save()
		}
		loadXml()
	}

	public addPackageInstallDatas(PackageInstallDatas packageInstallDatas) {
		packagesInstallDatas.add(packageInstallDatas)
	}

	public void loadXml() {
		// Read Xml file
		xml = new XmlSlurper().parse(file)
				
		packagesInstallDatas.clear()
		xml."package-datas".each { packageDatas ->
			packagesInstallDatas.add(new PackageInstallDatas(packageDatas))	
		}
	}

	public void save() {
		def xml = new StreamingMarkupBuilder().bind {
			"install-datas"(profile: profile.name) {
				packagesInstallDatas.each { PackageInstallDatas datas ->
					"package-datas"(
						name: datas.name, 
						version: datas.version, 
						mcversion: datas.mcversion, 
						type: datas.type, 
						installType: datas.installType, 
						index: datas.index
					)
				}
			}
		}
		
		file.write(XmlUtil.serialize(xml))
	}
}