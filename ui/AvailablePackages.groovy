class AvailablePackages {

	static Object[] refresh(shell) {

		/*Binding shellBinding = new Binding()
		GroovyShell shell = new GroovyShell(shellBinding)
		shell.setVariable("INIT_COMMON", 0)
		shell.setVariable("OPTION_ARGUMENTS", [])
		shell.evaluate(new File("scripts/_global.groovy"))*/

		def packages
		def MINECRAFT_VERSION_FILE = shell.getVariable("MINECRAFT_VERSION_FILE")
		try {
			packages = shell.evaluate(new File("scripts/available_packages.groovy"))
		} catch (Exception e) {
			return e.message
			return null
		}

		return packages.get(MINECRAFT_VERSION_FILE.text) as Object[]
	}
}