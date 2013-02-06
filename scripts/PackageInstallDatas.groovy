import groovy.xml.StreamingMarkupBuilder

class PackageInstallDatas {

	String index
	String name
	String version
	String mcversion
	String type
	String installType

	List installedFiles = []

	// Create new PackageInstallDatas
	public PackageInstallDatas(MinecraftPackage pkg, int index) {
		this.index = index
		this.name = pkg.name
		this.version = pkg.version
		this.mcversion = pkg.mcversion
		this.type = pkg.type
		this.installType = pkg.installType
	}

	// Load existing PackageInstallDatas
	public PackageInstallDatas(xml) {
		this.index = xml.@index
		this.name = xml.@name
		this.version = xml.@version
		this.mcversion = xml.@mcversion
		this.type = xml.@type
		this.installType = xml.@installType

		xml.file?.each { jarFile ->
			installedFiles.add( jarFile.@name )
		}
	}


	public void addFile(String fileName) {
		installedFiles.add( fileName )
	}


	public getXml() {
		return new StreamingMarkupBuilder().bind {
			"package-datas"(
				name: name, 
				version: version, 
				mcversion: mcversion, 
				type: type, 
				installType: installType,
				index: index
			) {
				installedFiles.each { installedFile ->
					"file"(name: installedFiles)
				}
			}
		}
	}

}