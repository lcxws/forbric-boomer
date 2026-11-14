package io.github.lcxws.forbricboomer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.fabricmc.loader.api.FabricLoader;

/** Looks for Forbric with several independent signals, then writes down every signal it saw. */
final class Detection {
	/** Classes that only the Forbric kernel ships. The branding class is the cheapest stable one. */
	private static final String[] MARKERS = {
			"net.forbric.kernel.util.ForbricBranding",
			"net.forbric.kernel.boot.KernelFabricLoader",
			"net.forbric.kernel.fabric.KernelFabricLoader"
	};

	final boolean forbric;
	final List<String> evidence;
	final String loaderClass;
	final String gameVersion;

	private Detection(boolean forbric, List<String> evidence, String loaderClass, String gameVersion) {
		this.forbric = forbric;
		this.evidence = evidence;
		this.loaderClass = loaderClass;
		this.gameVersion = gameVersion;
	}

	static Detection run() {
		List<String> evidence = new ArrayList<>();
		String loaderClass = "unknown";
		String gameVersion = "unknown";
		boolean marker = false;
		boolean modLoaded = false;

		try {
			FabricLoader loader = FabricLoader.getInstance();
			loaderClass = loader.getClass().getName();
			gameVersion = String.valueOf(loader.getRawGameVersion());
			evidence.add("FabricLoader 实现类 = " + loaderClass);
			try {
				modLoaded = loader.isModLoaded("forbric");
				evidence.add("isModLoaded(\"forbric\") = " + modLoaded);
			} catch (Throwable ignored) {
				evidence.add("isModLoaded(\"forbric\") 调用失败，忽略");
			}
		} catch (Throwable t) {
			evidence.add("FabricLoader API 不可用（" + t.getClass().getSimpleName() + "）");
		}

		String version = System.getProperty("forbric.version");
		if (version != null && !version.isBlank()) {
			evidence.add("系统属性 forbric.version = " + version);
		}

		for (String className : MARKERS) {
			try {
				Class.forName(className, false, Boomer.class.getClassLoader());
				marker = true;
				evidence.add("发现类 " + className);
			} catch (Throwable ignored) {
				// Not this one. The next signal still gets a chance.
			}
		}

		boolean forbric = marker || modLoaded || (version != null && !version.isBlank())
				|| loaderClass.toLowerCase(Locale.ROOT).contains("forbric");

		if (forbric) {
			evidence.add("结论：这就是 Forbric。");
		} else {
			evidence.add("结论：以上信号都没有出现，这不是 Forbric。");
		}

		return new Detection(forbric, List.copyOf(evidence), loaderClass, gameVersion);
	}
}

