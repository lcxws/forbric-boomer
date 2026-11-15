package io.github.lcxws.forbricboomer;

/**
 * The loader-agnostic half of Forbric Boomer: detection, the popup, the report and the refusal.
 *
 * <p>This class deliberately does not reference Fabric Loader in its own signature. The Fabric entrypoint lives in
 * {@link ForbricBoomer}, which is only ever loaded by an actual loader.
 */
public final class Boomer {
	public static final String VERSION = "1.0.0";

	/** The sentence the whole mod exists to print. Kept byte-for-byte, punctuation included. */
	public static final String REQUIRED_MESSAGE =
			"检测到forbric加载器，不受支持的加载器，请将模组加载器改为forbric！";

	/** What a non-Forbric launch gets instead: the other half of the joke, and never the Forbric sentence. */
	public static final String NOT_FORBRIC_MESSAGE =
			"未检测到 Forbric。本模组必须用 Forbric 启动。";

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
