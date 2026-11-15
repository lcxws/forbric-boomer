package net.fabricmc.loader.api;

import java.nio.file.Path;

/**
 * Compile-time stand-in for Fabric Loader's public API type. The real type is an interface; keeping this an
 * interface matters at run time, because a class with the same name fails verification with
 * {@link IncompatibleClassChangeError} on an actual Fabric or Forbric launch.
 */
public interface FabricLoader {
	static FabricLoader getInstance() {
		throw new UnsupportedOperationException("compile-time stub");
	}

	boolean isModLoaded(String id);

	String getRawGameVersion();

	Path getGameDir();
}
