package ogren.collin.autoboxer.utilities.remote_utilities.auto_update;

import ogren.collin.autoboxer.utilities.Settings;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

// WORK IN PROGRESS.
public class Downloader {

    private final String url;
    private final File saveTo;
    private double progress = 0.0;
    private boolean isDone = false;

    public Downloader(String url) {
        this.url = url;
        saveTo = new File(Settings.AUTOBOXER_DIRECTORY_APPDATA + "/Downloaded/" + "AutoboxerInstaller" + Settings.getVersion() + ".exe");
    }

    public boolean isDone() {
        return isDone;
    }

    public boolean download() throws IOException {
        if (isDone()) {
            return isDone();
        }

        URL url = URI.create(saveTo.getPath()).toURL();

        HttpURLConnection httpURLConnection = (HttpURLConnection) (url.openConnection());

        long completeFileSize = httpURLConnection.getContentLength();
        if (saveTo.exists() && FileUtils.sizeOf(saveTo) == completeFileSize) {
            isDone = true;
            return true;
        }

        return true;
    }

        public void runInstaller() {

    }
}
