///////////////////////////////////////////////////////////////////////////////
//
// Resolve a package in local and remote repositories
//
///////////////////////////////////////////////////////////////////////////////

// Get params
def packageName = resolveParams.packageName
def packageVersion = resolveParams.packageVersion
def mcversion = resolveParams.mcversion ? resolveParams.mcversion : getMinecraftVersion()
def localOnly = resolveParams.localOnly ? resolveParams.localOnly : false
def remoteOnly = resolveParams.remoteOnly ? resolveParams.remoteOnly : false

// Check if dependency exists in local and remoterepo 
findParams = [mcversion: mcversion, packageName: packageName, packageVersion: packageVersion]
MinecraftPackage localResolvedPackage = null
if(!remoteOnly) {
	localResolvedPackage = evaluate(new File("scripts/find_local_package.groovy"))
}
MinecraftPackage remoteResolvedPackage = null
if(!localOnly) {
	remoteResolvedPackage = evaluate(new File("scripts/find_remote_package.groovy"))
}

// If package was found in local and remote repo
if(localResolvedPackage != null && remoteResolvedPackage != null) {
	// If versions of local and remote package are different, return remote package
	if(remoteResolvedPackage.version != localResolvedPackage.version) {
		println " -> Dependency '${remoteResolvedPackage.name}' was found in remote repository"
		return new ResolvedPackage(remoteResolvedPackage, ResolvedPackage.LOCATION_REMOTE)
	} else {
		println " -> Dependency '${localResolvedPackage.name}' was found in local repository"
		return new ResolvedPackage(localResolvedPackage, ResolvedPackage.LOCATION_LOCAL)
	}
}
// If package was found on remote repo only
else if(localResolvedPackage == null && remoteResolvedPackage != null) {
	println " -> Dependency '${remoteResolvedPackage.name}' was found in remote repository"
	return new ResolvedPackage(remoteResolvedPackage, ResolvedPackage.LOCATION_REMOTE)
} 
// If package was found on local repo only
else if(localResolvedPackage != null) {
	return new ResolvedPackage(localResolvedPackage, ResolvedPackage.LOCATION_LOCAL)
}

return null 
