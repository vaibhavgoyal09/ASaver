/*
 * Copyright (c) Christopher A Longo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mystikcoder.statussaver.presentation.framework.videoconverter;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.Gravity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlaylistDownloader {
    private URL url;
    private String fileName;
    private final List<String> playlist;
    private final Context context;

    private static final String BANDWIDTH = "BANDWIDTH";

    public PlaylistDownloader(String playlistUrl,String fileName, Context context) throws MalformedURLException {
        this.url = new URL(playlistUrl);
        this.playlist = new ArrayList<>();
        this.context = context;
        this.fileName = fileName;
    }

    public void download() {
        this.download(null);
    }

    public void download(final String key) {
        new FetchPlaylist().execute(key);
    }

    private void downloadAfterCrypto() throws IOException {
        String downloadUrl = "";
        for (int i = 0; i < playlist.size(); i++) {

            String line = playlist.get(i);
            line = line.trim();

            String EXT_X_KEY = "#EXT-X-KEY";
            if (line.startsWith(EXT_X_KEY)) {

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {

                    }
                }, 0, 10);

            } else if (line.length() > 0 && !line.startsWith("#")) {

                URL segmentUrl;
                if (!line.startsWith("http")) {
                    String baseUrl = getBaseUrl(this.url);
                    segmentUrl = new URL(baseUrl + line);
                } else {
                    segmentUrl = new URL(line);
                }
                downloadUrl = segmentUrl.toString();
            }
        }
        startDownload(downloadUrl);
    }

    private void startDownload(String downloadPath) {
        Toast toast = Toast.makeText(context, "Download started", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        Uri uri = Uri.parse(downloadPath);

        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(fileName);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/Asaver/Josh/" + fileName);
        DownloadManager manager= (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    private String getBaseUrl(URL url) {
        String urlString = url.toString();
        int index = urlString.lastIndexOf('/');
        return urlString.substring(0, ++index);
    }

    private class FetchPlaylist extends AsyncTask<String, Void, Boolean> {
        boolean isMaster = false;
        long maxRate = 0L;
        int maxRateIndex = 0;

        @Override
        protected Boolean doInBackground(String... params) {
            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(url.openStream()));

                String line;
                int index = 0;

                while ((line = reader.readLine()) != null) {
                    playlist.add(line);

                    if (line.contains(BANDWIDTH))
                        isMaster = true;

                    if (isMaster && line.contains(BANDWIDTH)) {
                        try {
                            int pos = line.lastIndexOf(BANDWIDTH + "=") + 10;
                            int end = line.indexOf(",", pos);
                            if (end < 0 || end < pos) end = line.length() - 1;
                            long bandwidth = Long.parseLong(line.substring(pos, end));

                            maxRate = Math.max(bandwidth, maxRate);

                            if (bandwidth == maxRate)
                                maxRateIndex = index + 1;
                        } catch (NumberFormatException ignore) {
                        }
                    }

                    index++;
                }
                reader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return isMaster;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (isMaster) {
                URL tempUrl = updateUrlForSubPlaylist(playlist.get(maxRateIndex));
                if (null != tempUrl) {
                    url = tempUrl;
                    playlist.clear();
                    new FetchPlaylist().execute();
                } else {
                    try {
                        downloadAfterCrypto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    downloadAfterCrypto();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private URL updateUrlForSubPlaylist(String sub) {
        String newUrl;
        URL aUrl = null;

        if (!sub.startsWith("http")) {
            newUrl = getBaseUrl(this.url) + sub;
        } else {
            newUrl = sub;
        }

        try {
            aUrl = new URL(newUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return aUrl;
    }
}
