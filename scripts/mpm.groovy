///////////////////////////////////////////////////////////////////////////////
//
//
//
///////////////////////////////////////////////////////////////////////////////

INIT_COMMON = 1
evaluate(new File("scripts/_global.groovy")) 	// Include common properties

///////////////////////////////////////////////////////////////////////////////

evaluate(new File("scripts/parse_script_args.groovy")) // Parse script arguments

///////////////////////////////////////////////////////////////////////////////


//
// Method - Resolve dependencies to install for package installation
// @return List<ResolvedPackage> resolvedDependencies
//
resolveDependenciesToInstallForPackage = { ResolvedPackage parentPackage ->
	
	List resolvedPackages=[]

	// Iterate on package dependencies
	parentPackage.descriptor.dependencies?.each { MinecraftPackageDescriptor dependency ->
		
		// Resolve dependency
		resolveParams = [packageName: dependency.name, packageVersion: dependency.version, mcversion: dependency.mcversion]
		ResolvedPackage resolvedPackage = evaluate(new File("scripts/resolve_package.groovy"))
		if(resolvedPackage == null) {
			println " X> Error, unable to find package '${pkgName}'"
			System.exit(1)
		}

		// Check if resolved dependency requires other dependencies
		resolvedPackages.addAll(resolveDependenciesToInstallForPackage(resolvedPackage))

		// Add resolved dependency to resolvedPackages list
		resolvedPackages.add(resolvedPackage)
	}

	return resolvedPackages
}


///////////////////////////////////////////////////////////////////////////////

println "===================================================="
println "= Minecraft Package Manager v1.0                   " 
println "= --------------------------------------------------"
println "= System properties:"
println "=   Minecraft version:  " + getMinecraftVersion()
println "=   Minecraft profile:  " + MPM_ACTIVE_PROFILE.text
println "=   Minecraft install:  " + MINECRAFT_INSTALL_DIR.absolutePath
println "=   JAVA version:       " + getJavaVersion()
println "=   Operating system:   " + System.getProperty("os.name")
println "===================================================="
println ""

// HELP
if(SCRIPT_OPTIONS.h) {
	SCRIPT.usage()
	System.exit(0)
}


// LIST AVAILABLE PACKAGES
if(SCRIPT_OPTIONS.l) {
	Map<String, List<MinecraftPackageDescriptor>> availablePackages = evaluate(new File("scripts/available_packages.groovy"))
	
	availablePackages.each { version, versionPackages ->

		def sortedPkg = []
		if(OPTION_ARGUMENTS && OPTION_ARGUMENTS.find{ it == "all-pkg-versions" }) {
			sortedPkg = versionPackages
		} 
		// Remove duplicate package entries
		else {
			def pkgNames = versionPackages*.name as Set
			pkgNames.each { name ->
				def pkg = versionPackages.find{it.name == name}
				if(pkg) sortedPkg.add(pkg)
			}
		}

		println "\n-> Minecraft v${version}:"	
		if(sortedPkg?.size() > 0) {
			sortedPkg = sortedPkg.sort{it.name}
			sortedPkg?.each { pkg ->
				if(OPTION_ARGUMENTS && OPTION_ARGUMENTS.find{ it == "all-pkg-versions" }) {
					println "\t- ${pkg.name} [v${pkg.version}]\t\t${pkg.description}"
				} else {
					println "\t- ${pkg.name}\t\t${pkg.description}"
				}
			}
		} else {
			println "\t- No available packages..."
		}
	}

	System.exit(0)
}


// SET/DISPLAY ACTIVE PROFILE
if(SCRIPT_OPTIONS.p) {
	// Set active profile
	if(OPTION_ARGUMENTS.size() > 0) {
		profileParams = [name: OPTION_ARGUMENTS[0]]
		def success = evaluate(new File("scripts/set_active_profile.groovy"))
		if(!success) {
			println "Unable to set active profile !"
			System.exit(1)
		} else {
			println " -> Active profile was set to '${OPTION_ARGUMENTS[0]}'"
		}
	} 
	// Display current profile
	else {
		println " -> Minecraft active profile: '${MPM_ACTIVE_PROFILE.text}'\n"
		println " -> Available profiles:\n"
		println "      - default\t\tMinecraft default profile"
		def profiles = evaluate(new File("scripts/available_profiles.groovy"))
		profiles?.each { MinecraftProfile profile ->
			println "      - ${profile.name}"
		}
	}

	System.exit(0)
}

// CREATE PROFILE
if(SCRIPT_OPTIONS.cp) {
	if(OPTION_ARGUMENTS.size()>0) {
		profileParams = [name: OPTION_ARGUMENTS[0]]
		MinecraftProfile profile = evaluate(new File("scripts/create_profile.groovy"))
		if(profile == null) {
			println "Error, unable to create profile '${OPTION_ARGUMENTS[0]}' !"
			System.exit(1)
		}

		println " -> Profile '${OPTION_ARGUMENTS[0]}' created !"
	} else {
		println "Please specify a profile name."
		System.exit(1)
	}

	System.exit(0)
}

// DELETE PROFILE
if(SCRIPT_OPTIONS.dp) {
	if(OPTION_ARGUMENTS.size()>0) {
		profileParams = [name: OPTION_ARGUMENTS[0]]
		def success = evaluate(new File("scripts/delete_profile.groovy"))
		if(!success) {
			println " -> Unable to delete profile '${OPTION_ARGUMENTS[0]}' !"
			System.exit(1)
		}

		println " -> Profile '${OPTION_ARGUMENTS[0]}' deleted !"
	} else {
		println "Please specify a profile name."
		System.exit(1)
	}

	System.exit(0)
}

// INSTALL
if(SCRIPT_OPTIONS.i) {
	
	// Get active profile as install profile
	def installProfileName = MPM_ACTIVE_PROFILE.text

	// Get mincraft version
	def mcversion = getMinecraftVersion()
	def pkgName = null
	def pkgVersion = null

	// Check if params are specified
	if(OPTION_ARGUMENTS.size() > 0) {
		def optionArguments = OPTION_ARGUMENTS[0].split(':', -1)
		if(optionArguments.size() == 1) {
			pkgName = optionArguments[0]
		}
		if(optionArguments.size() == 2) {
			pkgName = optionArguments[0]
			pkgVersion = optionArguments[1]
		}
		if(optionArguments.size() == 3) {
			mcversion = optionArguments[0]
			pkgName = optionArguments[1]
			pkgVersion = optionArguments[2]
		}

		// If install profile is specified, set it
		//if(OPTION_ARGUMENTS.size() == 2) {
		//	installProfileName = OPTION_ARGUMENTS[1]
		//}

		if(installProfileName == "default") {
			println " X> You cannot install package on 'default' profile"
			System.exit(1)
		}

		// Load profile
		MinecraftProfile profile = null
		try {
			profile = new MinecraftProfile(new File(MPM_PROFILES_DIRECTORY, installProfileName+".mcp"))
		} catch (Exception e) {
			println " X> Unable to find profile '${installProfileName}'"
			System.exit(1)
		}

		// Initialize packages map
		def PACKAGES = [:]

		// Resolve package
		resolveParams = [packageName: pkgName, packageVersion: pkgVersion, mcversion: mcversion]
		ResolvedPackage resolvedPackage = evaluate(new File("scripts/resolve_package.groovy"))
		if(resolvedPackage == null) {
			println " X> Error, unable to find package '${pkgName}'"
			System.exit(1)
		}

		// Get descriptor of resolved package
		MinecraftPackageDescriptor resolvedPackageDescriptor = resolvedPackage.descriptor

		// Check if package is already installed on current profile
		if(profile.hasDependency(resolvedPackageDescriptor)) {
			println " X> Package '${resolvedPackageDescriptor.name}' already installed on profile '${profile.name}'"
			System.exit(1)
		}

		// Check if package requires other dependencies
		List<ResolvedPackage> resolvedDependencies = resolveDependenciesToInstallForPackage(resolvedPackage)
		println "Dependencies to install"
		resolvedDependencies.findAll { !profile.hasDependency(it.descriptor) }?.each { ResolvedPackage dependency ->
			println dependency.name
		}

		/*
		// Check if package exists in local repo
		findParams = [mcversion: mcversion, packageName: pkgName, packageVersion: pkgVersion]
		MinecraftPackage pkgToInstall = evaluate(new File("scripts/find_local_package.groovy"))
		if(pkgToInstall == null) {
			// Check if package exists in remote repository
			pkgToInstall = evaluate(new File("scripts/find_remote_package.groovy"))

			if(pkgToInstall == null) {
				println " X> Error, unable to find package '${pkgName}'"
				System.exit(1)
			} else {
				PACKAGES.put(pkgToInstall.name, [descriptor: pkgToInstall, location:"remote", installed: profile.hasDependency(pkgToInstall)])
				println " -> Package '${pkgToInstall.mcversion}:${pkgToInstall.name}:${pkgToInstall.version}' was found in remote repository"
			}
		} else {
			println " -> Package '${pkgToInstall.mcversion}:${pkgToInstall.name}:${pkgToInstall.version}' was found in local repository"
			PACKAGES.put(pkgToInstall.name, [descriptor: pkgToInstall, location:"local", installed: profile.hasDependency(pkgToInstall)])
		}

		// Check if package is already installed on profile
		if(profile.hasDependency(pkgToInstall)) {
			println " X> Package '${pkgToInstall.mcversion}:${pkgToInstall.name}:${pkgToInstall.version}' already installed on profile '${profile.name}'"
			System.exit(1)
		}

		// Iterate on package dependencies
		pkgToInstall.dependencies?.each { MinecraftPackageDescriptor pkgDependency ->

			// Check if profile has dependency installed
			if(profile.hasDependency(pkgDependency)) {
				PACKAGES.put(pkgDependency.name, [descriptor: pkgDependency, location:"local", installed: true])
			} else {

				// Check if dependency exists in local repo
				findParams = [mcversion: pkgDependency.mcversion, packageName: pkgDependency.name, packageVersion: pkgDependency.version]
				MinecraftPackage dependency = evaluate(new File("scripts/find_local_package.groovy"))
				if(dependency == null) {
					// Check if dependency exists in remote repository
					dependency = evaluate(new File("scripts/find_remote_package.groovy"))

					if(dependency == null) {
						println " X> Error, unable to find dependency '${dependency.name}'"
						System.exit(1)
					} else {
						PACKAGES.put(dependency.name, [descriptor: dependency, location:"remote", installed: false])
						println " -> Dependency '${dependency.name}' was found in remote repository"
					}
				} else {
					println " -> Dependency '${dependency.name}' was found in local repository"
					PACKAGES.put(dependency.name, [descriptor: dependency, location:"local", installed: false])
				}
			}
		}

		// Check if dependencies should be downloaded
		def dependenciesToDownload = PACKAGES.findAll{ key, value -> 
			key != pkgToInstall.name && (value.location == "remote" || value.installed == false)
		}
		if(dependenciesToDownload != null && dependenciesToDownload.size() > 0){
			
			// Prompt user
			def dependenciesNames = []
			dependenciesToDownload.each { key, value ->
				dependenciesNames.add(value.descriptor.name + ":" + value.descriptor.version)
			}

			String promptStr = "> The package you want to install requires other packages [${dependenciesNames.join(', ')}]. "
			promptStr += "Would you like to download and install them ? [y/n] "

			def prompt = System.console().readLine(promptStr)
			if(prompt.toLowerCase() != "y") {
				println " -> Cancelled"
				System.exit(0)
			}
		}

		// Download required packages
		def packagesToDownload = PACKAGES.findAll{ key, value -> 
			value.location == "remote"
		}
		packagesToDownload?.each { key, value ->
			MinecraftPackage downloadedPackage = null
			downloadParams = [packageToDownload: value.descriptor]
			downloadedPackage = evaluate(new File("scripts/download_package.groovy"))
			if(downloadedPackage == null) {
				println " X> Error, unable to download package '${value.descriptor.mcversion}:${value.descriptor.name}:${value.descriptor.version}' !"
				println " X> Abord installation..."
				System.exit(1)
			}
		}

		// Install dependencies
		def dependenciesToInstall = PACKAGES.findAll{ key, value ->	
			key != pkgToInstall.name && value.installed == false 
		}?.values()*.descriptor
		dependenciesToInstall?.each { MinecraftPackageDescriptor descriptor ->
			installParams = [pkgDescriptor: descriptor, profile: profile]
			def success = evaluate(new File("scripts/install_package.groovy"))
			if(success) {
				profile.addDependency(descriptor)
			} else {
				println " X> Error, unable to install dependency '${descriptor.name}'"
				println " X> Abord installation"
				System.exit(1)
			}
		}

		// Install package
		installParams = [pkgDescriptor: pkgToInstall, profile: profile]
		def success = evaluate(new File("scripts/install_package.groovy"))
		if(success) {
			profile.addDependency(pkgToInstall)
		} else {
			println " X> Error, unable to install package '${pkgToInstall.name}'"
			println " X> Abord installation"
			System.exit(1)
		}

		// Save profile config file
		profile.save()
		*/
		System.exit(0)

	} 
	// If not, display installed packages for current profile
	else {
		SCRIPT.usage()
	}

	System.exit(0)
}

SCRIPT.usage()
