package com.example.musicplayer;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.submitButton.setOnClickListener(view -> displaySubmittedData());
    }

    private void displaySubmittedData() {
        String name = binding.nameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String course = getSelectedCourse();
        String hobbies = getSelectedHobbies();

        if (TextUtils.isEmpty(name)) {
            binding.nameInputLayout.setError(getString(R.string.error_name_required));
        } else {
            binding.nameInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(email)) {
            binding.emailInputLayout.setError(getString(R.string.error_email_required));
        } else {
            binding.emailInputLayout.setError(null);
        }

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, R.string.validation_message, Toast.LENGTH_SHORT).show();
            return;
        }

        String result = getString(R.string.submitted_data_format, name, email, course, hobbies);
        binding.resultTitle.setText(R.string.submitted_data_heading);
        binding.resultText.setText(result);
    }

    private String getSelectedCourse() {
        int checkedId = binding.courseRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radioBca) {
            return getString(R.string.course_bca);
        }
        if (checkedId == R.id.radioMca) {
            return getString(R.string.course_mca);
        }
        return getString(R.string.not_selected);
    }

    private String getSelectedHobbies() {
        List<String> hobbies = new ArrayList<>();
        addHobbyIfChecked(binding.checkboxReading, hobbies);
        addHobbyIfChecked(binding.checkboxMusic, hobbies);
        addHobbyIfChecked(binding.checkboxSports, hobbies);
        addHobbyIfChecked(binding.checkboxTraveling, hobbies);

        if (hobbies.isEmpty()) {
            return getString(R.string.no_hobbies_selected);
        }

        return TextUtils.join(", ", hobbies);
    }

    private void addHobbyIfChecked(CheckBox checkBox, List<String> hobbies) {
        if (checkBox.isChecked()) {
            hobbies.add(checkBox.getText().toString());
        }
    }
}
