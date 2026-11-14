package net.fabricmc.loader.api;

import java.nio.file.Path;

/**
 * Compile-time stand-in for Fabric Loader's abstract class. The runtime class is the real one, from Forbric or
 * Fabric Loader; only these signatures are used, so no loader jar is needed to build this mod offline.
 */
public abstract class FabricLoader {
	public static FabricLoader getInstance() {
		throw new UnsupportedOperationException("compile-time stub");
	}

	public abstract boolean isModLoaded(String id);

	public abstract String getRawGameVersion();

	public abstract Path getGameDir();
}
