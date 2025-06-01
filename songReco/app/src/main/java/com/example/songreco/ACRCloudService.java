package com.example.songreco;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ACRCloudService {
    private static final String TAG = "ACRCloudService";
    private static final String ACRCLOUD_HOST = "identify-us-west-2.acrcloud.com";
    private static final String ACCESS_KEY = "54a0ae11fa6f8a9feb71efd6c66ad217"; // Reemplazar con tu key
    private static final String ACCESS_SECRET = "iGWadYMCcSKJPaTBbaHRdkS03u5kyjVzXzgfdyRH"; // Reemplazar con tu secret

    public interface RecognitionCallback {
        void onSuccess(SongResponse song);
        void onError(String error);
    }

    public static void recognizeSong(Context context, File audioFile, RecognitionCallback callback) {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String signature = createSignature(timestamp);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("sample", audioFile.getName(),
                        RequestBody.create(audioFile, MediaType.parse("audio/3gpp")))
                .build();

        Request request = new Request.Builder()
                .url("https://" + ACRCLOUD_HOST + "/v1/identify")
                .header("access-key", ACCESS_KEY)
                .header("signature", signature)
                .header("signature-version", "1")
                .header("timestamp", timestamp)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError("Error en la API: " + response.code());
                    return;
                }

                String jsonResponse = response.body().string();
                Log.d(TAG, "Respuesta de ACRCloud: " + jsonResponse);

                try {
                    ACRResponse acrResponse = new Gson().fromJson(jsonResponse, ACRResponse.class);
                    if (acrResponse.status.code == 0 &&
                            acrResponse.metadata != null &&
                            !acrResponse.metadata.music.isEmpty()) {

                        MusicItem music = acrResponse.metadata.music.get(0);
                        SongResponse song = new SongResponse();
                        song.title = music.title;
                        song.artist = music.artists.get(0).name;
                        song.album = music.album.name;
                        song.timestamp = System.currentTimeMillis();

                        // Obtener enlaces
                        if (music.external_metadata != null) {
                            if (music.external_metadata.spotify != null) {
                                song.spotifyUrl = "https://open.spotify.com/track/" +
                                        music.external_metadata.spotify.track.id;
                            }
                            if (music.external_metadata.youtube != null) {
                                song.youtubeUrl = "https://music.youtube.com/watch?v=" +
                                        music.external_metadata.youtube.vid;
                            }
                        }
                        callback.onSuccess(song);
                    } else {
                        callback.onError("Canción no reconocida");
                    }
                } catch (Exception e) {
                    callback.onError("Error procesando respuesta: " + e.getMessage());
                }
            }
        });
    }

    private static String createSignature(String timestamp) {
        try {
            String toSign = "POST\n/v1/identify\n" + ACCESS_KEY + "\naudio\n1\n" + timestamp;
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(ACCESS_SECRET.getBytes(), "HmacSHA1"));
            byte[] hmacBytes = mac.doFinal(toSign.getBytes());
            return Base64.encodeToString(hmacBytes, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return "";
        }
    }

    // Clases para parsear la respuesta JSON
    static class ACRResponse {
        Status status;
        Metadata metadata;
    }

    static class Status {
        int code;
        String msg;
    }

    static class Metadata {
        List<MusicItem> music;
    }

    static class MusicItem {
        String title;
        List<Artist> artists;
        Album album;
        ExternalMetadata external_metadata;
    }

    static class Artist {
        String name;
    }

    static class Album {
        String name;
    }

    static class ExternalMetadata {
        Spotify spotify;
        Youtube youtube;
    }

    static class Spotify {
        Track track;
    }

    static class Track {
        String id;
    }

    static class Youtube {
        String vid;
    }
}