package com.example.pawalert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.pawalert.models.Report;
import com.example.pawalert.utils.AppUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ReportActivity — Allows a logged-in user to submit an injury / distress report
 * for an animal by providing:
 *  • A photo — either from the gallery (ACTION_GET_CONTENT) or taken directly
 *    with the device camera (ACTION_IMAGE_CAPTURE via FileProvider).
 *  • Their current GPS location.
 *  • Animal type and priority level.
 *
 * Camera flow (Android 7+ safe):
 *  1. Create an empty temp JPEG in getCacheDir()  →  photoFile
 *  2. Convert it to a content:// URI via FileProvider  →  cameraOutputUri
 *  3. Pass that URI to ACTION_IMAGE_CAPTURE via EXTRA_OUTPUT
 *  4. On success the camera has written the full-size photo to cameraOutputUri
 *  5. Decode cameraOutputUri like a gallery URI in compressImageToBase64()
 *
 * Image submission flow (unchanged from original):
 *  Compress → 800 px max side, 50% JPEG quality → Base64 → Firestore "reports"
 */
public class ReportActivity extends AppCompatActivity {

    // ── UI references ──────────────────────────────────────────────────────────
    private ImageView ivReportImage;
    private Button    btnUploadImage;
    private Button    btnCapturePhoto;
    private Button    btnGetLocation;
    private TextView  tvLocation;
    private Spinner   spinnerAnimalType;
    private Spinner   spinnerPriority;
    private Button    btnSubmitReport;

    // ── State ──────────────────────────────────────────────────────────────────
    /** URI of the image chosen from the gallery (null while camera capture is in use). */
    private Uri    selectedImageUri    = null;

    /**
     * Content-URI pointing to the temp file the camera wrote to.
     * Kept as a field so we can read it in the camera result callback without
     * relying on the (sometimes-null) Intent data returned by camera apps.
     */
    private Uri    cameraOutputUri     = null;

    private double selectedLat         = Double.NaN;
    private double selectedLng         = Double.NaN;

    // ── Firebase ───────────────────────────────────────────────────────────────
    private FirebaseAuth      mAuth;
    private FirebaseFirestore mFirestore;

    // ── Background thread for image compression ────────────────────────────────
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // ── Location ───────────────────────────────────────────────────────────────
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback            locationCallback;

    // ── Launchers ──────────────────────────────────────────────────────────────

    /**
     * Gallery / file picker launcher.
     * Clears cameraOutputUri so validation knows which source to use.
     */
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            cameraOutputUri  = null;   // gallery mode active
                            if (selectedImageUri != null) {
                                ivReportImage.setImageURI(selectedImageUri);
                                ivReportImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            }
                        }
                    }
            );

    /**
     * Camera capture launcher.
     * On success, cameraOutputUri already points to the full-size photo
     * written by the camera app; we just decode it for the preview.
     * Clears selectedImageUri so it isn't mistakenly used in submission.
     */
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && cameraOutputUri != null) {
                            selectedImageUri = null;   // camera mode active
                            // Show preview directly from the captured file URI
                            ivReportImage.setImageURI(cameraOutputUri);
                            ivReportImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        } else {
                            // User cancelled — reset so the old preview (if any) is cleared
                            // only if there was no prior successful selection
                            if (selectedImageUri == null && cameraOutputUri == null) {
                                ivReportImage.setImageResource(
                                        android.R.drawable.ic_menu_camera);
                            }
                        }
                    }
            );

    /** Location permission launcher. */
    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            fetchLocation();
                        } else {
                            AppUtils.showToast(this,
                                    "Location permission is required to report animal position.");
                        }
                    }
            );

    /** Camera permission launcher. */
    private final ActivityResultLauncher<String> cameraPermissionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    isGranted -> {
                        if (isGranted) {
                            launchCamera();
                        } else {
                            AppUtils.showToast(this,
                                    "Camera permission is required to take a photo.");
                        }
                    }
            );

    // ──────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        mAuth               = FirebaseAuth.getInstance();
        mFirestore          = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        setupSpinners();
        setListeners();
    }

    // ── View binding ───────────────────────────────────────────────────────────
    private void bindViews() {
        ivReportImage     = findViewById(R.id.iv_report_image);
        btnUploadImage    = findViewById(R.id.btn_upload_image);
        btnCapturePhoto   = findViewById(R.id.btn_capture_photo);
        btnGetLocation    = findViewById(R.id.btn_get_location);
        tvLocation        = findViewById(R.id.tv_location);
        spinnerAnimalType = findViewById(R.id.spinner_animal_type);
        spinnerPriority   = findViewById(R.id.spinner_priority);
        btnSubmitReport   = findViewById(R.id.btn_submit_report);
    }

    // ── Spinner setup ──────────────────────────────────────────────────────────
    private void setupSpinners() {
        String[] animalTypes = {"Select Animal Type", "Dog", "Cat", "Cow", "Other"};
        ArrayAdapter<String> animalAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, animalTypes);
        animalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerAnimalType.setAdapter(animalAdapter);

        String[] priorities = {"Select Priority", "HIGH", "NORMAL"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
    }

    // ── Listeners ──────────────────────────────────────────────────────────────
    private void setListeners() {
        btnUploadImage.setOnClickListener(v  -> openImagePicker());
        btnCapturePhoto.setOnClickListener(v -> requestCameraAndLaunch());
        btnGetLocation.setOnClickListener(v  -> requestLocation());
        btnSubmitReport.setOnClickListener(v -> validateAndSubmit());
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  GALLERY PICKER
    // ──────────────────────────────────────────────────────────────────────────

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Animal Photo"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  CAMERA CAPTURE
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Checks the CAMERA permission before launching the camera.
     * The permission is always listed in the manifest; on API ≥ 23 we also
     * need to request it at runtime.
     */
    private void requestCameraAndLaunch() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Creates a uniquely-named temp file in the app's cache directory,
     * converts it to a content:// URI via FileProvider, and fires
     * ACTION_IMAGE_CAPTURE so the camera writes the full-resolution photo there.
     *
     * We deliberately NOT use EXTRA_OUTPUT-less capture (which returns only a
     * tiny thumbnail Bitmap) so we always get a full-quality photo.
     */
    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            // FileProvider authority matches the one declared in AndroidManifest.xml
            cameraOutputUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    photoFile);

            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraOutputUri);

            // Grant the camera app temporary write permission to the URI
            cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                cameraLauncher.launch(cameraIntent);
            } else {
                AppUtils.showToast(this, "No camera app found on this device.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            AppUtils.showToast(this, "Could not prepare camera. Please try again.");
        }
    }

    /**
     * Creates a uniquely-named empty JPEG file in the app's cache directory.
     * The system camera will write the captured photo into this file.
     *
     * @return the newly created (empty) File
     * @throws IOException if the file could not be created
     */
    private File createImageFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String fileName  = "PawAlert_" + timestamp + "_";
        File   cacheDir  = getCacheDir();
        // createTempFile appends a random suffix, ensuring uniqueness
        return File.createTempFile(fileName, ".jpg", cacheDir);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  LOCATION
    // ──────────────────────────────────────────────────────────────────────────

    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fetchLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Uses FusedLocationProviderClient to retrieve a fresh high-accuracy location.
     * Falls back to requesting a fresh GPS fix if no cached fix is available.
     */
    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        btnGetLocation.setEnabled(false);
        btnGetLocation.setText("Fetching…");

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                        requestFreshLocation();
                    }
                })
                .addOnFailureListener(e -> requestFreshLocation());
    }

    private void requestFreshLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000L)
                .setMaxUpdates(1)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (result.getLastLocation() != null) {
                    onLocationReceived(
                            result.getLastLocation().getLatitude(),
                            result.getLastLocation().getLongitude());
                } else {
                    AppUtils.showToast(ReportActivity.this, "Could not get location. Try again.");
                    resetLocationButton();
                }
                fusedLocationClient.removeLocationUpdates(locationCallback);
            }
        };

        fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void onLocationReceived(double lat, double lng) {
        selectedLat = lat;
        selectedLng = lng;
        tvLocation.setText(String.format("📍  Lat: %.6f,  Lng: %.6f", lat, lng));
        tvLocation.setTextColor(0xFFEAEAEA);
        resetLocationButton();
    }

    private void resetLocationButton() {
        btnGetLocation.setEnabled(true);
        btnGetLocation.setText("📍  Get My Location");
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  VALIDATION + SUBMIT
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if the user has a valid image from either source
     * (gallery URI or camera capture URI).
     */
    private boolean hasImage() {
        return selectedImageUri != null || cameraOutputUri != null;
    }

    private void validateAndSubmit() {
        if (!hasImage()) {
            AppUtils.showToast(this, "Please select or capture an animal photo.");
            return;
        }
        if (Double.isNaN(selectedLat) || Double.isNaN(selectedLng)) {
            AppUtils.showToast(this, "Please fetch your current location.");
            return;
        }
        if (spinnerAnimalType.getSelectedItemPosition() == 0) {
            AppUtils.showToast(this, "Please select an animal type.");
            return;
        }
        if (spinnerPriority.getSelectedItemPosition() == 0) {
            AppUtils.showToast(this, "Please select a priority level.");
            return;
        }

        setFormEnabled(false);
        btnSubmitReport.setText("Processing…");
        compressAndSaveReport();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  IMAGE COMPRESSION → BASE64 → FIRESTORE
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Chooses the correct image source URI (gallery or camera), runs
     * compression on a background thread, then saves to Firestore on
     * the main thread.
     */
    private void compressAndSaveReport() {
        if (mAuth.getCurrentUser() == null) {
            AppUtils.showToast(this, "You must be logged in to submit a report.");
            resetSubmitButton();
            return;
        }

        // Use whichever source is active
        final Uri imageUri = (selectedImageUri != null) ? selectedImageUri : cameraOutputUri;
        if (imageUri == null) {
            AppUtils.showToast(this, "No image selected.");
            resetSubmitButton();
            return;
        }

        final String uid = mAuth.getCurrentUser().getUid();

        executor.execute(() -> {
            String base64Image = compressImageToBase64(imageUri);

            runOnUiThread(() -> {
                if (base64Image == null) {
                    AppUtils.showToast(this, "Failed to process image. Please try again.");
                    resetSubmitButton();
                    return;
                }

                int sizeKB = base64Image.length() / 1024;
                if (sizeKB > 500) {
                    AppUtils.showToast(this,
                            "Image is too large after compression (" + sizeKB + " KB). "
                            + "Please select a smaller photo.");
                    resetSubmitButton();
                    return;
                }

                saveReportToFirestore(base64Image, uid);
            });
        });
    }

    /**
     * Converts the image at {@code uri} to a compressed Base64 string.
     *
     * Steps:
     *  1. Decode contentResolver → Bitmap
     *  2. Scale to max 800 px on the longest side (maintains aspect ratio)
     *  3. Compress JPEG at 50% quality
     *  4. Base64-encode
     *
     * Works identically for both gallery URIs and FileProvider camera URIs.
     *
     * @return Base64-encoded JPEG string, or null on any error.
     */
    private String compressImageToBase64(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            if (bitmap == null) return null;

            final int MAX_SIZE = 800;
            int width  = bitmap.getWidth();
            int height = bitmap.getHeight();
            float ratio = (float) width / height;

            if (width > height) {
                width  = MAX_SIZE;
                height = Math.round(MAX_SIZE / ratio);
            } else {
                height = MAX_SIZE;
                width  = Math.round(MAX_SIZE * ratio);
            }

            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, true);
            bitmap.recycle();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            scaled.recycle();

            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Writes the validated report document to Firestore "reports" collection.
     */
    private void saveReportToFirestore(String imageBase64, String uid) {
        String animalType = spinnerAnimalType.getSelectedItem().toString();
        String priority   = spinnerPriority.getSelectedItem().toString();

        Map<String, Object> locationMap = new HashMap<>();
        locationMap.put("lat", selectedLat);
        locationMap.put("lng", selectedLng);

        Map<String, Object> report = new HashMap<>();
        report.put("imageBase64", imageBase64);
        report.put("location",    locationMap);
        report.put("animalType",  animalType);
        report.put("priority",    priority);
        report.put("status",      Report.STATUS_PENDING_APPROVAL);
        report.put("createdBy",   uid);
        report.put("timestamp",   FieldValue.serverTimestamp());

        btnSubmitReport.setText("Saving…");

        mFirestore.collection("reports")
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    documentReference.update("reportId", documentReference.getId());
                    AppUtils.showLongToast(this, "✅ Report submitted successfully!");
                    cleanupTempCameraFile();
                    finish();
                })
                .addOnFailureListener(e -> {
                    AppUtils.showToast(this, "Failed to save report: " + e.getMessage());
                    resetSubmitButton();
                });
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  HELPERS
    // ──────────────────────────────────────────────────────────────────────────

    private void setFormEnabled(boolean enabled) {
        btnUploadImage.setEnabled(enabled);
        btnCapturePhoto.setEnabled(enabled);
        btnGetLocation.setEnabled(enabled);
        spinnerAnimalType.setEnabled(enabled);
        spinnerPriority.setEnabled(enabled);
        btnSubmitReport.setEnabled(enabled);
    }

    private void resetSubmitButton() {
        setFormEnabled(true);
        btnSubmitReport.setText("Submit Report");
    }

    /**
     * Deletes the temp camera file from the cache directory once the report has
     * been successfully submitted (or when the activity is destroyed).
     * This keeps the app's cache clean.
     */
    private void cleanupTempCameraFile() {
        if (cameraOutputUri != null) {
            try {
                // Convert content URI back to file path via ContentResolver
                getContentResolver().delete(cameraOutputUri, null, null);
            } catch (Exception ignored) { /* best-effort cleanup */ }
            cameraOutputUri = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        cleanupTempCameraFile();
    }
}
