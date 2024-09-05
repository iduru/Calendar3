package com.example.calendar3;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.content.Context;


import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class VisionHelper {

    private static final String TAG = "VisionHelper";
    private static EventRepository eventRepository; // ここで EventRepository を利用

    public interface TextDetectionCallback {
        void onTextDetected(String text);
        void onDatesExtracted(List<String> dates);
        void onAnnouncementsExtracted(List<String> announcements);
        void onError(Exception e);
    }

    // Bitmapからテキストを検出するメソッド
    public static void detectTextFromBitmap(Bitmap bitmap, Context context, TextDetectionCallback callback) {
        // EventRepository の初期化
        eventRepository = new EventRepository(context);

        // Bitmapをbyte[]に変換して、detectTextFromByteArrayメソッドを呼び出す
        new Thread(() -> {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                detectTextFromByteArray(imageBytes, callback);
            } catch (Exception e) {
                Log.e(TAG, "Error converting bitmap to byte array", e);
                notifyError(callback, e);
            }
        }).start();
    }

    // byte[]からテキストを検出するメソッド
    public static void detectTextFromByteArray(byte[] imgBytes, TextDetectionCallback callback) {
        new Thread(() -> {
            try {
                // ByteStringに変換
                ByteString imgByteString = ByteString.copyFrom(imgBytes);
                Image img = Image.newBuilder().setContent(imgByteString).build();

                // 注釈機能を設定
                Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();

                // 画像リクエストを作成
                AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                        .addFeatures(feature)
                        .setImage(img)
                        .build();

                // バッチリクエストを作成
                BatchAnnotateImagesRequest batchRequest = BatchAnnotateImagesRequest.newBuilder()
                        .addRequests(request)
                        .build();

                // ImageAnnotatorClientを作成してリクエストを送信
                try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {
                    BatchAnnotateImagesResponse response = vision.batchAnnotateImages(batchRequest);

                    // レスポンスから画像注釈を取得する
                    List<AnnotateImageResponse> responses = response.getResponsesList();
                    StringBuilder detectedText = new StringBuilder();
                    for (AnnotateImageResponse imageResponse : responses) {
                        if (imageResponse.hasError()) {
                            Log.e(TAG, "Error: " + imageResponse.getError().getMessage());
                            notifyError(callback, new Exception(imageResponse.getError().getMessage()));
                            return;
                        }
                        for (EntityAnnotation annotation : imageResponse.getTextAnnotationsList()) {
                            detectedText.append(annotation.getDescription()).append("\n");
                        }
                    }

                    // テキストから日付と案内を抽出
                    String text = detectedText.toString();
                    List<String> dates = extractDates(text);
                    List<String> announcements = extractAnnouncements(text);

                    // メインスレッドでコールバックを実行する
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onTextDetected(text);
                        callback.onDatesExtracted(dates);
                        callback.onAnnouncementsExtracted(announcements);

                        // 予定をカレンダーに追加する
                        addEventsToCalendar(dates, announcements);
                    });
                }
            } catch (IOException e) {
                Log.e(TAG, "Error processing image", e);
                notifyError(callback, e);
            }
        }).start();
    }

    // テキストから日付と時間を抽出するメソッド
    private static List<String> extractDates(String text) {
        List<String> dates = new ArrayList<>();

        // 西暦日付パターン
        Pattern datePattern = Pattern.compile("\\b(\\d{4}/\\d{1,2}/\\d{1,2})\\b"); // 例: 2024/8/25
        Matcher matcher = datePattern.matcher(text);
        while (matcher.find()) {
            dates.add(matcher.group(1));
        }

        // 和暦日付パターン
        Pattern japaneseEraPattern = Pattern.compile("令和\\d{1,2}年\\d{1,2}月\\d{1,2}日\\s*\\d{1,2}時"); // 例: 令和5年8月25日 14時
        Matcher japaneseEraMatcher = japaneseEraPattern.matcher(text);
        while (japaneseEraMatcher.find()) {
            dates.add(japaneseEraMatcher.group());
        }

        return dates;
    }

    // テキストから案内を抽出するメソッド
    private static List<String> extractAnnouncements(String text) {
        List<String> announcements = new ArrayList<>();
        Pattern announcementPattern = Pattern.compile("(?<=案内：)(.*?)(?=\\n|$)");
        Matcher matcher = announcementPattern.matcher(text);
        while (matcher.find()) {
            announcements.add(matcher.group());
        }
        return announcements;
    }

    // 抽出したデータをカレンダーに追加するメソッド
    private static void addEventsToCalendar(List<String> dates, List<String> announcements) {
        if (dates.size() != announcements.size()) {
            Log.w(TAG, "The number of dates does not match the number of announcements.");
            return;
        }

        for (int i = 0; i < dates.size(); i++) {
            String dateStr = dates.get(i);
            String announcement = announcements.get(i);

            // 日付文字列をUNIXタイムスタンプに変換
            long timestamp = convertDateToTimestamp(dateStr);

            // 予定をカレンダーに追加
            eventRepository.addEvent(timestamp, announcement, "説明が必要な場合はここに記述");
        }
    }

    // 日付文字列をUNIXタイムスタンプに変換するメソッド
    private static long convertDateToTimestamp(String dateStr) {
        // ここで適切な日付形式に基づいて変換処理を実装
        // 例: yyyy/MM/dd 形式で処理
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
            Date date = sdf.parse(dateStr);
            return date.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date", e);
            return 0;
        }
    }

    // エラーを通知するメソッド
    private static void notifyError(TextDetectionCallback callback, Exception e) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
    }
}
