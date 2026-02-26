package com.farmo.activities.commonActivities;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KYCApiService {

    private static final String BASE_URL = "https://api.farmoapp.com/";
    private static final String KYC_UPLOAD_ENDPOINT = "kyc/submit";

    private final OkHttpClient client;
    private final Context context;

    public KYCApiService(Context context) {
        this.context = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Upload KYC documents to server.
     * ✅ FIX: Properly converts content:// and file:// URIs to real files
     * so OkHttp can upload them without quality loss.
     */
    public void uploadKYCDocument(KYCDocument kycDocument, KYCUploadCallback callback) {
        new Thread(() -> {
            try {
                if (kycDocument.getDocumentType() == null || kycDocument.getDocumentNumber() == null
                        || kycDocument.getFrontImageUri() == null) {
                    callback.onError("Missing required document data");
                    return;
                }

                // ✅ Convert URIs to actual files (handles both content:// and file:// URIs)
                File frontFile = copyUriToFile(kycDocument.getFrontImageUri(), "front_image.jpg");
                File backFile  = kycDocument.getBackImageUri() != null
                        ? copyUriToFile(kycDocument.getBackImageUri(), "back_image.jpg") : null;
                File selfieFile = kycDocument.getSelfieImageUri() != null
                        ? copyUriToFile(kycDocument.getSelfieImageUri(), "selfie_image.jpg") : null;

                if (frontFile == null) {
                    callback.onError("Failed to process front image");
                    return;
                }

                MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("documentType",   kycDocument.getDocumentType())
                        .addFormDataPart("documentNumber", kycDocument.getDocumentNumber())
                        .addFormDataPart("frontImage", frontFile.getName(),
                                RequestBody.create(MediaType.parse("image/jpeg"), frontFile));

                if (backFile != null) {
                    bodyBuilder.addFormDataPart("backImage", backFile.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), backFile));
                }

                if (selfieFile != null) {
                    bodyBuilder.addFormDataPart("selfieImage", selfieFile.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), selfieFile));
                }

                Request request = new Request.Builder()
                        .url(BASE_URL + KYC_UPLOAD_ENDPOINT)
                        .post(bodyBuilder.build())
                        .addHeader("Authorization", "Bearer YOUR_AUTH_TOKEN")
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    callback.onSuccess(body);
                } else {
                    callback.onError("Upload failed: HTTP " + response.code());
                }
                response.close();

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    public void checkKYCStatus(String documentNumber, KYCStatusCallback callback) {
        new Thread(() -> {
            try {
                Request request = new Request.Builder()
                        .url(BASE_URL + "kyc/status/" + documentNumber)
                        .get()
                        .addHeader("Authorization", "Bearer YOUR_AUTH_TOKEN")
                        .build();

                Response response = client.newCall(request).execute();

                if (response.isSuccessful() && response.body() != null) {
                    String body = response.body().string();
                    JSONObject json = new JSONObject(body);
                    callback.onStatusReceived(json.optString("status", "UNKNOWN"));
                } else {
                    callback.onError("Failed to check status: HTTP " + response.code());
                }
                response.close();

            } catch (Exception e) {
                e.printStackTrace();
                callback.onError("Error: " + e.getMessage());
            }
        }).start();
    }

    /**
     * ✅ FIX: Properly copy any URI (content://, file://) into a real temp file.
     * Old getFileFromUri() just did uri.getPath() which FAILS for content:// URIs
     * from gallery, FileProvider, or Google Photos.
     */
    private File copyUriToFile(Uri uri, String fileName) {
        try {
            File outFile = new File(context.getCacheDir(), fileName);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
            fos.close();
            inputStream.close();
            return outFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface KYCUploadCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public interface KYCStatusCallback {
        void onStatusReceived(String status);
        void onError(String error);
    }
}