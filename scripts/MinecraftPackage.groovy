class MinecraftPackage extends MinecraftPackageDescriptor {
	
	def xml

	String author
	String website
	String fileType
	String originalFileName
	String checksum
	Date lastUpdate
	String installType
	String installDir
	List<MinecraftPackageDescriptor> dependencies

	public MinecraftPackage() {}

	public MinecraftPackage(xmlNode) {
		super(xmlNode)
		this.xml = xmlNode
		this.author = xmlNode.author?.text()
		this.website = xmlNode.website?.text()
		this.fileType = xmlNode.fileType?.text()
		this.originalFileName = xmlNode.originalFileName?.text()
		this.lastUpdate = xmlNode.lastUpdate ? Date.parse('yyyy/MM/dd hh:mm:ss', xmlNode.lastUpdate.text()) : null
		this.checksum = xmlNode.checksum?.text()
		this.installType = xmlNode.install?.@type[0]
		this.installDir = xmlNode.install?.@dir[0]

		// If package has dependecies
		if(xmlNode.dependencies?.package?.size() > 0) {

			// Initialize dependencies
			this.dependencies = []

			for(int i=0; i<xmlNode.dependencies?.package?.size(); i++) {
					this.dependencies.add(new MinecraftPackageDescriptor(xmlNode.dependencies.package[i]))
				}
		}
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