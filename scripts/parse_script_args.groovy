///////////////////////////////////////////////////////////////////////////////
//
//
//
///////////////////////////////////////////////////////////////////////////////

SCRIPT_ARGUMENTS = this.args // Get arguments passed to the MPM SCRIPT

// Create a new CliBuilder
SCRIPT = new CliBuilder(usage: "mpm [options] [params]", header: "Options")
SCRIPT.with {
	h longOpt: "help", "Display this message"

	l (longOpt: "list", 
	   argName: "mcVersion",
	   "List available Minecraft packages"
	)
	i (longOpt: "install", 
	  argName:"mcVersion:pkgName:pkgVersion",
	  'Install Minecraft package'
	)
	r (longOpt: "remove", "Remove Minecraft package")
	p (longOpt: "profile", 
	  argName: "profileName", 
	  "Set active profile or display active and available profiles"
	)
	cp (
	   longOpt: 'create-profile', 
	   argName: "profileName", 
	   "Create new Minecraft profile"
	)
	dp (
	   longOpt: 'delete-profile', 
	   argName: "profileName",
	   "Delete existing Minecraft profile"
	)
}

// Pasre SCRIPT arguments with CliBuilder
SCRIPT_OPTIONS = SCRIPT.parse(SCRIPT_ARGUMENTS)
if(!SCRIPT_OPTIONS) {
	System.exit(1)
}

// Parse arguments for current option
OPTION_ARGUMENTS = SCRIPT_OPTIONS.arguments()
