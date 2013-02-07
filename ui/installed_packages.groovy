MinecraftProfile activeProfile = new MinecraftProfile(new File(MPM_PROFILES_DIRECTORY, MPM_ACTIVE_PROFILE.text+".mcp"))
return activeProfile.dependencies as Object[]