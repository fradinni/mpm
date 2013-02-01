class MinecraftPackage {

	public String name
	public String group
	public String description
	public String version
	public String author
	public String checksum
	public Date lastUpdated

	private Node rootNode

	public MinecraftPackage() {}

	public MinecraftPackage(Node xmlNodePackage) {
		this.name = xmlNodePackage.name.text()
		this.group = xmlNodePackage.group.text()
		this.version = xmlNodePackage.version.text()
		this.description = xmlNodePackage.description.text()
	}

	public MinecraftPackage(File xmlFile) {

	}
}