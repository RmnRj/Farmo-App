package com.farmo.activities.commonActivities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.farmo.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class KYCVerificationActivity extends AppCompatActivity {

    private RadioGroup rgDocumentType;
    private TextInputEditText etDocumentNumber;
    private TextInputLayout tilDocumentNumber;

    private CardView cardFrontView;
    private ImageView ivFrontView;
    private LinearLayout layoutFrontPlaceholder, layoutFrontOverlay;
    private Button btnViewFront, btnRemoveFront;
    private ImageView ivFrontCheck;

    private TextView tvBackLabel;
    private CardView cardBackView;
    private ImageView ivBackView;
    private LinearLayout layoutBackPlaceholder, layoutBackOverlay;
    private Button btnViewBack, btnRemoveBack;
    private ImageView ivBackCheck;
    private TextView tvBackOptional;

    private CardView cardSelfieView;
    private ImageView ivSelfieView;
    private LinearLayout layoutSelfiePlaceholder, layoutSelfieOverlay;
    private Button btnViewSelfie, btnRemoveSelfie;
    private ImageView ivSelfieCheck;

    private CheckBox cbTerms;
    private Button btnSubmit;
    private ProgressBar progressBar;

    // URIs stored for View button and API upload — never null when image is selected
    private Uri frontImageUri = null;
    private Uri backImageUri = null;
    private Uri selfieImageUri = null;

    // ✅ FIX CAMERA: Pre-created file URI where camera saves FULL resolution photo
    private Uri cameraOutputUri = null;

    private String documentType = "Citizenship Card";
    private boolean isFrontImageSelected = false;
    private boolean isBackImageSelected = false;
    private boolean isSelfieImageSelected = false;
    private boolean isBackRequired = true;

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private int currentUploadingImage = 0; // 1=Front, 2=Back, 3=Selfie

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kyc_verification_v2);
        initializeViews();
        setupListeners();
        setupActivityResultLaunchers();
        updateBackImageRequirement();
    }

    private void initializeViews() {
        rgDocumentType       = findViewById(R.id.rgDocumentType);
        etDocumentNumber     = findViewById(R.id.etDocumentNumber);
        tilDocumentNumber    = findViewById(R.id.tilDocumentNumber);

        cardFrontView        = findViewById(R.id.cardFrontView);
        ivFrontView          = findViewById(R.id.ivFrontView);
        layoutFrontPlaceholder = findViewById(R.id.layoutFrontPlaceholder);
        layoutFrontOverlay   = findViewById(R.id.layoutFrontOverlay);
        btnViewFront         = findViewById(R.id.btnViewFront);
        btnRemoveFront       = findViewById(R.id.btnRemoveFront);
        ivFrontCheck         = findViewById(R.id.ivFrontCheck);

        tvBackLabel          = findViewById(R.id.tvBackLabel);
        cardBackView         = findViewById(R.id.cardBackView);
        ivBackView           = findViewById(R.id.ivBackView);
        layoutBackPlaceholder = findViewById(R.id.layoutBackPlaceholder);
        layoutBackOverlay    = findViewById(R.id.layoutBackOverlay);
        btnViewBack          = findViewById(R.id.btnViewBack);
        btnRemoveBack        = findViewById(R.id.btnRemoveBack);
        ivBackCheck          = findViewById(R.id.ivBackCheck);
        tvBackOptional       = findViewById(R.id.tvBackOptional);

        cardSelfieView       = findViewById(R.id.cardSelfieView);
        ivSelfieView         = findViewById(R.id.ivSelfieView);
        layoutSelfiePlaceholder = findViewById(R.id.layoutSelfiePlaceholder);
        layoutSelfieOverlay  = findViewById(R.id.layoutSelfieOverlay);
        btnViewSelfie        = findViewById(R.id.btnViewSelfie);
        btnRemoveSelfie      = findViewById(R.id.btnRemoveSelfie);
        ivSelfieCheck        = findViewById(R.id.ivSelfieCheck);

        cbTerms   = findViewById(R.id.cbTerms);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        rgDocumentType.setOnCheckedChangeListener((group, checkedId) -> {
            if      (checkedId == R.id.rbCitizenship)  { documentType = "Citizenship Card"; isBackRequired = true; }
            else if (checkedId == R.id.rbPassport)     { documentType = "Passport";         isBackRequired = false; }
            else if (checkedId == R.id.rbDrivingLicence){ documentType = "Driving Licence"; isBackRequired = true; }
            else if (checkedId == R.id.rbNationalId)   { documentType = "National ID Card"; isBackRequired = true; }
            updateBackImageRequirement();
            validateForm();
        });

        etDocumentNumber.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { validateForm(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        // --- Front ---
        cardFrontView.setOnClickListener(v -> {
            if (!isFrontImageSelected) { currentUploadingImage = 1; showImageSourceDialog(); }
        });
        btnViewFront.setOnClickListener(v -> showFullscreenImage(frontImageUri, "Document Front Side"));
        btnRemoveFront.setOnClickListener(v -> removeFrontImage());

        // --- Back ---
        cardBackView.setOnClickListener(v -> {
            if (!isBackImageSelected) { currentUploadingImage = 2; showImageSourceDialog(); }
        });
        btnViewBack.setOnClickListener(v -> showFullscreenImage(backImageUri, "Document Back Side"));
        btnRemoveBack.setOnClickListener(v -> removeBackImage());

        // --- Selfie ---
        cardSelfieView.setOnClickListener(v -> {
            if (!isSelfieImageSelected) { currentUploadingImage = 3; showImageSourceDialog(); }
        });
        btnViewSelfie.setOnClickListener(v -> showFullscreenImage(selfieImageUri, "Selfie with Document"));
        btnRemoveSelfie.setOnClickListener(v -> removeSelfieImage());

        cbTerms.setOnCheckedChangeListener((btn, checked) -> validateForm());
        btnSubmit.setOnClickListener(v -> submitKYCVerification());
    }

    private void setupActivityResultLaunchers() {

        // Gallery picker result
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedUri = result.getData().getData();
                        if (selectedUri != null) {
                            // Keep persistent read permission so URI stays valid after restart
                            try {
                                getContentResolver().takePersistableUriPermission(
                                        selectedUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            } catch (Exception ignored) {}
                            loadAndDisplayImage(selectedUri);
                        }
                    }
                });

        // ✅ FIX CAMERA: Result just reads from the pre-created file URI (full resolution)
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (cameraOutputUri != null) {
                            loadAndDisplayImage(cameraOutputUri);
                        }
                    }
                });
    }

    private void updateBackImageRequirement() {
        if (isBackRequired) {
            tvBackLabel.setText("Document Back Side *");
            tvBackOptional.setVisibility(View.GONE);
        } else {
            tvBackLabel.setText("Document Back Side");
            tvBackOptional.setVisibility(View.VISIBLE);
        }
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        // Camera
                        if (checkCameraPermission()) openCamera();
                        else requestCameraPermission();
                    } else {
                        // Gallery — Android 13+ needs no permission
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            openGallery();
                        } else {
                            if (checkStoragePermission()) openGallery();
                            else requestStoragePermission();
                        }
                    }
                })
                .show();
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
    }

    /**
     * ✅ FIX CAMERA QUALITY — Complete fix:
     *
     * PROBLEM (old code):
     *   Intent camera = new Intent(ACTION_IMAGE_CAPTURE);
     *   // No EXTRA_OUTPUT set
     *   // Camera returns extras.get("data") = 160x120 thumbnail ONLY = blurry image
     *
     * SOLUTION (new code):
     *   1. Create a temp file in cache directory
     *   2. Wrap it in a FileProvider URI (required for Android 7+)
     *   3. Pass as EXTRA_OUTPUT — camera saves FULL resolution photo to this file
     *   4. On result, read back from same URI — original quality, zero compression
     */
    private void openCamera() {
        try {
            // Step 1: Create temp file to receive the full-res photo
            String fileName = "KYC_" + new SimpleDateFormat("yyyyMMdd_HHmmss",
                    Locale.getDefault()).format(new Date());
            File photoFile = File.createTempFile(fileName, ".jpg", getCacheDir());

            // Step 2: Wrap in FileProvider URI — required for camera to write to app's cache
            cameraOutputUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",   // must match AndroidManifest authority
                    photoFile
            );

            // Step 3: Tell camera to write full photo to our file
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(cameraIntent);
            } else {
                Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Cannot prepare camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    /**
     * ✅ FIX IMAGE QUALITY — Load at original quality for BOTH gallery and camera:
     *   inSampleSize = 1  → no pixel downscaling
     *   ARGB_8888         → highest color depth (8 bits per channel)
     *   No compress call  → zero quality loss
     *
     * Also fixes camera rotation: phones often save photos rotated 90°.
     * We read the EXIF orientation and correct it before display.
     */
    private void loadAndDisplayImage(Uri uri) {
        try {
            // Load at full quality
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(this, "Cannot open image", Toast.LENGTH_SHORT).show();
                return;
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;                             // no downscaling
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;  // max color quality
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            if (bitmap == null) {
                Toast.makeText(this, "Could not decode image", Toast.LENGTH_SHORT).show();
                return;
            }

            // Fix rotation for camera photos (EXIF orientation correction)
            bitmap = correctRotation(bitmap, uri);

            // Store URI and show image
            setImageToView(uri, bitmap);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Reads EXIF orientation from the image and rotates the bitmap to correct it.
     * Camera photos are often stored rotated — this fixes the display.
     */
    private Bitmap correctRotation(Bitmap bitmap, Uri uri) {
        try {
            InputStream exifStream = getContentResolver().openInputStream(uri);
            if (exifStream == null) return bitmap;

            ExifInterface exif = new ExifInterface(exifStream);
            exifStream.close();

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int degrees = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:  degrees = 90;  break;
                case ExifInterface.ORIENTATION_ROTATE_180: degrees = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_270: degrees = 270; break;
            }

            if (degrees != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(degrees);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception ignored) {}
        return bitmap;
    }

    private void setImageToView(Uri imageUri, Bitmap bitmap) {
        if (currentUploadingImage == 1) {
            frontImageUri = imageUri;
            isFrontImageSelected = true;
            ivFrontView.setImageBitmap(bitmap);
            layoutFrontPlaceholder.setVisibility(View.GONE);
            layoutFrontOverlay.setVisibility(View.VISIBLE);
            ivFrontCheck.setVisibility(View.VISIBLE);

        } else if (currentUploadingImage == 2) {
            backImageUri = imageUri;
            isBackImageSelected = true;
            ivBackView.setImageBitmap(bitmap);
            layoutBackPlaceholder.setVisibility(View.GONE);
            layoutBackOverlay.setVisibility(View.VISIBLE);
            ivBackCheck.setVisibility(View.VISIBLE);

        } else if (currentUploadingImage == 3) {
            selfieImageUri = imageUri;
            isSelfieImageSelected = true;
            ivSelfieView.setImageBitmap(bitmap);
            layoutSelfiePlaceholder.setVisibility(View.GONE);
            layoutSelfieOverlay.setVisibility(View.VISIBLE);
            ivSelfieCheck.setVisibility(View.VISIBLE);
        }
        validateForm();
    }

    /**
     * ✅ FIX VIEW BUTTON — Shows uploaded image in a fullscreen dialog.
     *
     * PROBLEM (old code): Passed a Bitmap variable that could be null/recycled.
     * SOLUTION: Reads fresh from the stored URI every time. Works for both
     *           gallery content:// URIs and camera file:// URIs.
     *
     * Uses a simple Dialog with a black background so the image is clearly visible.
     */
    private void showFullscreenImage(Uri imageUri, String title) {
        if (imageUri == null) {
            Toast.makeText(this, "No image uploaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load fresh from URI at full quality
        Bitmap bitmap = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream != null) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inSampleSize = 1;
                opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
                inputStream.close();
                bitmap = correctRotation(bitmap, imageUri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bitmap == null) {
            Toast.makeText(this, "Cannot preview image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build fullscreen dialog
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Create layout programmatically — no dependency on dialog XML layout IDs
        // This ensures it always works regardless of your dialog_image_preview.xml structure
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);

        // Title bar
        LinearLayout titleBar = new LinearLayout(this);
        titleBar.setOrientation(LinearLayout.HORIZONTAL);
        titleBar.setBackgroundColor(Color.parseColor("#CC000000"));
        titleBar.setPadding(32, 24, 32, 24);
        titleBar.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(17);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tvTitle.setLayoutParams(titleParams);

        Button btnClose = new Button(this);
        btnClose.setText("✕ Close");
        btnClose.setTextColor(Color.WHITE);
        btnClose.setBackgroundColor(Color.parseColor("#661B5E20"));
        btnClose.setPadding(24, 12, 24, 12);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        titleBar.addView(tvTitle);
        titleBar.addView(btnClose);

        // Image view — fills remaining space
        ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundColor(Color.BLACK);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        imageView.setLayoutParams(imgParams);

        root.addView(titleBar);
        root.addView(imageView);

        dialog.setContentView(root);
        dialog.show();
    }

    private void removeFrontImage() {
        frontImageUri = null;
        isFrontImageSelected = false;
        ivFrontView.setImageDrawable(null);
        layoutFrontPlaceholder.setVisibility(View.VISIBLE);
        layoutFrontOverlay.setVisibility(View.GONE);
        ivFrontCheck.setVisibility(View.GONE);
        validateForm();
    }

    private void removeBackImage() {
        backImageUri = null;
        isBackImageSelected = false;
        ivBackView.setImageDrawable(null);
        layoutBackPlaceholder.setVisibility(View.VISIBLE);
        layoutBackOverlay.setVisibility(View.GONE);
        ivBackCheck.setVisibility(View.GONE);
        validateForm();
    }

    private void removeSelfieImage() {
        selfieImageUri = null;
        isSelfieImageSelected = false;
        ivSelfieView.setImageDrawable(null);
        layoutSelfiePlaceholder.setVisibility(View.VISIBLE);
        layoutSelfieOverlay.setVisibility(View.GONE);
        ivSelfieCheck.setVisibility(View.GONE);
        validateForm();
    }

    private void validateForm() {
        boolean isValid = true;
        String docNumber = etDocumentNumber.getText().toString().trim();

        if (docNumber.isEmpty()) {
            tilDocumentNumber.setError("Document number is required");
            isValid = false;
        } else if (docNumber.length() < 5) {
            tilDocumentNumber.setError("Must be at least 5 characters");
            isValid = false;
        } else {
            tilDocumentNumber.setError(null);
        }

        if (!isFrontImageSelected) isValid = false;
        if (isBackRequired && !isBackImageSelected) isValid = false;
        if (!isSelfieImageSelected) isValid = false;
        if (!cbTerms.isChecked()) isValid = false;

        btnSubmit.setEnabled(isValid);
    }

    private void submitKYCVerification() {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submitting...");

        // Integrate with KYCApiService using the stored URIs
        // KYCDocument doc = new KYCDocument(documentType, docNumber, frontImageUri, backImageUri);
        // new KYCApiService(this).uploadKYCDocument(doc, callback);

        new android.os.Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);
            btnSubmit.setText("Submit for Verification");
            new AlertDialog.Builder(this)
                    .setTitle("✓ Submitted Successfully")
                    .setMessage("Your KYC documents have been submitted.\n\nVerification takes 24-48 hours.")
                    .setPositiveButton("OK", (d, w) -> finish())
                    .setCancelable(false)
                    .show();
        }, 2000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}