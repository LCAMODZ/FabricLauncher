package launcher;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main extends JFrame {

    private static final Path MC_DIR = Paths.get(System.getenv("APPDATA"), ".minecraft");
    private static final String VERSION_ID = "1.21.8";
    private static final String ASSETS_ID = "26";


    private static final Color BG_PRIMARY = new Color(24, 26, 30);
    private static final Color BG_SECONDARY = new Color(33, 36, 42);
    private static final Color BG_CARD = new Color(42, 45, 52);
    private static final Color ACCENT_GREEN = new Color(33, 196, 137);
    private static final Color ACCENT_GREEN_HOVER = new Color(45, 210, 150);
    private static final Color ACCENT_BLUE = new Color(60, 130, 240);
    private static final Color TEXT_PRIMARY = new Color(240, 242, 245);
    private static final Color TEXT_SECONDARY = new Color(175, 180, 190);
    private static final Color TEXT_MUTED = new Color(120, 125, 135);
    private static final Color BORDER_COLOR = new Color(55, 60, 70);
    private static final Color ERROR_COLOR = new Color(230, 74, 95);


    private final LauncherLogic launcherLogic;


    private JButton launchButton;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel statusIconLabel;
    private Point mouseDownCompCoords;

    public Main() {
        setTitle("Minecraft Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));

        launcherLogic = new LauncherLogic(this::log, this::updateProgress, MC_DIR, VERSION_ID, ASSETS_ID);

        initUI();
        addDragFunctionality();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(BG_PRIMARY);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 1, 20),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JPanel headerPanel = createHeaderPanel();

        JPanel contentWrapper = new JPanel(new BorderLayout(0, 0));
        contentWrapper.setOpaque(false);

        JPanel sideBySidePanel = new JPanel(new BorderLayout(30, 0));
        sideBySidePanel.setOpaque(false);
        sideBySidePanel.setBorder(new EmptyBorder(0, 30, 25, 30));

        JPanel leftSide = createInfoPanel();
        leftSide.setPreferredSize(new Dimension(300, 0));

        JPanel rightSide = new JPanel(new BorderLayout(0, 20));
        rightSide.setOpaque(false);
        rightSide.add(createCenterPanel(), BorderLayout.CENTER);
        rightSide.add(createBottomPanel(), BorderLayout.SOUTH);

        sideBySidePanel.add(leftSide, BorderLayout.WEST);
        sideBySidePanel.add(rightSide, BorderLayout.CENTER);

        contentWrapper.add(sideBySidePanel, BorderLayout.CENTER);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentWrapper, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(18, 30, 18, 30));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("M");
        logoLabel.setFont(new Font("Consolas", Font.BOLD, 30));
        logoLabel.setForeground(ACCENT_GREEN);
        logoLabel.setPreferredSize(new Dimension(50, 50));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        logoLabel.setVerticalAlignment(SwingConstants.CENTER);
        logoLabel.setOpaque(false);

        logoLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT_GREEN, 1, true),
                new EmptyBorder(8, 0, 0, 0)
        ));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 3));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("MINECRAFT LAUNCHER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(TEXT_PRIMARY);

        JLabel subtitleLabel = new JLabel("Powered by Fabric");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_MUTED);

        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        leftPanel.add(logoLabel);
        leftPanel.add(titlePanel);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controlsPanel.setOpaque(false);

        JButton minimizeBtn = createControlButton("_");
        JButton closeBtn = createControlButton("X");

        minimizeBtn.addActionListener(e -> setState(JFrame.ICONIFIED));
        closeBtn.addActionListener(e -> System.exit(0));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                closeBtn.setBackground(ERROR_COLOR);
            }
            public void mouseExited(MouseEvent e) {
                closeBtn.setBackground(BG_SECONDARY);
            }
        });

        controlsPanel.add(minimizeBtn);
        controlsPanel.add(closeBtn);

        header.add(leftPanel, BorderLayout.WEST);
        header.add(controlsPanel, BorderLayout.EAST);

        return header;
    }

    private JButton createControlButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(TEXT_SECONDARY);
        btn.setBackground(BG_SECONDARY);
        btn.setPreferredSize(new Dimension(40, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(BORDER_COLOR);
                btn.setForeground(TEXT_PRIMARY);
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(BG_SECONDARY);
                btn.setForeground(TEXT_SECONDARY);
            }
        });

        return btn;
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JPanel versionCard = createInfoCard("VERSION", VERSION_ID, ACCENT_BLUE);
        JPanel loaderCard = createInfoCard("LOADER", "Fabric 0.17.2", ACCENT_GREEN);
        JPanel statusCard = createInfoCard("STATUS", "Bereit", new Color(150, 120, 200));

        infoPanel.add(versionCard);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(loaderCard);
        infoPanel.add(Box.createVerticalStrut(15));
        infoPanel.add(statusCard);

        return infoPanel;
    }

    private JPanel createInfoCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(12, 10));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 1, 14),
                new EmptyBorder(18, 22, 18, 22)
        ));
        card.setMaximumSize(new Dimension(Short.MAX_VALUE, 100));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(TEXT_MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(TEXT_PRIMARY);

        JPanel indicatorPanel = new JPanel();
        indicatorPanel.setOpaque(false);
        JPanel indicator = new JPanel();
        indicator.setBackground(accentColor);
        indicator.setPreferredSize(new Dimension(5, 44));
        indicatorPanel.add(indicator);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(indicatorPanel, BorderLayout.WEST);

        return card;
    }

    private JPanel createCenterPanel() {
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JPanel logCard = new JPanel(new BorderLayout(0, 15));
        logCard.setBackground(BG_CARD);
        logCard.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(BORDER_COLOR, 1, 14),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setOpaque(false);

        JLabel logTitle = new JLabel("KONSOLE / LAUNCH LOG");
        logTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logTitle.setForeground(TEXT_MUTED);

        logHeader.add(logTitle, BorderLayout.WEST);


        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        logArea.setBackground(BG_SECONDARY);
        logArea.setForeground(TEXT_SECONDARY);
        logArea.setCaretColor(ACCENT_GREEN);
        logArea.setBorder(new EmptyBorder(14, 14, 14, 14));
        logArea.setLineWrap(false);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(new RoundedBorder(BORDER_COLOR, 1, 8));
        scrollPane.getViewport().setBackground(BG_SECONDARY);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        logCard.add(logHeader, BorderLayout.NORTH);
        logCard.add(scrollPane, BorderLayout.CENTER);

        center.add(logCard, BorderLayout.CENTER);

        return center;
    }

    private JPanel createBottomPanel() {
        JPanel bottom = new JPanel(new BorderLayout(0, 18));
        bottom.setOpaque(false);

        JPanel progressSection = new JPanel(new BorderLayout(0, 8));
        progressSection.setOpaque(false);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusPanel.setOpaque(false);

        statusIconLabel = new JLabel("*");
        statusIconLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusIconLabel.setForeground(ACCENT_GREEN);

        statusLabel = new JLabel("Bereit zum Starten");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLabel.setForeground(TEXT_SECONDARY);

        statusPanel.add(statusIconLabel);
        statusPanel.add(statusLabel);

        progressBar = new JProgressBar() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(BG_SECONDARY);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

                if (getValue() > 0) {
                    int width = (int) ((getWidth() - 4) * (getValue() / 100.0));
                    GradientPaint gradient = new GradientPaint(
                            0, 0, ACCENT_GREEN,
                            width, 0, ACCENT_BLUE
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRoundRect(2, 2, width, getHeight() - 4, 6, 6);
                }
            }
        };
        progressBar.setPreferredSize(new Dimension(0, 8));
        progressBar.setOpaque(false);
        progressBar.setBorderPainted(false);

        progressSection.add(statusPanel, BorderLayout.NORTH);
        progressSection.add(progressBar, BorderLayout.CENTER);

        launchButton = new JButton("SPIEL STARTEN") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (isEnabled()) {
                    GradientPaint gradient = new GradientPaint(
                            0, 0, getModel().isRollover() ? ACCENT_GREEN_HOVER : ACCENT_GREEN,
                            getWidth(), 0, ACCENT_BLUE
                    );
                    g2d.setPaint(gradient);
                } else {
                    g2d.setColor(new Color(52, 73, 94));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        launchButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        launchButton.setForeground(Color.WHITE);
        launchButton.setPreferredSize(new Dimension(0, 55));
        launchButton.setFocusPainted(false);
        launchButton.setBorderPainted(false);
        launchButton.setContentAreaFilled(false);
        launchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        launchButton.addActionListener(this::launchMinecraft);

        bottom.add(progressSection, BorderLayout.NORTH);
        bottom.add(launchButton, BorderLayout.SOUTH);

        return bottom;
    }

    private void addDragFunctionality() {
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                mouseDownCompCoords = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
        });
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void updateProgress(int progress) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            if (progress > 0 && progress < 100) {
                statusLabel.setText("Wird gestartet... " + progress + "%");
                statusIconLabel.setForeground(ACCENT_BLUE);
            } else if (progress == 100) {
                statusLabel.setText("Erfolgreich gestartet!");
                statusIconLabel.setForeground(ACCENT_GREEN);
            }
        });
    }

    private void launchMinecraft(ActionEvent event) {
        launchButton.setEnabled(false);
        launchButton.setText("WIRD GESTARTET...");
        statusLabel.setText("Minecraft wird vorbereitet...");
        statusIconLabel.setForeground(ACCENT_BLUE);

        new Thread(() -> {
            try {
                launcherLogic.launch();

            } catch (Exception e) {
                e.printStackTrace();
                log("\n!!! FEHLER: " + e.getClass().getName() + ": " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    showModernError("Fehler beim Starten", e.getMessage());
                    launchButton.setEnabled(true);
                    launchButton.setText("SPIEL STARTEN");
                    statusLabel.setText("Fehler beim Starten");
                    statusIconLabel.setForeground(ERROR_COLOR);
                    progressBar.setValue(0);
                });
            } finally {
                if (launcherLogic.isFinished()) {
                    dispose();
                } else {
                    SwingUtilities.invokeLater(() -> {
                        if (progressBar.getValue() < 100) {
                            launchButton.setEnabled(true);
                            launchButton.setText("SPIEL STARTEN");
                        }
                    });
                }
            }
        }).start();
    }

    private void showModernError(String title, String message) {
        JDialog dialog = new JDialog(this, "", true);
        dialog.setUndecorated(true);
        dialog.setSize(450, 230);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(ERROR_COLOR, 2, 15),
                new EmptyBorder(25, 25, 25, 25)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(ERROR_COLOR);

        JTextArea msgArea = new JTextArea(message);
        msgArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msgArea.setForeground(TEXT_SECONDARY);
        msgArea.setBackground(BG_CARD);
        msgArea.setEditable(false);
        msgArea.setLineWrap(true);
        msgArea.setWrapStyleWord(true);

        JButton okBtn = new JButton("VERSTANDEN");
        okBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        okBtn.setForeground(Color.WHITE);
        okBtn.setBackground(ERROR_COLOR);
        okBtn.setPreferredSize(new Dimension(0, 45));
        okBtn.setFocusPainted(false);
        okBtn.setBorderPainted(false);
        okBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okBtn.addActionListener(e -> dialog.dispose());

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(msgArea, BorderLayout.CENTER);
        panel.add(okBtn, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }

    static class RoundedBorder implements javax.swing.border.Border {
        private Color color;
        private int thickness;
        private int radius;

        RoundedBorder(Color color, int thickness, int radius) {
            this.color = color;
            this.thickness = thickness;
            this.radius = radius;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(thickness, thickness, thickness, thickness);
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(thickness));
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}