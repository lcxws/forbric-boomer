import io.github.lcxws.forbricboomer.ForbricBoomer;

/** Runs the real preLaunch entrypoint without Minecraft, and reports what it threw. */
public final class EntrypointHarness {
	public static void main(String[] args) {
		System.setProperty("forbricboomer.console", "true");
		System.setProperty("forbricboomer.autoclose", "1");
		try {
			new ForbricBoomer().onPreLaunch();
			System.out.println("HARNESS_RESULT=NO_THROW");
		} catch (Throwable t) {
			System.out.println("HARNESS_RESULT=" + t.getClass().getSimpleName());
			System.out.println("HARNESS_MSG=" + t.getMessage());
		}
	}
}
