package com.farmo.activities.commonActivities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.farmo.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Setup Change Password click listener
        View btnChangePassword = findViewById(R.id.setting_change_password);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        // Setup Change PIN click listener
        View btnChangePin = findViewById(R.id.setting_change_pin);
        if (btnChangePin != null) {
            btnChangePin.setOnClickListener(v -> showChangePinDialog());
        }

        // Setup Contact Us click listener
        View btnContactUs = findViewById(R.id.setting_contact_us);
        if (btnContactUs != null) {
            btnContactUs.setOnClickListener(v -> openEmail());
        }

        // Setup About Us click listener
        View btnAboutUs = findViewById(R.id.setting_about_us);
        if (btnAboutUs != null) {
            btnAboutUs.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, AboutUsActivity.class);
                startActivity(intent);
            });
        }

        // Check for update just Toaste Message:
        findViewById(R.id.setting_update).setOnClickListener(v -> {
            Toast.makeText(this, "Update is under development", Toast.LENGTH_SHORT).show();
        });

        // Edit Profile
        findViewById(R.id.setting_profile).setOnClickListener(v -> showEditProfile() );
    }

    private void openEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"officailfarmo@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - Farmo App");
        
        try {
            startActivity(Intent.createChooser(intent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChangePasswordDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        dialog.setContentView(dialogView);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }

        ImageView btnClose = dialogView.findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        View btnCancel = dialogView.findViewById(R.id.btnCancel);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void showChangePinDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_transaction_pin, null);
        dialog.setContentView(dialogView);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }

        ImageView btnClose = dialogView.findViewById(R.id.btnClosePin);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        View btnCancel = dialogView.findViewById(R.id.btnCancelPin);
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }


    private void showEditProfile() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        dialog.setContentView(dialogView);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }

        // ── Views ──
        ImageView btnClose       = dialogView.findViewById(R.id.btnClose);
        TextInputEditText etFirstName      = dialogView.findViewById(R.id.etFirstName);
        TextInputEditText etMiddleName     = dialogView.findViewById(R.id.etMiddleName);
        TextInputEditText etLastName       = dialogView.findViewById(R.id.etLastName);
        TextInputEditText etPhone          = dialogView.findViewById(R.id.etPhone);
        TextInputEditText etSecondaryPhone = dialogView.findViewById(R.id.etSecondaryPhone);
        TextInputEditText etDob            = dialogView.findViewById(R.id.etDob);
        AutoCompleteTextView spinnerSex       = dialogView.findViewById(R.id.spinnerSex);
        AutoCompleteTextView spinnerProvince  = dialogView.findViewById(R.id.spinnerProvince);
        AutoCompleteTextView spinnerDistrict  = dialogView.findViewById(R.id.spinnerDistrict);
        AutoCompleteTextView spinnerMunicipal = dialogView.findViewById(R.id.spinnerMunicipal);
        TextInputEditText etWard           = dialogView.findViewById(R.id.etWard);
        TextInputEditText etTole           = dialogView.findViewById(R.id.etTole);
        TextInputEditText etFacebook       = dialogView.findViewById(R.id.etFacebook);
        TextInputEditText etWhatsapp       = dialogView.findViewById(R.id.etWhatsapp);
        TextInputEditText etAbout          = dialogView.findViewById(R.id.etAbout);
        MaterialButton btnNext             = dialogView.findViewById(R.id.btnNext);
        MaterialButton btnCancel           = dialogView.findViewById(R.id.btnCancel);

        // ── Dropdowns ──
        String[] sexOptions       = {"Male", "Female", "Other"};
        String[] provinceOptions  = {"Province 1", "Madhesh", "Bagmati", "Gandaki", "Lumbini", "Karnali", "Sudurpashchim"};
        String[] districtOptions  = {"Kathmandu", "Lalitpur", "Bhaktapur"}; // update as needed
        String[] municipalOptions = {"Kathmandu Metropolitan", "Lalitpur Metropolitan"}; // update as needed

        spinnerSex.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, sexOptions));
        spinnerProvince.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, provinceOptions));
        spinnerDistrict.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, districtOptions));
        spinnerMunicipal.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, municipalOptions));

        // ── DOB Date Picker ──
        etDob.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                etDob.setText(date);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        // ── Close / Cancel ──
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        // ── Next → open password confirmation dialog ──
        if (btnNext != null) {
            btnNext.setOnClickListener(v -> {
                if (!validateEditProfileForm(etFirstName, etLastName, etPhone,
                        etDob, spinnerSex, spinnerProvince, spinnerDistrict,
                        spinnerMunicipal, etWard, etTole, etFacebook, etWhatsapp, etAbout)) {
                    return;
                }
                dialog.dismiss();
                showEditProfileCheckPassword();
            });
        }

        dialog.show();
    }

    // ─────────────────────────────────────────────────────────────
//  Password confirmation dialog
// ─────────────────────────────────────────────────────────────
    private void showEditProfileCheckPassword() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile_check_password, null);
        dialog.setContentView(dialogView);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setGravity(Gravity.BOTTOM);
        }

        ImageView btnBack              = dialogView.findViewById(R.id.btnBackToEdit);
        TextInputEditText etPassword   = dialogView.findViewById(R.id.etConfirmPassword);
        MaterialButton btnSubmit       = dialogView.findViewById(R.id.btnSubmit);
        MaterialButton btnCancel       = dialogView.findViewById(R.id.btnCancelConfirm);

        // Back arrow → go back to edit profile
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> {
                dialog.dismiss();
                showEditProfile();
            });
        }

        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String password = etPassword.getText() != null
                        ? etPassword.getText().toString().trim() : "";

                if (password.isEmpty()) {
                    etPassword.setError("Password is required");
                    etPassword.requestFocus();
                    return;
                }

                // TODO: call your API here, e.g.:
                // submitEditProfile(password);

                dialog.dismiss();
            });
        }

        dialog.show();
    }

    // ─────────────────────────────────────────────────────────────
//  Form validation helper
// ─────────────────────────────────────────────────────────────
    private boolean validateEditProfileForm(
            TextInputEditText etFirstName, TextInputEditText etLastName,
            TextInputEditText etPhone, TextInputEditText etDob,
            AutoCompleteTextView spinnerSex, AutoCompleteTextView spinnerProvince,
            AutoCompleteTextView spinnerDistrict, AutoCompleteTextView spinnerMunicipal,
            TextInputEditText etWard, TextInputEditText etTole,
            TextInputEditText etFacebook, TextInputEditText etWhatsapp,
            TextInputEditText etAbout) {

        String firstName  = etFirstName.getText()  != null ? etFirstName.getText().toString().trim()  : "";
        String lastName   = etLastName.getText()   != null ? etLastName.getText().toString().trim()   : "";
        String phone      = etPhone.getText()      != null ? etPhone.getText().toString().trim()      : "";
        String dob        = etDob.getText()        != null ? etDob.getText().toString().trim()        : "";
        String sex        = spinnerSex.getText().toString().trim();
        String province   = spinnerProvince.getText().toString().trim();
        String district   = spinnerDistrict.getText().toString().trim();
        String municipal  = spinnerMunicipal.getText().toString().trim();
        String ward       = etWard.getText()       != null ? etWard.getText().toString().trim()       : "";
        String tole       = etTole.getText()       != null ? etTole.getText().toString().trim()       : "";
        String facebook   = etFacebook.getText()   != null ? etFacebook.getText().toString().trim()   : "";
        String whatsapp   = etWhatsapp.getText()   != null ? etWhatsapp.getText().toString().trim()   : "";
        String about      = etAbout.getText()      != null ? etAbout.getText().toString().trim()      : "";

        if (firstName.isEmpty())  { etFirstName.setError("First name is required");   etFirstName.requestFocus();  return false; }
        if (lastName.isEmpty())   { etLastName.setError("Last name is required");     etLastName.requestFocus();   return false; }
        if (phone.isEmpty())      { etPhone.setError("Phone is required");            etPhone.requestFocus();      return false; }
        if (dob.isEmpty())        { etDob.setError("Date of birth is required");      etDob.requestFocus();        return false; }
        if (sex.isEmpty())        { spinnerSex.setError("Sex is required");           spinnerSex.requestFocus();   return false; }
        if (province.isEmpty())   { spinnerProvince.setError("Province is required"); spinnerProvince.requestFocus(); return false; }
        if (district.isEmpty())   { spinnerDistrict.setError("District is required"); spinnerDistrict.requestFocus(); return false; }
        if (municipal.isEmpty())  { spinnerMunicipal.setError("Municipal is required"); spinnerMunicipal.requestFocus(); return false; }
        if (ward.isEmpty())       { etWard.setError("Ward is required");              etWard.requestFocus();       return false; }
        if (tole.isEmpty())       { etTole.setError("Tole is required");              etTole.requestFocus();       return false; }
        if (facebook.isEmpty())   { etFacebook.setError("Facebook is required");      etFacebook.requestFocus();   return false; }
        if (whatsapp.isEmpty())   { etWhatsapp.setError("WhatsApp is required");      etWhatsapp.requestFocus();   return false; }
        if (about.isEmpty())      { etAbout.setError("About is required");            etAbout.requestFocus();      return false; }

        return true;
    }
}
