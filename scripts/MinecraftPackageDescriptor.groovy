class MinecraftPackageDescriptor {

	String name
	String description
	String version
	String mcversion
	String type

	public MinecraftPackageDescriptor() {}

	public MinecraftPackageDescriptor(xmlNode) {

		// Shared properties
		this.name = ( xmlNode.'@name' ? xmlNode.'@name' : xmlNode.name?.text() )
		this.description = ( xmlNode.'@description' ? xmlNode.'@description' : xmlNode.description?.text() )
		this.version = ( xmlNode.'@version' ? xmlNode.'@version' : xmlNode.version?.text() )
		this.mcversion = ( xmlNode.'@mcversion' ? xmlNode.'@mcversion' : xmlNode.mcversion?.text() )
		this.type = ( xmlNode.'@type' ? xmlNode.'@type' : xmlNode.type?.text() )

	}
}