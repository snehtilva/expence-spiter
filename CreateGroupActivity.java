package com.example.expenseapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expenseapp.database.ExpenseRepository;
import com.example.expenseapp.models.Group;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;

public class CreateGroupActivity extends AppCompatActivity {
    private TextInputEditText etGroupName;
    private Button btnCreate;
    private ProgressBar progressBar;
    private ExpenseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        etGroupName = findViewById(R.id.etGroupName);
        btnCreate = findViewById(R.id.btnCreate);
        progressBar = findViewById(R.id.progressBar);

        repository = new ExpenseRepository(this);

        btnCreate.setOnClickListener(v -> createGroup());
    }

    private void createGroup() {
        String groupName = etGroupName.getText().toString().trim();

        if (groupName.isEmpty()) {
            Toast.makeText(this, "Enter group name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user info from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("ExpenseApp", MODE_PRIVATE);
        String yourName = prefs.getString("current_user_name", "");
        String yourEmail = prefs.getString("current_user_email", "");

        if (yourName.isEmpty() || yourEmail.isEmpty()) {
            Toast.makeText(this, "User info not found. Please restart app.", Toast.LENGTH_SHORT).show();
            return;
        }

        String code = generateCode();
        showLoading(true);

        repository.createGroupWithEmail(groupName, code, yourName, yourEmail,
                new ExpenseRepository.RepositoryCallback<Group>() {
                    @Override
                    public void onSuccess(Group group) {
                        showLoading(false);
                        Toast.makeText(CreateGroupActivity.this,
                                "Group created!\nCode: " + code + "\nShare this code!",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        Toast.makeText(CreateGroupActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateCode() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnCreate.setEnabled(!show);
    }
}

// Location: app/src/main/java/com/example/expenseapp/CreateGroupActivity.java