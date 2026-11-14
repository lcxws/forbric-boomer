package net.fabricmc.loader.api.entrypoint;

/** Compile-time stand-in. Forbric and Fabric Loader both ship the real interface under this name. */
public interface PreLaunchEntrypoint {
	void onPreLaunch();
}
