///////////////////////////////////////////////////////////////////////////////
//
//
//
///////////////////////////////////////////////////////////////////////////////

def cmdArguments = this.args;

// Get user directory path
def userHomeDir = new File(System.getProperty('user.home'))
def mpmLocalDir = new File(userHomeDir, ".mpm")
if(!mpmLocalDir.exists()) {
	mpmLocalDir.mkdir()
}

// Ceate new CliBuilder
def script = new CliBuilder(usage: 'mpm -[acdhrsu] [params]')
script.with {
	h longOpt: 'help', 'Display mpm usage...'
	u longOpt: 'update', 'Update available Minecraft packages'
	s longOpt: 'search', 'Search Minecraft packages'
	a longOpt: 'add', 'Add Minecraft package'
	r longOpt: 'remove', 'Remove a Minecraft package'
	c longOpt: 'create', 'Create a Minecraft package'
	d longOpt: 'deploy', 'Deploy a Minecraft package'
}

// Get script options
def options = script.parse(cmdArguments)
if(!options || cmdArguments.size() == 0) {
	script.usage()
	return
}

// Get extra arguments
def params = options.arguments()





///////////////////////////////////////////////////////////////////////////////
// Script Options
//

if(options.h) {
	script.usage()
	return
}

if(options.u) {
	update()
}


///////////////////////////////////////////////////////////////////////////////
// Script Methods
//
def update() {
	println "Updating available Minecraft packages..."
	Updater.getInstance().updateAvailablePackages()
}