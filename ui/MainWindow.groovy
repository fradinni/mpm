import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import groovy.swing.*
import javax.swing.*
import java.awt.*

class MainWindow {

	def profilesMenu
	def shell
	def shellBinding
	def swingBuilder = new SwingBuilder()
	def frame
	def statusBar
	def mainPanel
	def currentProfilePanel

	def activeProfileLabel
	def installedPackagesDetailsEditor
	def availablePackagesDetailsEditor

	def availablePackagesList
	def installedPackagesList

	def selectedPackageToInstall
	def selectedPackageToRemove

	def MPM_ACTIVE_PROFILE
	def REMOTE_REPO_URL
	def MPM_PROFILES_DIRECTORY

	public MainWindow() {
		initShell()
	}

	public initShell() {
		shellBinding = new Binding()
		shell = new GroovyShell(shellBinding);
		evaluate("_global.groovy", ["INIT_COMMON": 1, "OPTION_ARGUMENTS":[]])

		MPM_ACTIVE_PROFILE = shellBinding.getVariable("MPM_ACTIVE_PROFILE")
		REMOTE_REPO_URL = shellBinding.getVariable("REMOTE_REPO_URL")
		MPM_PROFILES_DIRECTORY = shellBinding.getVariable("MPM_PROFILES_DIRECTORY")
	}

	public evaluate(script, variables=null, path=null) {
		path = path ? path : "scripts/"
		variables?.each { variable, value ->
			shell.setVariable(variable, value)
		}
		return shell.evaluate(new File(path+script))
	}


	public getActiveProfile() {
		MinecraftProfile profile = null
		try {
			profile = new MinecraftProfile(new File(MPM_PROFILES_DIRECTORY, MPM_ACTIVE_PROFILE.text +".mcp"))
		} catch (Exception e) {
			println " X> Unable to find profile '${MPM_ACTIVE_PROFILE.text}'"
			return null
		}

		return profile
	}

	public boolean setActiveProfile(profileName) {
		new Thread(new Runnable() {
			public void run() {
				statusBar.setStatusBarProgress("Backup active profile ('${MPM_ACTIVE_PROFILE.text}')...", 0, 3, 0)
				evaluate("backup_active_profile.groovy")
				statusBar.setStatusBarProgress("Activate profile '${profileName}'...", 0, 3, 1)
				def res = evaluate("set_active_profile.groovy", [ "profileParams": [
					 name: profileName,
					 noBackup: false
				]])
				statusBar.setStatusBarProgress("Activate profile '${profileName}'...", 0, 3, 2)
				buildProfilesMenu()
				updateInstalledPackages(profileName)
				updateAvailablePackages()
				activeProfileLabel.text = MPM_ACTIVE_PROFILE.text

				if(res) {
					statusBar.setStatusBarProgress("Profile '${profileName}' activated !", 0, 3, 3)
					JOptionPane.showMessageDialog(null, "Profile '${profileName}' is now active !")
				} else {
					statusBar.setStatusBarProgress("Unable to active profile '${profileName}' !", 0, 3, 3)
					JOptionPane.showMessageDialog(frame,
					    "Unable to activate profile '${profileName}' !",
					    "Error...",
					    JOptionPane.ERROR_MESSAGE);
				}
								
				statusBar.setStatusBarProgressFinished()
			}
		}).start()
	}

	public boolean deleteProfile(profileName) {

		def confirm = JOptionPane.showConfirmDialog(null,
    					"Delete profile '${profileName}' ?",
    					"Delete profile...",
    					JOptionPane.YES_NO_OPTION)

		if(confirm != null && confirm == JOptionPane.YES_OPTION) {
			new Thread(new Runnable() {
				public void run() {
					statusBar.setStatusBarProgress("Deleting profile '${profileName}'...", 0, 2, 1)

					boolean res = evaluate("delete_profile.groovy", [profileParams: [
						name: profileName,
						noPrompt: true
					]])

					statusBar.setStatusBarProgress("Deleting profile '${profileName}'...", 0, 2, 2)

					if(!res) {
						JOptionPane.showMessageDialog(frame,
					    "Unable to delete profile '${profileName}' !",
					    "Error...",
					    JOptionPane.ERROR_MESSAGE);
					} else {
						buildProfilesMenu()
						JOptionPane.showMessageDialog(null, "Profile '${profileName}' deleted !");
					}

					statusBar.setStatusBarProgressFinished()
				}
			}).start()
		}
	}

	public boolean createProfile() {
		def profileName = JOptionPane.showInputDialog(
                    null,
                    "Please enter a profile name :",
                    "New profile...",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "MyProfile1")

		if(profileName != null && profileName.trim() != "") {

			new Thread(new Runnable() {
				public void run() {
					statusBar.setStatusBarProgress("Creating profile '${profileName}'...", 0, 2, 1)

					boolean res = evaluate("create_profile.groovy", [profileParams: [
						name: profileName,
						noPrompt: true
					]])

					statusBar.setStatusBarProgress("Creating profile '${profileName}'...", 0, 2, 2)

					if(!res) {
						JOptionPane.showMessageDialog(frame,
					    "Unable to create profile '${profileName}' !",
					    "Error...",
					    JOptionPane.ERROR_MESSAGE);
					} else {
						buildProfilesMenu()
						JOptionPane.showMessageDialog(null, "Profile '${profileName}' created !");
					}

					statusBar.setStatusBarProgressFinished()
				}
			}).start()
		} else if(profileName != null) {
			JOptionPane.showMessageDialog(frame,
			    "Please enter a valid profile name !",
			    "Error...",
			    JOptionPane.ERROR_MESSAGE);
		}
	}

	//
	// Method - Resolve dependencies to install for a resolved package
	//
	// @return List<ResolvedPackage> resolvedDependencies
	//
	public resolveDependenciesToInstallForPackage(parentPackage) {
		
		def resolvedPackages=[]

		// Iterate on package dependencies
		parentPackage.descriptor.dependencies?.each { dependency ->
			
			// Resolve dependency
			def resolveParams = [packageName: dependency.name, packageVersion: dependency.version, mcversion: dependency.mcversion]
			def resolvedPackage = evaluate("resolve_package.groovy", ["resolveParams": resolveParams])
			if(resolvedPackage == null) {
				println " X> Error, unable to find package '${pkgName}'"
				return
			}

			// Check if resolved dependency requires other dependencies
			resolvedPackages.addAll(resolveDependenciesToInstallForPackage(resolvedPackage))

			// Add resolved dependency to resolvedPackages list if not already added
			if(	resolvedPackages.find{ it.descriptor.name != resolvedPackage.descriptor.name } == null) {
				resolvedPackages.add(resolvedPackage)
			}

			// If already added but version is more recent, add it to list
			else if(resolvedPackages.find{ it.descriptor.name == resolvedPackage.descriptor.name && 
					resolvedPackage.descriptor.version > it.descriptor.version }) {
				resolvedPackages.add(resolvedPackage)
			}
		}

		return resolvedPackages
	}

	public void installPackage() {
		new Thread(new Runnable() {
			public void run() {
				// Get active profile as install profile
				def installProfileName = MPM_ACTIVE_PROFILE.text

				def pkgName = selectedPackageToInstall?.name
				def pkgVersion = selectedPackageToInstall?.version
				def mcversion = selectedPackageToInstall?.mcversion

				statusBar.setStatusBarProgress("Installing package [${pkgName}:${pkgVersion}]...", 0, 10, 0)
				evaluate("backup_active_profile.groovy")

				if(installProfileName == "default") {
					println " X> You cannot install package on 'default' profile"
				}

				// Load profile
				MinecraftProfile profile = null
				try {
					profile = new MinecraftProfile(new File(MPM_PROFILES_DIRECTORY, installProfileName+".mcp"))
				} catch (Exception e) {
					println " X> Unable to find profile '${installProfileName}'"
					return
				}


		
				/////////////////////////////////////////////////////////////////////////////////////////
				// Start
				def confirm = JOptionPane.showConfirmDialog(null,
    					"Do you want to install package [${pkgName}:${pkgVersion}] ?",
    					"Install package...",
    					JOptionPane.YES_NO_OPTION)

				if(confirm == null || confirm == JOptionPane.NO_OPTION) {
					statusBar.setStatusBarProgressFinished()
					return
				}

				statusBar.setStatusBarProgress("Resolving package [${pkgName}:${pkgVersion}]...", 0, 5, 1)
				// Resolve package
				def resolveParams = [packageName: pkgName, packageVersion: pkgVersion, mcversion: mcversion]
				def resolvedPackage = evaluate("resolve_package.groovy", ["resolveParams":resolveParams])
				if(resolvedPackage == null) {
					println " X> Error, unable to find package '${pkgName}'"
					return
				}

				// Get descriptor of resolved package
				def resolvedPackageDescriptor = resolvedPackage.descriptor

				// Check if package is already installed on current profile
				if(profile.hasDependency(resolvedPackageDescriptor)) {
					println " X> Package '${resolvedPackageDescriptor.name}' is already installed on profile '${profile.name}'"
					return
				}

				statusBar.setStatusBarProgress("Resolve package dependencies...", 0, 5, 2)
				// Check if package requires other dependencies which are not already installed
				def resolvedDependencies = resolveDependenciesToInstallForPackage(resolvedPackage)
				def dependenciesToInstall = resolvedDependencies.findAll { !profile.hasDependency(it.descriptor) }
				if(dependenciesToInstall?.size() > 0) {

					def dependenciesNames = dependenciesToInstall.collect { it.descriptor.name }
					String promptStr = "The package you want to install requires dependencies to be installed [${dependenciesNames.join(', ')}].\n"
					promptStr += "Would you like to download and install them ?"
					confirm = JOptionPane.showConfirmDialog(null,
    					promptStr,
    					"Install dependencies...",
    					JOptionPane.YES_NO_OPTION)

					if(confirm == null || confirm == JOptionPane.NO_OPTION) {
						statusBar.setStatusBarProgressFinished()
						return
					}

					// Download each dependency
					dependenciesToInstall.findAll{ it.location == ResolvedPackage.LOCATION_REMOTE }?.each { dependency ->
						
						statusBar.setStatusBarProgress("Downloading dependency [${dependency?.descriptor?.name}:${dependency?.descriptor.version}] ...", 0, 5, 3)
						// Download it
						MinecraftPackage downloadedPackage = null
						def downloadParams = [packageToDownload: dependency.descriptor]
						downloadedPackage = evaluate("download_package.groovy", ["downloadParams": downloadParams])
						if(downloadedPackage == null) {
							println " X> Error, unable to download package '${dependency.descriptor.mcversion}:${dependency.descriptor.name}:${dependency.descriptor.version}' !"
							println " X> Abord installation..."
							return
						}
					}
				
				}


				statusBar.setStatusBarProgress("Downloading package [${resolvedPackageDescriptor?.name}:${resolvedPackageDescriptor.version}] ...", 0, 5, 3)
				// Download package
				def downloadedPackage = null
				def downloadParams = [packageToDownload: resolvedPackageDescriptor]
				downloadedPackage = evaluate("download_package.groovy", ["downloadParams" : downloadParams])
				if(downloadedPackage == null) {
					println " X> Error, unable to download package '${resolvedPackageDescriptor.mcversion}:${resolvedPackageDescriptor.name}:${resolvedPackageDescriptor.version}' !"
					println " X> Abord installation..."
					System.exit(1)
				}

				// Sort dependencies by install priority
				def dependenciesByPriority = dependenciesToInstall?.sort{ it.descriptor.priority }

				// Install each dependency by priority
				def profileDependencies = profile.dependencies
				dependenciesByPriority?.each { dependency ->
					statusBar.setStatusBarProgress("Install dependency [${dependency.descriptor.name}:${dependency.descriptor.version}] ...", 0, 5, 4)
					def installParams = [pkgDescriptor: dependency.descriptor, profile: profile]
					def success = evaluate("install_package.groovy", ["installParams" : installParams])
					if(success) {
						profileDependencies.add(dependency.descriptor)
						//profile.addDependency(dependency.descriptor)
					} else {
						println " X> Error, unable to install dependency '${descriptor.name}'"
						println " X> Abord installation"
						System.exit(1)
					}
				}

				statusBar.setStatusBarProgress("Install package [${resolvedPackageDescriptor.name}:${resolvedPackageDescriptor.version}] ...", 0, 5, 4)

				// Install package
				def installParams = [pkgDescriptor: resolvedPackageDescriptor, profile: profile]
				def success = evaluate("install_package.groovy", ["installParams" : installParams])
				if(success) {
					profile.addDependency(resolvedPackageDescriptor)
				} else {
					println " X> Error, unable to install dependency '${descriptor.name}'"
					println " X> Abord installation"
					System.exit(1)
				}

				// Save profile config file
				statusBar.setStatusBarProgress("Save active profile...", 0, 5, 5)
				profile.save()
				updateInstalledPackages(installProfileName)
				updateAvailablePackages()

				statusBar.setStatusBarProgressFinished()

				JOptionPane.showMessageDialog(null, "Package '${resolvedPackageDescriptor.name}' installed !");
			}
		}).start()
	}

	public void removePackage() {
		new Thread(new Runnable() {
			public void run() {
				// Get active profile as install profile
				def uninstallProfileName = MPM_ACTIVE_PROFILE.text

				def pkgName = selectedPackageToRemove?.name
				def pkgVersion = selectedPackageToRemove?.version
				def mcversion = selectedPackageToRemove?.mcversion


				if(uninstallProfileName == "default") {
					println " X> You cannot remove package from 'default' profile"
					return
				}

				// Load profile
				MinecraftProfile profile = null
				try {
					profile = new MinecraftProfile(new File(MPM_PROFILES_DIRECTORY, uninstallProfileName+".mcp"))
				} catch (Exception e) {
					println " X> Unable to find profile '${installProfileName}'"
					return
				}

				/////////////////////////////////////////////////////////////////////////////////////////
				// Start



				// Resolve package
				statusBar.setStatusBarProgress("Resolving package [${pkgName}:${pkgVersion}] ...", 0, 5, 0)
				def resolveParams = [packageName: pkgName, packageVersion: pkgVersion, mcversion: mcversion, localOnly: true]
				def resolvedPackage = evaluate("resolve_package.groovy", ["resolveParams" : resolveParams])
				if(resolvedPackage == null) {
					println " X> Error, unable to find package '${pkgName}'"
					return
				}

				// Get descriptor of resolved package
				def resolvedPackageDescriptor = resolvedPackage.descriptor

				// Check if package is already installed on current profile
				if(!profile.hasDependency(resolvedPackageDescriptor)) {
					println " X> Package '${resolvedPackageDescriptor.name}' is not installed on profile '${profile.name}'"
					return
				}

				// Test if profile has dependencies linked to this package
				def profilePackages = []
				def linkedPackages = []
				profile.dependencies.each { dependency ->

					// Resolve dependency in local repo
					statusBar.setStatusBarProgress("Resolving dependency [${dependency.name}:${dependency.version}] ...", 0, 5, 1)
					resolveParams = [packageName: dependency.name, packageVersion: dependency.version, mcversion: dependency.mcversion, localOnly: true]
					def resolvedDependency = evaluate("resolve_package.groovy", ["resolveParams" : resolveParams])
					if(resolvedDependency == null) {
						println " X> Error, unable to find package '${pkgName}'"
						return
					}

					if(dependency.name != resolvedPackage.name) {
						profilePackages.add(resolvedDependency)
						if(resolvedDependency.descriptor.dependencies.find { it.name == resolvedPackage.name }) {
							linkedPackages.add(resolvedDependency.descriptor)
						}
					}
				}

				// If dependencies are linked to this package, prompt user for delete
				if(linkedPackages.size() > 0) {
					def dependenciesNames = linkedPackages.collect { it.name +":"+it.version }
					String promptStr = "> Some Minecraft's dependencies require this package to work !\nPackage [${resolvedPackage.name}:${resolvedPackage.version}] and dependencies [${dependenciesNames.join(', ')}] will be REMOVED !\n"
					promptStr += "\nWould you like to continue ?"

					def confirm = JOptionPane.showConfirmDialog(null,
    					promptStr,
    					"Remove packge...",
    					JOptionPane.YES_NO_OPTION)

					if(confirm == null || confirm == JOptionPane.NO_OPTION) {
						statusBar.setStatusBarProgressFinished()
						return
					}
				} else {
					String promptStr = "> Package [${resolvedPackage.name}:${resolvedPackage.version}] will be REMOVED !\n"
					promptStr += "\nWould you like to continue ?"

					def confirm = JOptionPane.showConfirmDialog(null,
    					promptStr,
    					"Remove package...",
    					JOptionPane.YES_NO_OPTION)

					if(confirm == null || confirm == JOptionPane.NO_OPTION) {
						statusBar.setStatusBarProgressFinished()
						return
					}
				}


				// Use set active profile to backup minecraft install
				statusBar.setStatusBarProgress("Backup active profile ...", 0, 5, 2)
				evaluate("backup_active_profile.groovy")
		
				// Uninstall each linked package
				linkedPackages.each { linkedPackage ->
					statusBar.setStatusBarProgress("Remove dependency [${linkedPackage.name}:${linkedPackage.version}] ...", 0, 5, 2)
					def removeParams = [pkgDescriptor: linkedPackage, parentPkgDescriptor: resolvedPackage.descriptor, profile: profile]
					def success = evaluate("remove_package.groovy", ["removeParams" : removeParams])
					if(success) {
						profile.removeDependency(linkedPackage)
						println " -> Package [${linkedPackage.name}:${linkedPackage.version}] was removed from profile '${profile.name}'"
					} else {
						println " X> Error, unable to remove dependency '${linkedPackage.name}'"
						return
					}
				}

				// Uninstal package
				statusBar.setStatusBarProgress("Remove package [${resolvedPackage.name}:${resolvedPackage.version}] ...", 0, 5, 3)
				def removeParams = [pkgDescriptor: resolvedPackage.descriptor, profile: profile]
				def success = evaluate("remove_package.groovy", ["removeParams" : removeParams])
				if(success) {
					profile.removeDependency(resolvedPackage.descriptor)
				} else {
					println " X> Error, unable to remove package '${resolvedPackage.descriptor.name}'"
					return
				}

				// Use set active profile to update minecraft install
				statusBar.setStatusBarProgress("Update Mincraft installation ...", 0, 5, 4)
				def profileParams = [name: profile.name, noBackup: true]
				success = evaluate("set_active_profile.groovy", ["profileParams" : profileParams])
				if(!success) {
					println "Unable to update Minecraft install directory !"
					return
				}

				// Save profile
				statusBar.setStatusBarProgress("Save profile ...", 0, 5, 5)
				profile.save()

				updateInstalledPackages(uninstallProfileName)
				updateAvailablePackages()

				statusBar.setStatusBarProgressFinished()

				JOptionPane.showMessageDialog(null, "Package [${resolvedPackage.name}:${resolvedPackage.version}] was removed !");
			}
		}).start()
	}

	public void updateInstalledPackages(profileName) {
		def packages = InstalledPackages.refresh(profileName, shell)
		if(packages) {
			packages = packages.sort{a,b -> (a.type <=> b.type) ?: (a.name <=> b.name) ?: (b.version <=> a.version)}
		} else {
			def emptyPkg = new MinecraftPackageDescriptor()
			if(profileName == 'default') {
				emptyPkg.name = "Cannot install package on this profile..."
			} else {
				emptyPkg.name = "No package installed on this profile..."
			}
			emptyPkg.version = ""
			emptyPkg.type = "empty"
			packages = [emptyPkg]
		}
		installedPackagesList.listData = packages
	} 

	public void updateAvailablePackages() {
		def availablePackages = AvailablePackages.refresh(shell)
		if(availablePackages) {
			availablePackages = availablePackages.sort{a,b -> (a.type <=> b.type) ?: (a.name <=> b.name) ?: (b.version <=> a.version)}
		} else {
			def emptyPkg = new MinecraftPackageDescriptor()
			emptyPkg.name ="No more packages available..."
			emptyPkg.version = ""
			emptyPkg.type = "empty"
			availablePackages = [emptyPkg]
		}
		availablePackagesList.listData = availablePackages
	}

	public void buildProfilesMenu() {
		profilesMenu.removeAll()
		profilesMenu.add(swingBuilder.menuItem(text: "New profile...", mnemonic: 'N', actionPerformed: { createProfile() }))
		profilesMenu.add(swingBuilder.separator())
		def suffix = ""
		if(MPM_ACTIVE_PROFILE.text == "default") {
			suffix += "              [Active]"
		}
		profilesMenu.add(
			swingBuilder.menu(text: "Default Profile"+suffix, actionPerformed: { }) {
				if(MPM_ACTIVE_PROFILE.text != "default") {
					menuItem(text: "Active profile...", actionPerformed: { setActiveProfile("default") })
				}
			}
		)

		// Get profiles
		def profiles = evaluate(new File("available_profiles.groovy"))
		profiles?.each { profile ->
			suffix = ""
			if(MPM_ACTIVE_PROFILE.text == profile.name) {
				suffix += "                   [Active]"
			} 
			profilesMenu.add(
				swingBuilder.menu(text: profile.name + suffix, actionPerformed: { }) {
					if(MPM_ACTIVE_PROFILE.text != profile.name) {
						menuItem(text: "Active profile...", actionPerformed: { setActiveProfile(profile.name) })
					}
					menuItem(text: "Backup profile...", actionPerformed: { })
					if(MPM_ACTIVE_PROFILE.text != profile.name) {
						separator()
						menuItem(text: "Delete profile...", actionPerformed: { deleteProfile(profile.name) })
					}
				}

			)
		}
	}

	public void showAbout() {
		def text = ""
		text += "---------------------------------------------------------------\n"
		text += "                     Minecraft Package Manager GUI v1.0\n"
		text += "---------------------------------------------------------------\n"
		text += "\n"
		text += "                                Author:  Nicolas FRADIN\n"
		text += "                               Website: http://nfradin.fr\n"
		text += "\n"
		text += "                                Release date: 2013/02/08\n"
		text += "\n"
		text += "---------------------------------------------------------------\n"
		JOptionPane.showMessageDialog(frame,
    		text,
    		"About...",
    	JOptionPane.PLAIN_MESSAGE);
	}

	public void show() {
		
		def instalButton

		///////////////////////////////////////////////////////////////////////
		// Application Components

		// Method - Creates menu bar
		def customMenuBar = {
			swingBuilder.menuBar {
				menu(text: "File", mnemonic: 'F') {
					menuItem(text: "Exit", mnemonic: 'X', actionPerformed: { dispose() })
				}
				profilesMenu = swingBuilder.menu(id: "profilesMenu", text: "Profiles", mnemonic: 'P')
				menu(text: "Help", mnemonic: 'H') {
					menuItem(text: "About", mnemonic: 'A', actionPerformed: { showAbout() })
				}
			}  
	    }

	    def installedPackagesPanel = {
	    	swingBuilder.scrollPane(constraints: BorderLayout.WEST, horizontalScrollBarPolicy: ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
		    	installedPackagesList = list(fixedCellWidth: 360,
		    								 fixedCellHeight: 40,
		    								 cellRenderer: new StripeRenderer())
	    	}
	    	installedPackagesList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent evt) {
					swingBuilder.removeButton.setVisible(false)
					def selection = installedPackagesList.getSelectedValue()
					selectedPackageToRemove = selection
					def url
					if(REMOTE_REPO_URL.startsWith("http")) {
						url = new URL(REMOTE_REPO_URL + "/" + selection?.packageDetailsURL)
					} else {
						url = new URL("file:///" + REMOTE_REPO_URL + "/" + selection?.packageDetailsURL)
					}
					try {
					    URLConnection conn = url.openConnection();
					    conn.connect();
					    installedPackagesDetailsEditor.setPage(url)
					} catch (Exception e) {
						println "error: ${e.message}"
					    installedPackagesDetailsEditor.setText("No details available for this package...")
					}
					if(selection) {
						swingBuilder.removeButton.setVisible(true)
					}
				}
	    	});
	    }

	    def availablePackagesPanel = {
	    	swingBuilder.scrollPane(constraints: BorderLayout.WEST, horizontalScrollBarPolicy: ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
		    	availablePackagesList = list(fixedCellWidth: 360,
		    								 fixedCellHeight: 40,
		    								 cellRenderer: new StripeRenderer(this))
	    	}
	    	availablePackagesList.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent evt) {
					swingBuilder.installButton.setVisible(false)
					def selection = availablePackagesList.getSelectedValue()
					selectedPackageToInstall = selection
					def url
					if(REMOTE_REPO_URL.startsWith("http")) {
						url = new URL(REMOTE_REPO_URL + "/" + selection?.packageDetailsURL)
					} else {
						url = new URL("file:///" + REMOTE_REPO_URL + "/" + selection?.packageDetailsURL)
					}
					try {
					    URLConnection conn = url.openConnection();
					    conn.connect();
					    availablePackagesDetailsEditor.setPage(url)
					} catch (Exception e) {
					    availablePackagesDetailsEditor.setText("No details available for this package...")
					} 
					if(selection && !getActiveProfile().hasDependency(selection)) {
						swingBuilder.installButton.setVisible(true)
					}
				}
	    	});
	    }


	    def installedPackagesDetailsPanel = {
	    	swingBuilder.panel(constraints: BorderLayout.CENTER) {
	    		borderLayout()
		    	scrollPane(constraints: BorderLayout.CENTER, horizontalScrollBarPolicy: ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
		    		installedPackagesDetailsEditor = editorPane()
		    		installedPackagesDetailsEditor.setEditable(false)
		    	}
		    	removeButtonPanel = panel(constraints: BorderLayout.SOUTH) {
	    			//boxLayout(axis:BoxLayout.PAGE_AXIS)
	    			removeButton= button(id: 'removeButton', text: "Remove package...", alignmentX: Component.CENTER_ALIGNMENT, actionPerformed:{ 
				          // Button click
				          removePackage()
				    })
	    			removeButton.setPreferredSize(new Dimension(640,20))
	    			removeButton.setVisible(false)
	    		}
	    		removeButtonPanel.setBackground(Color.WHITE)
		    }
	    }

	    def availablePackagesDetailsPanel = {
	    	swingBuilder.panel(constraints: BorderLayout.CENTER) {
	    		borderLayout()
	    		scrollPane(constraints: BorderLayout.CENTER, horizontalScrollBarPolicy: ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER) {
		    		availablePackagesDetailsEditor = editorPane()
		    		availablePackagesDetailsEditor.setEditable(false)
	    		}
	    		installButtonPanel = panel(constraints: BorderLayout.SOUTH) {
	    			//boxLayout(axis:BoxLayout.PAGE_AXIS)
	    			installButton = button(id: 'installButton', text: "Install package...", alignmentX: Component.CENTER_ALIGNMENT, actionPerformed:{ 
				          // Button click
				          installPackage()
				    })
	    			installButton.setPreferredSize(new Dimension(640,20))
	    			installButton.setVisible(false)
	    		}
	    		installButtonPanel.setBackground(Color.WHITE)
	    	}
	    }



	    ///////////////////////////////////////////////////////////////////////
	    // Build Application
	    statusBar = new StatusBar()
	    statusBar.textWhenEmpty = "Ready."
		frame = swingBuilder.frame(title:"Minecraft Package Manager v0.9beta", resizable: false, defaultCloseOperation:JFrame.EXIT_ON_CLOSE, size:[1024,700], show:true, locationRelativeTo: null) {

			// Set system look and feel
			lookAndFeel("system")

			// Add menu bar
			customMenuBar()

			// Build profiles menu
			buildProfilesMenu()

			mainPanel = panel() {
				borderLayout()
				panel(constraints: BorderLayout.NORTH) {
					borderLayout()
					label(icon: new ImageIcon("ui/images/logo_banner_1024.png"), constraints: BorderLayout.NORTH)
					panel(constraints: BorderLayout.CENTER) {
						activeProfileLabel = label(text: MPM_ACTIVE_PROFILE.text, font: MinecraftFont.getFont(30))
						activeProfileLabel.setBorder(BorderFactory.createEmptyBorder(12,0,0,0))
						activeProfileLabel.setForeground(new Color(44, 127, 204))
					}
				}
				tabbedPane(id: 'tabs', constraints: BorderLayout.CENTER) {
					panel(title: 'Installed Packages') {
						borderLayout()
						installedPackagesPanel()
						installedPackagesDetailsPanel()
					}
					panel(title: 'Available Packages') {
						borderLayout()
						availablePackagesPanel()
						availablePackagesDetailsPanel()
					}
				}
			}
    	}
    	mainPanel.add(statusBar, BorderLayout.SOUTH)
    	updateInstalledPackages(MPM_ACTIVE_PROFILE.text)
    	updateAvailablePackages()
	}
}