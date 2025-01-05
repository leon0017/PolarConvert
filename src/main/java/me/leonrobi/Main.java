package me.leonrobi;

import net.hollowcube.polar.AnvilPolar;
import net.hollowcube.polar.PolarWorld;
import net.hollowcube.polar.PolarWriter;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    private static final String TITLE = "PolarConvert";

    private static JFrame frame;

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 150);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JTextField folderPathField = new JTextField();
        folderPathField.setEditable(true);
        folderPathField.setPreferredSize(new Dimension(300, 25));
        JButton browseFolderButton = new JButton("Select Folder");

        JTextField filePathField = new JTextField();
        filePathField.setEditable(true);
        filePathField.setPreferredSize(new Dimension(300, 25));
        JButton browseFileButton = new JButton("Select File");

        JButton submitButton = new JButton("Submit");

        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("World Folder Path:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(folderPathField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        mainPanel.add(browseFolderButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        mainPanel.add(new JLabel("Polar Dest Path:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        mainPanel.add(filePathField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        mainPanel.add(browseFileButton, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(submitButton, gbc);

        frame.add(mainPanel);

        browseFolderButton.addActionListener(e -> {
            JFileChooser folderChooser = new JFileChooser();
            folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = folderChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = folderChooser.getSelectedFile();
                folderPathField.setText(selectedFolder.getAbsolutePath());
            }
        });

        browseFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Polar files (*.polar)", "polar"));
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String path = selectedFile.getAbsolutePath();
                if (!path.endsWith(".polar"))
                    path += ".polar";
                filePathField.setText(path);
            }
        });

        submitButton.addActionListener(e -> {
            String folderPath = folderPathField.getText();
            String filePath = filePathField.getText();
            if (!folderPath.isEmpty() && !filePath.isEmpty()) {
                try {
                    anvilToPolar(new File(folderPath), new File(filePath));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Please select both a folder and a file.");
            }
        });

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    public static void anvilToPolar(@NotNull File anvilPath, @NotNull File polarPath) {
        frame.setTitle(TITLE + " | Initializing Minestom...");

        try {
            if (!anvilPath.exists()) {
                JOptionPane.showMessageDialog(frame, "Could not find anvil path '" + anvilPath.getAbsolutePath() + "'");
                return;
            }

            File regionFolder = Path.of(anvilPath.getAbsolutePath(), "region").toFile();
            if (!regionFolder.exists() || !regionFolder.isDirectory()) {
                JOptionPane.showMessageDialog(frame, "Could not find 'region' folder in '" + anvilPath.getAbsolutePath() + "'");
                return;
            }

            MinecraftServer.init();

            polarPath.getParentFile().mkdirs();

            frame.setTitle(TITLE + " | Converting...");
            PolarWorld polarWorld = AnvilPolar.anvilToPolar(anvilPath.toPath());
            frame.setTitle(TITLE + " | Getting bytes...");
            byte[] polarWorldBytes = PolarWriter.write(polarWorld);
            frame.setTitle(TITLE + " | Writing bytes...");
            Files.write(polarPath.toPath(), polarWorldBytes);

            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd.exe", "/c", "explorer.exe /select, \"" + polarPath.getAbsolutePath() + "\"")
                        .redirectErrorStream(true)
                        .start();
            } else {
                JOptionPane.showMessageDialog(frame, "Successfully converted to polar. path='" + polarPath.toPath() + "'");
            }
        } catch (Exception e) {
            frame.setTitle(TITLE + " | EXCEPTION: " + e);
            e.printStackTrace(System.err);
        }

        frame.setTitle(TITLE + " | Uninitializing Minestom...");
        MinecraftServer.stopCleanly();

        frame.setTitle(TITLE);
    }

}