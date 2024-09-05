package com.example.calendar3;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

public class MenuFragment extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;
    private ActivityResultLauncher<Intent> _cameraLauncher;
    private ActivityResultLauncher<String> _pickImageLauncher;
    private ImageView imageView;
    private Button buttonClearImage;
    private Button cameraButton;
    private Button pickImageButton;
    private Button inButton;
    private Bitmap bitmap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fragment のレイアウトを膨らませる
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        // 各UI要素を初期化する
        buttonClearImage = view.findViewById(R.id.button_clear_image);
        imageView = view.findViewById(R.id.imageView);
        cameraButton = view.findViewById(R.id.button_take_picture);
        pickImageButton = view.findViewById(R.id.button_pick_image);
        inButton = view.findViewById(R.id.button_to_api);

        // 各ボタンにクリックリスナーを設定する
        buttonClearImage.setOnClickListener(v -> clearImage());
        cameraButton.setOnClickListener(v -> onCameraButtonClick());
        pickImageButton.setOnClickListener(v -> onPickImageButtonClick());
        inButton.setOnClickListener(v -> {
            if (bitmap != null) {
                byte[] imgBytes = convertBitmapToByteArray(bitmap);
                VisionHelper.detectTextFromByteArray(imgBytes, new VisionHelper.TextDetectionCallback() {
                    @Override
                    public void onTextDetected(String text) {
                        Log.d("MainActivity", "Detected text: " + text);
                    }

                    @Override
                    public void onDatesExtracted(List<String> dates) {
                        Log.d("MainActivity", "Extracted dates: " + dates);
                        Log.d("MainActivity", "日時抽出");
                    }

                    @Override
                    public void onAnnouncementsExtracted(List<String> announcements) {
                        Log.d("MainActivity", "Extracted announcements: " + announcements);
                        Log.d("MainActivity", "予定抽出");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("MainActivity", "Error detecting text", e);
                    }
                });
            } else {
                Log.d("MainActivity", "画像が選択されていません。");
            }
        });


        // カメラアクティビティ結果ランチャーを登録する
        _cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Bundle extras = data.getExtras();
                            if (extras != null) {
                                Bitmap bitmap = (Bitmap) extras.get("data");
                                if (bitmap != null) {
                                    imageView.setImageBitmap(bitmap);
                                    imageView.setVisibility(View.VISIBLE);
                                    buttonClearImage.setVisibility(View.VISIBLE);
                                    inButton.setVisibility(View.VISIBLE);
                                    cameraButton.setVisibility(View.GONE);
                                    pickImageButton.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                }
        );

        // 画像選択アクティビティ結果ランチャーを登録する
        _pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeStream(requireContext().getContentResolver().openInputStream(uri));
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                                imageView.setVisibility(View.VISIBLE);
                                buttonClearImage.setVisibility(View.VISIBLE);
                                inButton.setVisibility(View.VISIBLE);
                                cameraButton.setVisibility(View.GONE);
                                pickImageButton.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            Log.e("MenuFragment", "Error loading image", e);
                        }
                    }
                }
        );

        return view;
    }

    // 画像選択ボタンがクリックされた時の処理
    private void onPickImageButtonClick() {
        _pickImageLauncher.launch("image/*");
    }

    // カメラボタンがクリックされた時の処理
    private void onCameraButtonClick() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            _cameraLauncher.launch(cameraIntent);
        }
    }

    // 画像クリアボタンがクリックされた時の処理
    private void clearImage() {
        imageView.setImageURI(null);
        imageView.setVisibility(View.GONE);
        buttonClearImage.setVisibility(View.GONE);
        cameraButton.setVisibility(View.VISIBLE);
        pickImageButton.setVisibility(View.VISIBLE);
        inButton.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onCameraButtonClick();
            } else {
                Log.d("MenuFragment", "カメラのパーミッションが拒否されました。");
            }
        }
    }
    // Bitmap を byte[] に変換するメソッド
    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }

    // Uri から byte[] に変換するメソッド
    private byte[] convertUriToByteArray(ContentResolver contentResolver, Uri uri) {
        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                return convertBitmapToByteArray(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}
