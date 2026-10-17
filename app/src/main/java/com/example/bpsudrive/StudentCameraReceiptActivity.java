package com.example.bpsudrive;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.example.bpsudrive.databinding.ActivityStudentCameraReceiptBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

// Live Camera receipt capture activity using CameraX and ViewBinding with robust emulator/error handling.
public class StudentCameraReceiptActivity extends AppCompatActivity {

    private ActivityStudentCameraReceiptBinding binding;

    private long requestId;
    private String serviceName;
    private double serviceFee;
    private String refNo;

    private ImageCapture imageCapture;
    private Camera camera;
    private boolean isFlashOn = false;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentCameraReceiptBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnBottomBack.setOnClickListener(v -> finish());

        requestId = getIntent().getLongExtra("request_id", -1);
        serviceName = getIntent().getStringExtra("service_name");
        serviceFee = getIntent().getDoubleExtra("service_fee", 0.0);
        refNo = getIntent().getStringExtra("reference_no");

        // Register permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        binding.previewView.post(this::startCamera);
                    } else {
                        Toast.makeText(this, "Camera permission is required to capture receipts", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Register gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            String localPath = copyUriToInternalStorage(imageUri);
                            navigateToPreview(localPath);
                        }
                    }
                }
        );

        // Check camera permission or start camera safely
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            binding.previewView.post(this::startCamera);
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Shutter button captures photo via CameraX
        binding.btnCaptureReceipt.setOnClickListener(v -> capturePhoto());

        // Gallery button opens document/image picker
        binding.btnGallery.setOnClickListener(v -> {
            Intent pickPhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
            pickPhotoIntent.setType("image/*");
            galleryLauncher.launch(Intent.createChooser(pickPhotoIntent, "Select Receipt Image"));
        });

        // Flash toggle
        binding.btnFlash.setOnClickListener(v -> toggleFlash());
    }

    private void startCamera() {
        try {
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(binding.previewView.getSurfaceProvider());

                    imageCapture = new ImageCapture.Builder().build();

                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                    cameraProvider.unbindAll();
                    camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

                } catch (Exception e) {
                    Toast.makeText(this, "Camera not available on this device. Please use Gallery.", Toast.LENGTH_LONG).show();
                }
            }, ContextCompat.getMainExecutor(this));
        } catch (Exception e) {
            Toast.makeText(this, "Camera initialization failed. Please use Gallery.", Toast.LENGTH_LONG).show();
        }
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready or unavailable. Please use Gallery.", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(getFilesDir(), "receipt_" + System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                navigateToPreview(photoFile.getAbsolutePath());
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(StudentCameraReceiptActivity.this, "Photo capture failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleFlash() {
        if (camera != null && camera.getCameraInfo().hasFlashUnit()) {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);
        } else {
            Toast.makeText(this, "Flash not available", Toast.LENGTH_SHORT).show();
        }
    }

    private String copyUriToInternalStorage(Uri uri) {
        File file = new File(getFilesDir(), "receipt_" + System.currentTimeMillis() + ".jpg");
        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return file.getAbsolutePath();
        } catch (Exception e) {
            return null;
        }
    }

    private void navigateToPreview(String imagePath) {
        if (imagePath == null) {
            Toast.makeText(this, "Failed to load receipt image", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, StudentReceiptPreviewActivity.class);
        intent.putExtra("request_id", requestId);
        intent.putExtra("service_name", serviceName);
        intent.putExtra("service_fee", serviceFee);
        intent.putExtra("reference_no", refNo);
        intent.putExtra("image_path", imagePath);
        startActivity(intent);
    }
}
