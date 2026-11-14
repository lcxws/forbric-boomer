package net.fabricmc.api;

/** Compile-time stand-in. Forbric and Fabric Loader both ship the real interface under this name. */
public interface ModInitializer {
	void onInitialize();
}
