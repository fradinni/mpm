class ProfileInstallDatas {

	MinecraftProfile profile
	File file
	def xml

	public ProfileInstallDatas() {}

	public ProfileInstallDatas(Profile profile, File file) {
		this.file = file
		if(!file.exists()) {
			save()
		} else {
			loadXml()
		}
	}

	public void loadXml() {
		// Read Xml file
		xml = new XmlSlurper().parse(file)

				
	}

	public void save() {
		def xml = new StreamingMarkupBuilder().bind {
			"install-datas"() {
				profile(profile.name)
			}
		}
		
		file.write(XmlUtil.serialize(xml))
	}
}