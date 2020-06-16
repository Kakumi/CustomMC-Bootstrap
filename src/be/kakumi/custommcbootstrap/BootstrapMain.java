package be.kakumi.custommcbootstrap;

import fr.theshark34.openlauncherlib.external.ClasspathConstructor;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import fr.theshark34.openlauncherlib.util.SplashScreen;
import fr.theshark34.openlauncherlib.util.explorer.ExploredDirectory;
import fr.theshark34.openlauncherlib.util.explorer.Explorer;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.supdate.application.integrated.FileDeleter;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.colored.SColoredBar;
import fr.theshark34.swinger.util.WindowMover;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class BootstrapMain {
    private static final File FILE = new File(GameDirGenerator.createGameDir(Config.LAUNCHER_NAME.replaceAll(" ", "-").toLowerCase()),Config.LAUNCHER_FILE_FOLDER);
    private static SplashScreen splashScreen;
    private static SColoredBar progressBar = new SColoredBar(Swinger.getTransparentWhite(100), new Color(30, 206, 30, 150));
    private static JLabel texteProgressBar = new JLabel("File scanning", SwingConstants.CENTER);

    public static void main(String[] args) {
        try {
            FILE.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Swinger.setResourcePath(Config.RESOURCES_PATH);
        showSplahScreen();
        try {
            update();
        }catch (Exception e) {
            e.printStackTrace();
        }

        try {
            launch();
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void showSplahScreen() {
        splashScreen = new SplashScreen(Config.LAUNCHER_NAME + " - Updater", Swinger.getResource("screen.png"));
        WindowMover mover = new WindowMover(splashScreen);
        splashScreen.addMouseListener(mover);
        splashScreen.addMouseMotionListener(mover);
        splashScreen.setBackground(Swinger.TRANSPARENT);
        splashScreen.setIconImage(Swinger.getResource("logo.png"));
        splashScreen.setSize(700, 267);
        splashScreen.setLocationRelativeTo(null);
        splashScreen.setAlwaysOnTop(true);
        splashScreen.setLayout(null);
        splashScreen.setVisible(true);

        progressBar.setBounds(3, 267 - 31, 700 - 3, 28);
        splashScreen.add(progressBar);

        texteProgressBar.setFont(new Font("Arial", Font.PLAIN, 18));
        texteProgressBar.setBounds(3, 267 - 55, 700 - 3, 28);
        texteProgressBar.setForeground(Color.WHITE);
        splashScreen.add(texteProgressBar);
    }

    private static void update() throws Exception{
        SUpdate sUpdate = new SUpdate(Config.SITE_WEB_UPDATER, FILE);
        sUpdate.addApplication(new FileDeleter());

        Thread updateThread = new Thread() {
            private int val;
            private int max;

            public void run() {
                while (!isInterrupted()) {
                    progressBar.setVisible(true);

                    if (BarAPI.getNumberOfFileToDownload() == 0) {
                        texteProgressBar.setText("File scanning");
                        progressBar.setMaximum(1);
                        progressBar.setValue(1);
                    }
                    else {
                        this.val = ((int)(BarAPI.getNumberOfTotalDownloadedBytes() / 1000L));
                        this.max = ((int)(BarAPI.getNumberOfTotalBytesToDownload() / 1000L));
                        progressBar.setMaximum(this.max);
                        progressBar.setValue(this.val);
                        texteProgressBar.setText("Download in progress.. (" + Swinger.percentage(this.val, this.max) + "%)");

                    }
                }
            }
        };
        updateThread.start();
        sUpdate.start();
        updateThread.interrupt();

        texteProgressBar.setText("Launcher launch !");
    }

    private static void launch() throws Exception {
        splashScreen.setVisible(false);
        ClasspathConstructor classpathConstructor = new ClasspathConstructor();
        ExploredDirectory exploredDirectory = Explorer.dir(FILE);
        //classpathConstructor.add(exploredDirectory.sub("libs").allRecursive().files().match("^(.*\\.((jar)$))*$"));
        classpathConstructor.add(exploredDirectory.get(Config.LAUNCHER_FILE));
        ExternalLaunchProfile externalLaunchProfile = new ExternalLaunchProfile(Config.CLASS_PATH_FROM_LAUNCHER, classpathConstructor.make());
        ExternalLauncher externalLauncher = new ExternalLauncher(externalLaunchProfile);
        Process process = externalLauncher.launch();
        System.out.println(classpathConstructor.make());
        try {
            process.waitFor();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        System.exit(0);
    }

}
