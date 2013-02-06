class ResolvedPackage {

	String name
	String version
	String mcversion
	String location
	boolean installed

	def descriptor

	static final String LOCATION_REMOTE = "remote"
	static final String LOCATION_LOCAL = "local"

	public ResolvedPackage() {}

	public ResolvedPackage(MinecraftPackageDescriptor descriptor, String location, Boolean installed = false) {
		this.name = descriptor.name
		this.version = descriptor.version
		this.mcversion = descriptor.mcversion
		this.location = location
		this.installed = installed

		this.descriptor = descriptor
	}
}