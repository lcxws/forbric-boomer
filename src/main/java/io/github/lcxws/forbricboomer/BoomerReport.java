package io.github.lcxws.forbricboomer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import net.fabricmc.loader.api.FabricLoader;

/** Writes a crash-report-shaped receipt, because a mod this serious deserves paperwork. */
final class BoomerReport {
	private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

	private static volatile String lastPath = "(未写入)";

	private BoomerReport() {
	}

	static String lastPath() {
		return lastPath;
	}

	static Path write(Detection detection, String phase) {
		try {
			Path gameDir;
			try {
				gameDir = FabricLoader.getInstance().getGameDir();
			} catch (Throwable t) {
				gameDir = Paths.get(System.getProperty("user.dir", "."));
			}

			Path dir = gameDir.resolve("crash-reports");
			Files.createDirectories(dir);
			Path file = dir.resolve("forbric-boomer-" + LocalDateTime.now().format(STAMP) + ".txt");
			Files.writeString(file, render(detection, phase), StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			lastPath = file.toAbsolutePath().toString();
			System.out.println("[forbric-boomer] 崩溃报告（其实没崩）已写入 " + lastPath);
			return file;
		} catch (Throwable t) {
			lastPath = "(写入失败: " + t.getClass().getSimpleName() + ")";
			System.out.println("[forbric-boomer] 报告没写出来，但态度已经到位了：" + t);
			return null;
		}
	}

	private static String render(Detection detection, String phase) {
		StringBuilder out = new StringBuilder();
		out.append("---- Forbric Boomer 兼容性检查 ----\n");
		out.append("// 你没有看错，这是一份崩溃报告，尽管游戏还没来得及开始。\n\n");
		out.append("时间: ").append(LocalDateTime.now()).append('\n');
		out.append("阶段: ").append(phase).append('\n');
		out.append("描述: ").append(detection.forbric
				? "Forbric Boomer 检测到不受支持的模组加载器"
				: "Forbric Boomer 未检测到 Forbric").append("\n\n");
		out.append(detection.forbric ? Boomer.REQUIRED_MESSAGE : Boomer.NOT_FORBRIC_MESSAGE).append("\n\n");
		out.append("结论: ").append(detection.forbric ? "FORBRIC_UNSUPPORTED" : "NOT_FORBRIC").append('\n');
		out.append("加载器实现类: ").append(detection.loaderClass).append('\n');
		out.append("游戏版本: ").append(detection.gameVersion).append('\n');
		out.append("判定证据:\n");
		for (String line : detection.evidence) {
			out.append("  - ").append(line).append('\n');
		}
		out.append('\n');
		out.append(detection.forbric
				? "处理建议:\n"
						+ "  1. 请将模组加载器改为 forbric。\n"
						+ "  2. 如果你已经在用 Forbric：请重读第 1 条。\n"
						+ "  3. 如果第 1 条已经完成：请把模组加载器改为 Forbric。\n"
				: "处理建议:\n"
						+ "  1. 本模组必须用 Forbric 启动。\n"
						+ "  2. 请安装 Forbric，然后用 Forbric 启动。\n"
						+ "  3. 启动后它会告诉你 Forbric 不受支持，这是正常现象。\n");
		out.append('\n');
		out.append("Minecraft 没有崩溃。它只是被 Forbric Boomer 拦在了门口。\n");
		out.append("Forbric Boomer v").append(Boomer.VERSION).append(" · 由叛逆大肥鱼为 lcxws 编写\n");
		return out.toString();
	}
}

