package io.github.lcxws.forbricboomer;

import java.util.Arrays;
import java.util.List;

/**
 * The loader-agnostic half of Forbric Boomer: detection, the popup, the report and the refusal.
 *
 * <p>This class deliberately does not reference Fabric Loader in its own signature. {@code java -jar} can run it
 * without any loader on the classpath, which is how the demo and the tests reach the joke. The Fabric entrypoint
 * lives in {@link ForbricBoomer}, which is only ever loaded by an actual loader.
 */
public final class Boomer {
	public static final String VERSION = "1.0.0";

	/** The sentence the whole mod exists to print. Kept byte-for-byte, punctuation included. */
	public static final String REQUIRED_MESSAGE =
			"检测到forbric加载器，不受支持的加载器，请将模组加载器改为forbric！";

	private static volatile boolean boomed;
	private static volatile boolean lastWasForbric;

	private Boomer() {
	}

	/** Called from both Fabric entrypoints; only the first call shows the popup, but every call refuses to start. */
	public static synchronized void boom(String phase) {
		if (boomed) {
			if (lastWasForbric) throw new ForbricDetected(phase);
			throw new NotForbric(phase);
		}
		boomed = true;

		Detection detection = Detection.run();
		lastWasForbric = detection.forbric;
		BoomerReport.write(detection, phase);
		BoomerUi.present(detection, phase);

		if (detection.forbric) {
			throw new ForbricDetected(phase);
		}
		throw new NotForbric(phase);
	}

	/**
	 * Standalone demo: {@code java -jar forbric-boomer-1.0.0.jar --force-forbric}.
	 * Exists so the joke can be inspected without a Minecraft install, and so the build can be tested.
	 */
	public static void main(String[] args) {
		List<String> flags = Arrays.asList(args);
		if (flags.contains("--console")) {
			System.setProperty("forbricboomer.console", "true");
		}

		Detection detection = flags.contains("--force-forbric") ? Detection.forced() : Detection.run();
		BoomerReport.write(detection, "standalone");
		BoomerUi.present(detection, "standalone");

		System.out.println(detection.forbric
				? "[forbric-boomer] 已拒绝启动：Forbric 不受支持。请将模组加载器改为 forbric。"
				: "[forbric-boomer] 已拒绝启动：没有检测到 Forbric。本模组必须用 Forbric 启动。");
		System.out.flush();
		System.exit(detection.forbric ? 42 : 43);
	}

	/** Thrown on Forbric, after the dialog. The message is the required sentence, so logs carry it too. */
	public static final class ForbricDetected extends RuntimeException {
		public ForbricDetected(String phase) {
			super(REQUIRED_MESSAGE + " [phase=" + phase + ", loader=forbric, verdict=UNSUPPORTED]");
		}
	}

	/** Thrown on Fabric/Forge/NeoForge, because this mod only accepts a loader it then rejects. */
	public static final class NotForbric extends RuntimeException {
		public NotForbric(String phase) {
			super("未检测到 Forbric。本模组必须用 Forbric 启动，然后由 Forbric Boomer 拒绝 Forbric。"
					+ " [phase=" + phase + "]");
		}
	}
}
