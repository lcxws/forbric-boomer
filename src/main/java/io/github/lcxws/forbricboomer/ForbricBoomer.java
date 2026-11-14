package io.github.lcxws.forbricboomer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

/**
 * The Fabric entrypoint. Forbric loads this class because it reads {@code fabric.mod.json}; the class then asks
 * {@link Boomer} to detect Forbric and refuse it. Both entrypoints are wired, because a loader that skips one
 * should still get the joke.
 */
public final class ForbricBoomer implements PreLaunchEntrypoint, ModInitializer {
	@Override
	public void onPreLaunch() {
		Boomer.boom("preLaunch");
	}

	@Override
	public void onInitialize() {
		Boomer.boom("main");
	}
}
