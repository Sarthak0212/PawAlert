package com.example.pawalert;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pawalert.models.Report;
import com.example.pawalert.utils.AppUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ReportDetailActivity extends AppCompatActivity {

    private ImageView ivDetailImage;
    private TextView  tvDetailAnimalType;
    private TextView  tvDetailPriority;
    private TextView  tvDetailLocation;
    private TextView  tvDetailStatus;
    private TextView  tvDetailCreatedBy;

    // Volunteer Actions UI
    private LinearLayout llVolunteerActions, llResolveSection;
    private Button btnMarkInProgress, btnUploadProof, btnRequestResolution;
    private EditText etRescueDescription;
    private ImageView ivProofImage;
    private LinearLayout llRejectionSection;
    private TextView tvRejectionReason;

    private String reportId;
    private String currentStatus;
    private Uri selectedProofUri = null;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            selectedProofUri = result.getData().getData();
                            if (selectedProofUri != null) {
                                ivProofImage.setImageURI(selectedProofUri);
                            }
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        bindViews();
        populateFromIntent();
        setupVolunteerActions();
    }

    private void bindViews() {
        ivDetailImage      = findViewById(R.id.iv_detail_image);
        tvDetailAnimalType = findViewById(R.id.tv_detail_animal_type);
        tvDetailPriority   = findViewById(R.id.tv_detail_priority);
        tvDetailLocation   = findViewById(R.id.tv_detail_location);
        tvDetailStatus     = findViewById(R.id.tv_detail_status);
        tvDetailCreatedBy  = findViewById(R.id.tv_detail_created_by);

        llVolunteerActions = findViewById(R.id.ll_volunteer_actions);
        llResolveSection   = findViewById(R.id.ll_resolve_section);
        btnMarkInProgress  = findViewById(R.id.btn_mark_in_progress);
        btnUploadProof     = findViewById(R.id.btn_upload_proof);
        btnRequestResolution = findViewById(R.id.btn_request_resolution);
        etRescueDescription = findViewById(R.id.etRescueDescription);
        ivProofImage       = findViewById(R.id.iv_proof_image);
        llRejectionSection  = findViewById(R.id.ll_rejection_section);
        tvRejectionReason   = findViewById(R.id.tv_rejection_reason);
    }

    private void populateFromIntent() {
        reportId           = getIntent().getStringExtra("reportId");
        String imageBase64 = getIntent().getStringExtra("imageBase64");
        String animalType  = getIntent().getStringExtra("animalType");
        String priority    = getIntent().getStringExtra("priority");
        currentStatus      = getIntent().getStringExtra("status");
        String createdBy   = getIntent().getStringExtra("createdBy");
        double lat         = getIntent().getDoubleExtra("lat", 0.0);
        double lng         = getIntent().getDoubleExtra("lng", 0.0);
        String rejReason   = getIntent().getStringExtra("rejectionReason");

        if (rejReason != null && !rejReason.isEmpty()) {
            llRejectionSection.setVisibility(View.VISIBLE);
            tvRejectionReason.setText(rejReason);
        } else {
            llRejectionSection.setVisibility(View.GONE);
        }

        if (imageBase64 != null && !imageBase64.isEmpty()) {
            try {
                byte[] bytes  = Base64.decode(imageBase64, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    ivDetailImage.setImageBitmap(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tvDetailAnimalType.setText(animalType != null ? animalType : "—");
        tvDetailPriority.setText(priority   != null ? priority   : "—");
        tvDetailStatus.setText(currentStatus     != null ? currentStatus     : "—");
        tvDetailCreatedBy.setText("Reported by UID:\n" + (createdBy != null ? createdBy : "—"));
        tvDetailLocation.setText(String.format("📍  Lat: %.6f\n      Lng: %.6f", lat, lng));
    }

    private void setupVolunteerActions() {
        // Assume if user got here via VolunteerHomeActivity, they are a volunteer.
        // We will just show/hide buttons based on the `currentStatus`.
        if (Report.STATUS_APPROVED.equals(currentStatus)) {
            llVolunteerActions.setVisibility(View.VISIBLE);
            btnMarkInProgress.setVisibility(View.VISIBLE);
            llResolveSection.setVisibility(View.GONE);
        } else if (Report.STATUS_IN_PROGRESS.equals(currentStatus)) {
            llVolunteerActions.setVisibility(View.VISIBLE);
            btnMarkInProgress.setVisibility(View.GONE);
            llResolveSection.setVisibility(View.VISIBLE);
        } else {
            llVolunteerActions.setVisibility(View.GONE);
        }

        btnMarkInProgress.setOnClickListener(v -> markInProgress());
        btnUploadProof.setOnClickListener(v -> openImagePicker());
        btnRequestResolution.setOnClickListener(v -> submitResolution());
    }

    private void markInProgress() {
        if (reportId == null) return;

        btnMarkInProgress.setEnabled(false);
        String volunteerUid = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getUid() : "";

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", Report.STATUS_IN_PROGRESS);
        updates.put("volunteerId", volunteerUid);

        db.collection("reports").document(reportId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.showToast(this, "Status updated to In Progress");
                    currentStatus = Report.STATUS_IN_PROGRESS;
                    tvDetailStatus.setText(currentStatus);
                    setupVolunteerActions();
                })
                .addOnFailureListener(e -> {
                    AppUtils.showToast(this, "Failed to update status");
                    btnMarkInProgress.setEnabled(true);
                });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Proof Image"));
    }

    private void submitResolution() {
        String description = etRescueDescription.getText().toString().trim();
        
        if (description.isEmpty()) {
            etRescueDescription.setError("Description is required");
            etRescueDescription.requestFocus();
            return;
        }

        if (selectedProofUri == null) {
            AppUtils.showToast(this, "Please select an image first as proof");
            return;
        }

        btnRequestResolution.setEnabled(false);
        btnRequestResolution.setText("Submitting...");

        new Thread(() -> {
            String base64 = compressImageToBase64(selectedProofUri);
            runOnUiThread(() -> {
                if (base64 != null) {
                    performResolutionUpdate(base64, description);
                } else {
                    AppUtils.showToast(this, "Failed to process image");
                    btnRequestResolution.setEnabled(true);
                    btnRequestResolution.setText("Request Resolution Approval");
                }
            });
        }).start();
    }

    private void performResolutionUpdate(String base64Image, String description) {
        if (reportId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", Report.STATUS_RESOLVED_PENDING);
        updates.put("resolvedProofImage", base64Image);
        updates.put("rescueDescription", description);
        updates.put("timestamp", System.currentTimeMillis());

        db.collection("reports").document(reportId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    AppUtils.showToast(this, "Resolution sent for approval");
                    currentStatus = Report.STATUS_RESOLVED_PENDING;
                    tvDetailStatus.setText(currentStatus);
                    setupVolunteerActions();
                    btnRequestResolution.setText("Request Resolution Approval");
                    btnRequestResolution.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    AppUtils.showToast(this, "Failed to submit resolution");
                    btnRequestResolution.setEnabled(true);
                    btnRequestResolution.setText("Request Resolution Approval");
                });
    }

    private String compressImageToBase64(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            if (bitmap == null) return null;

            int MAX_SIZE = 800;
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
}
