package launcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main extends JFrame {

    private static final Path MC_DIR = Paths.get(System.getenv("APPDATA"), ".minecraft");
    private static final String VERSION_ID = "1.21.4";
    private static final String ASSETS_ID = "19";

    private final LauncherLogic launcherLogic;

    private JButton launchButton;
    private JTextArea logArea;
    private JProgressBar progressBar;

    public Main() {
        setTitle("Fabric 0.17.2 Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        launcherLogic = new LauncherLogic(this::log, this::updateProgress, MC_DIR, VERSION_ID, ASSETS_ID);

        launchButton = new JButton("Minecraft 1.21.4 mit Fabric starten");
        launchButton.addActionListener(this::launchMinecraft);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        JScrollPane scrollPane = new JScrollPane(logArea);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        JPanel topPanel = new JPanel();
        topPanel.add(launchButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
        System.out.println(message);
    }

    private void updateProgress(int progress) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
    }

    private void launchMinecraft(ActionEvent event) {
        launchButton.setEnabled(false);
        new Thread(() -> {
            try {
                launcherLogic.launch();
            } catch (Exception e) {
                e.printStackTrace();
                log("\n!!! FATALER FEHLER: " + e.getClass().getName() + ": " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Fehler beim Starten:\n" + e.getMessage(),
                            "Fehler", JOptionPane.ERROR_MESSAGE);
                    launchButton.setEnabled(true);
                });
            } finally {
                if (launcherLogic.isFinished()) {
                    dispose();
                } else if (launchButton.isEnabled()) {
                    launchButton.setEnabled(true);
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}