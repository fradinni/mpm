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

	def availablePackagesList
	def installedPackagesList

	def MPM_ACTIVE_PROFILE

	public MainWindow() {
		initShell()
	}

	public initShell() {
		shellBinding = new Binding()
		shell = new GroovyShell(shellBinding);
		evaluate("_global.groovy", ["INIT_COMMON": 0, "OPTION_ARGUMENTS":[]])

		MPM_ACTIVE_PROFILE = shellBinding.getVariable("MPM_ACTIVE_PROFILE")
	}

	public evaluate(script, variables=null) {
		variables?.each { variable, value ->
			shell.setVariable(variable, value)
		}
		return shell.evaluate(new File("scripts/"+script))
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

	public void updateInstalledPackages(profileName) {
		def packages = InstalledPackages.refresh(profileName, shell)
		installedPackagesList.listData = packages ? packages : []
	} 

	public void updateAvailablePackages() {
		def availablePackages = AvailablePackages.refresh(shell)
		availablePackagesList.listData = availablePackages ? availablePackages/*.sort{a,b -> (a.type <=> b.type) ?: (a.name <=> b.name)}*/ : []
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
	    	swingBuilder.scrollPane(id:'installedPackagesScroll', constraints: BorderLayout.WEST) {
		    	installedPackagesList = list(fixedCellWidth: 360,
		    								 fixedCellHeight: 40,
		    								 cellRenderer: new StripeRenderer())
	    	}
	    }

	    def availablePackagesPanel = {
	    	swingBuilder.scrollPane(id: 'availablePackagesScroll', constraints: BorderLayout.WEST) {
		    	availablePackagesList = list(fixedCellWidth: 360,
		    								 fixedCellHeight: 40,
		    								 cellRenderer: new StripeRenderer())
	    	}
	    }



	    ///////////////////////////////////////////////////////////////////////
	    // Build Application
	    statusBar = new StatusBar()
	    statusBar.textWhenEmpty = "Ready."
		frame = swingBuilder.frame(title:"Minecraft Package Manager", defaultCloseOperation:JFrame.EXIT_ON_CLOSE, size:[800,500], show:true, locationRelativeTo: null) {

			// Set system look and feel
			lookAndFeel("system")

			// Add menu bar
			customMenuBar()

			// Build profiles menu
			buildProfilesMenu()

			mainPanel = panel() {
				borderLayout()
				panel(constraints: BorderLayout.NORTH) {
					label(text: "ok")
				}
				tabbedPane(id: 'tabs', constraints: BorderLayout.CENTER) {
					panel(title: 'Installed Packages', font: MinecraftFont.getFont()) {
						borderLayout()
						installedPackagesPanel()
					}
					panel(title: 'Available Packages') {
						borderLayout()
						availablePackagesPanel()
					}
				}
			}
    	}
    	mainPanel.add(statusBar, BorderLayout.SOUTH)
    	updateInstalledPackages(MPM_ACTIVE_PROFILE.text)
    	updateAvailablePackages()
    	//buildInstalledPackagesPanel(MPM_ACTIVE_PROFILE.text)
    	//buildAvailablePackagesPanel(MPM_ACTIVE_PROFILE.text)
	}
}