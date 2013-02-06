import groovy.xml.StreamingMarkupBuilder

class PackageInstallDatas {

	String index
	String name
	String version
	String mcversion
	String type
	String installType

	public PackageInstallDatas(MinecraftPackage pkg, int index) {
		this.index = index
		this.name = pkg.name
		this.version = pkg.version
		this.mcversion = pkg.mcversion
		this.type = pkg.type
		this.installType = pkg.installType
	}

	public PackageInstallDatas(xml) {
		this.index = xml.@index
		this.name = xml.@name
		this.version = xml.@version
		this.mcversion = xml.@mcversion
		this.type = xml.@type
		this.installType = xml.@installType
	}


	public getXml() {
		return new StreamingMarkupBuilder().bind {
			"package-datas"(
				name: name, 
				version: version, 
				mcversion: mcversion, 
				type: type, 
				installType: installType,
				index: datas
			)
		}
	}

}