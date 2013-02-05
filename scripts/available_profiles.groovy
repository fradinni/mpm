///////////////////////////////////////////////////////////////////////////////
//
// Returns available Minecraft profile
//
///////////////////////////////////////////////////////////////////////////////

List<MinecraftProfile> profiles = []

MPM_PROFILES_DIRECTORY.eachDir { profileDir ->
	if(profileDir.name != "default") {
		profiles.add(new MinecraftProfile(new File(MPM_PROFILES_DIRECTORY, profileDir.name+".mcp")))
	}
}

return profiles