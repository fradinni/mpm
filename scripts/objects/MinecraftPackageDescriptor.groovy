import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil

class MinecraftPackageDescriptor {

	String name
	String description
	String version
	String mcversion
	String type
	Integer priority

	public MinecraftPackageDescriptor() {}

	public MinecraftPackageDescriptor(xmlNode) {

		// Shared properties
		this.name = ( xmlNode.'@name' ? xmlNode.'@name' : xmlNode.name?.text() )
		this.description = ( xmlNode.'@description' ? xmlNode.'@description' : xmlNode.description?.text() )
		this.version = ( xmlNode.'@version' ? xmlNode.'@version' : xmlNode.version?.text() )
		this.mcversion = ( xmlNode.'@mcversion' ? xmlNode.'@mcversion' : xmlNode.mcversion?.text() )
		this.type = ( xmlNode.'@type' ? xmlNode.'@type' : xmlNode.type?.text() )

		if(xmlNode.'@priority' != null && xmlNode.'@priority'[0] != null) {
			this.priority = Integer.parseInt(xmlNode.'@priority'[0].toString())
		} else if(xmlNode.install.'@priority' != null && xmlNode.install.'@priority'[0] != null ) {
			this.priority = Integer.parseInt(xmlNode.install.'@priority'[0].toString())
		} else {
			this.priority = 10
		}

	}

	public getDescriptorXml() {
		def xml = new StreamingMarkupBuilder().bind {
			"package"(name: name, description: description, version: version, mcversion: mcversion, type: type, priority: priority)
		}
		return xml
	}

	public String getPackageFileName() {
		return "${name}-${version}--${mcversion}.${fileType}"
	}

	public String getPackageFileURL() {
		return "${mcversion}/${name}/${version}/" + getPackageFileName()
	}

	public String getPackageDescriptorURL() {
		return "${mcversion}/${name}/${version}/package.xml"
	}

}