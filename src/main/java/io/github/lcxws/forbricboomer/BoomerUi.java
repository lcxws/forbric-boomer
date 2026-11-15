package io.github.lcxws.forbricboomer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

/** The popup. Console output is the headless fallback, not the main event. */
final class BoomerUi {
	private static final String MESSAGE = Boomer.REQUIRED_MESSAGE;
	private static final String NOT_FORBRIC_MESSAGE = Boomer.NOT_FORBRIC_MESSAGE;
	private static final Color BG = new Color(0x15171C);
	private static final Color PANEL = new Color(0x1E2129);
	private static final Color RED = new Color(0xC62828);
	private static final Color TEXT = new Color(0xE6E6E6);
	private static final Color MUTED = new Color(0x9AA0A6);

	private BoomerUi() {
	}

	static void present(Detection detection, String phase) {
		boolean console = Boolean.getBoolean("forbricboomer.console")
				|| GraphicsEnvironment.isHeadless();
		if (console) {
			console(detection, phase);
			return;
		}

		try {
			SwingUtilities.invokeAndWait(() -> show(detection, phase));
		} catch (Throwable t) {
			console(detection, phase);
		}
	}

	private static void console(Detection detection, String phase) {
		String art = """
				  ______ ____  ____  ____  ___ ____   ____  _____ ____  _____ ____
				 |  ____/ __ \\|  _ \\| __ )|_ _/ ___| | __ )/ _ \\|  _ \\| ____|  _ \\
				 | |__ | |  | | |_) |  _ \\ | | |     |  _ \\ | | | |_) |  _| | |_) |
				 |  __|| |__| |  _ <| |_) || | |___  | |_) | |_| |  _ <| |___|  _ <
				 |_|    \\____/|_| \\_\\____/|___\\____| |____/ \\___/|_| \\_\\_____|_| \\_\\
				""";
		System.out.println(art);
		System.out.println("FORBRIC BOOMER v" + Boomer.VERSION + " · 兼容性检查");
		System.out.println("阶段: " + phase + " · 加载器: " + detection.loaderClass
				+ " · 游戏: " + detection.gameVersion);
		System.out.println();
		System.out.println("================================================================");
		System.out.println("  " + (detection.forbric ? MESSAGE : NOT_FORBRIC_MESSAGE));
		System.out.println("================================================================");
		System.out.println();
		for (String line : detection.evidence) {
			System.out.println("  - " + line);
		}
		System.out.println();
		System.out.println("  崩溃报告（其实没崩）: " + BoomerReport.lastPath());
		System.out.println("  这不是崩溃。这是一次有原则的拒绝。");
		System.out.println();
		System.out.flush();
	}

	private static void show(Detection detection, String phase) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable ignored) {
			// The cross-platform look still gets the job done.
		}

		JDialog dialog = new JDialog((java.awt.Frame) null,
				detection.forbric
						? "Forbric Boomer — 兼容性检查失败"
						: "Forbric Boomer — 未检测到 Forbric",
				true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getContentPane().setBackground(BG);

		JPanel header = new JPanel();
		header.setBackground(RED);
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

		JLabel brand = new JLabel("FORBRIC BOOMER  ·  v" + Boomer.VERSION);
		brand.setForeground(Color.WHITE);
		brand.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
		JLabel title = new JLabel(detection.forbric
				? "兼容性检查失败：不受支持的模组加载器"
				: "未检测到 Forbric：本模组拒绝启动");
		title.setForeground(Color.WHITE);
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
		header.add(brand);
		header.add(Box.createVerticalStrut(4));
		header.add(title);
		dialog.add(header, BorderLayout.NORTH);

		JPanel body = new JPanel(new BorderLayout(0, 10));
		body.setBackground(BG);
		body.setBorder(BorderFactory.createEmptyBorder(14, 18, 8, 18));

		String headline = detection.forbric ? MESSAGE : NOT_FORBRIC_MESSAGE;
		JLabel message = new JLabel("<html><div style='width:600px'>" + headline + "</div></html>");
		message.setForeground(detection.forbric ? new Color(0xFFB4B4) : new Color(0xFFD180));
		message.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
		body.add(message, BorderLayout.NORTH);

		String reportPath = BoomerReport.lastPath();
		int slash = Math.max(reportPath.lastIndexOf('/'), reportPath.lastIndexOf('\\'));
		String reportName = slash >= 0 ? reportPath.substring(slash + 1) : reportPath;

		StringBuilder details = new StringBuilder();
		details.append("阶段: ").append(phase).append('\n');
		details.append("加载器: ").append(detection.loaderClass).append('\n');
		details.append("游戏版本: ").append(detection.gameVersion).append('\n');
		details.append("报告: crash-reports/").append(reportName).append("\n\n");
		details.append("判定证据:\n");
		for (String line : detection.evidence) {
			details.append("  • ").append(line).append('\n');
		}
		details.append('\n');
		if (detection.forbric) {
			details.append("处理建议:\n");
			details.append("  1. 请将模组加载器改为 forbric。\n");
			details.append("  2. 如果你已经在用 Forbric：请重读第 1 条。\n");
			details.append("  3. 如果你已经改完了：请把模组加载器改为 Forbric。\n");
			details.append('\n');
			details.append("注意：本模组只能在 Forbric 上加载，而 Forbric 不受支持。\n");
		} else {
			details.append("处理建议:\n");
			details.append("  1. 本模组必须用 Forbric 启动。\n");
			details.append("  2. 请安装 Forbric，然后用 Forbric 启动。\n");
			details.append("  3. 启动后它会拒绝 Forbric，这是正常现象。\n");
		}

		JTextArea area = new JTextArea(details.toString());
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		area.setBackground(PANEL);
		area.setForeground(TEXT);
		area.setCaretColor(TEXT);
		area.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
		JScrollPane scroll = new JScrollPane(area);
		scroll.setBorder(BorderFactory.createLineBorder(new Color(0x333843)));
		scroll.setPreferredSize(new Dimension(640, 220));
		body.add(scroll, BorderLayout.CENTER);

		JLabel status = new JLabel(" ");
		status.setForeground(MUTED);
		status.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));

		JPanel bottom = new JPanel(new BorderLayout());
		bottom.setBackground(BG);
		bottom.setBorder(BorderFactory.createEmptyBorder(0, 18, 14, 18));
		bottom.add(status, BorderLayout.NORTH);

		JPanel buttons = new JPanel();
		buttons.setBackground(BG);
		JButton switchButton = new JButton(detection.forbric ? "改为 Forbric" : "改用 Forbric");
		JButton retryButton = new JButton("重试");
		JButton exitButton = new JButton("退出游戏");
		switchButton.setToolTipText(detection.forbric
				? "把模组加载器改为 Forbric（然后回到同一个弹窗）"
				: "先安装 Forbric，再用 Forbric 启动本模组");
		retryButton.setToolTipText("再检查一次，看看 Forbric 会不会消失");
		exitButton.setToolTipText("退出。反正也启动不了。");
		buttons.add(switchButton);
		buttons.add(retryButton);
		buttons.add(exitButton);
		bottom.add(buttons, BorderLayout.SOUTH);
		dialog.add(bottom, BorderLayout.SOUTH);
		dialog.add(body, BorderLayout.CENTER);

		switchButton.addActionListener(e -> {
			if (!detection.forbric) {
				JOptionPane.showMessageDialog(dialog,
						"<html><div style='width:420px'><b>未检测到 Forbric。</b><br><br>"
								+ "本模组必须用 Forbric 启动。请先安装 Forbric，再用 Forbric 启动。</div></html>",
						"需要 Forbric", JOptionPane.WARNING_MESSAGE);
				status.setText("当前加载器不是 Forbric。请安装 Forbric 后再试。");
				return;
			}
			int[] attempts = {0};
			JOptionPane.showMessageDialog(dialog,
					"<html><div style='width:420px'><b>正在把模组加载器改为 Forbric…</b><br><br>"
							+ "进度: 100%<br>"
							+ "结果: 当前加载器 = forbric<br><br>"
							+ "检测到forbric加载器，不受支持的加载器，请将模组加载器改为forbric！<br><br>"
							+ "<i>提示：你已经改过一次了。</i></div></html>",
					"切换成功", JOptionPane.INFORMATION_MESSAGE);
			attempts[0]++;
			status.setText("第 " + attempts[0] + " 次“改为 Forbric”已完成。加载器仍然是 Forbric。");
			Toolkit.getDefaultToolkit().beep();
		});
		retryButton.addActionListener(e -> {
			Detection again = Detection.run();
			status.setText("重试结果：" + (again.forbric ? "依然是 Forbric。不受支持。" : "依然不是 Forbric。必须用 Forbric 启动。"));
			Toolkit.getDefaultToolkit().beep();
		});
		exitButton.addActionListener(e -> dialog.dispose());
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dialog.dispose();
			}
		});

		String autoClose = System.getProperty("forbricboomer.autoclose");
		if (autoClose != null && !autoClose.isBlank()) {
			try {
				new Timer(Integer.parseInt(autoClose), e -> dialog.dispose()).start();
			} catch (NumberFormatException ignored) {
				// No auto-close for a malformed number.
			}
		}

		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
	}
}

