///////////////////////////////////////////////////////////////////////////////
//
// Resolve a package in local and remote repositories
//
///////////////////////////////////////////////////////////////////////////////

// Get params
def packageName = resolveParams.packageName
def packageVersion = resolveParams.packageVersion
def mcversion = resolveParams.mcversion ? resolveParams.mcversion : getMinecraftVersion()

// Check if dependency exists in local repo
findParams = [mcversion: mcversion, packageName: packageName, packageVersion: packageVersion]
MinecraftPackage resolvedPackage = evaluate(new File("scripts/find_local_package.groovy"))
if(resolvedPackage == null) {
	// Check if dependency exists in remote repository
	resolvedPackage = evaluate(new File("scripts/find_remote_package.groovy"))
	if(resolvedPackage == null) {
		//println " X> Error, unable to find dependency '${resolvedPackage.name}'"
		return null
	} else {
		//PACKAGES.put(dependency.name, [descriptor: dependency, location:"remote", installed: false])
		println " -> Dependency '${resolvedPackage.name}' was found in remote repository"
		return new ResolvedPackage(resolvedPackage, ResolvedPackage.LOCATION_REMOTE)
	}
} else {
	println " -> Dependency '${resolvedPackage.name}' was found in local repository"
	return new ResolvedPackage(resolvedPackage, ResolvedPackage.LOCATION_LOCAL)
	//PACKAGES.put(dependency.name, [descriptor: dependency, location:"local", installed: false])
}