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
	l longOpt: 'list', 'List availaible Minecraft packages'
	s longOpt: 'search', 'Search Minecraft packages'
	i longOpt: 'install', 'Install a Minecraft package'
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
// Script Methods
//
def update = {
	print "\n-> Updating available Minecraft packages... "
	Updater.updateAvailablePackages()
}


def list = {
	if(params && params[0] == 'local') {
		LocalRepository.getInstance().displayAvailableLocalPackagesList()
	} else {
		RemoteRepository.getInstance().displayAvailableRemotePackagesList()
	}
}


def search = {
	if(!params || !params[0]) {
		script.usage()
		return
	}

	LocalRepository.getInstance().displayAvailableLocalPackagesList(params[0])
}



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

if(options.l) {
	list()
}

if(options.s) {
	search()
}
