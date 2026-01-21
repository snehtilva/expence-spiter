package com.example.expenseapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expenseapp.database.ExpenseRepository;
import com.example.expenseapp.models.Group;
import com.example.expenseapp.models.Member;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class JoinGroupActivity extends AppCompatActivity {
    private static final String TAG = "JoinGroupActivity";
    private TextInputEditText etGroupCode, etYourName, etYourEmail;
    private Button btnJoin;
    private ProgressBar progressBar;
    private ExpenseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);

        etGroupCode = findViewById(R.id.etGroupCode);
        etYourName = findViewById(R.id.etYourName);
        etYourEmail = findViewById(R.id.etYourEmail);
        btnJoin = findViewById(R.id.btnJoin);
        progressBar = findViewById(R.id.progressBar);

        repository = new ExpenseRepository(this);

        btnJoin.setOnClickListener(v -> joinGroup());
    }

    private void joinGroup() {
        String code = etGroupCode.getText().toString().trim();
        String yourName = etYourName.getText().toString().trim();
        String yourEmail = etYourEmail.getText().toString().trim();

        if (code.isEmpty()) {
            Toast.makeText(this, "Enter group code", Toast.LENGTH_SHORT).show();
            return;
        }

        if (yourName.isEmpty()) {
            Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (yourEmail.isEmpty()) {
            Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(yourEmail).matches()) {
            Toast.makeText(this, "Enter valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        repository.getGroupByCode(code, new ExpenseRepository.RepositoryCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (group != null) {
                    repository.getGroupMembers(group.getId(), new ExpenseRepository.RepositoryCallback<List<Member>>() {
                        @Override
                        public void onSuccess(List<Member> existingMembers) {
                            boolean memberExists = false;
                            Member firstMember = existingMembers.isEmpty() ? null : existingMembers.get(0);

                            for (Member member : existingMembers) {
                                if ((member.getEmail() != null && member.getEmail().equalsIgnoreCase(yourEmail)) ||
                                        member.getName().equalsIgnoreCase(yourName)) {
                                    memberExists = true;
                                    break;
                                }
                            }

                            if (memberExists) {
                                showLoading(false);
                                saveUserAndFinish(yourName, yourEmail, group.getName());
                            } else {
                                repository.addMemberWithEmail(group.getId(), yourName, yourEmail,
                                        new ExpenseRepository.RepositoryCallback<Member>() {
                                            @Override
                                            public void onSuccess(Member member) {
                                                // Send notification to creator
                                                if (firstMember != null && firstMember.getEmail() != null) {
                                                    sendJoinNotification(yourName, yourEmail,
                                                            group.getName(), firstMember.getName(),
                                                            firstMember.getEmail());
                                                }

                                                showLoading(false);
                                                saveUserAndFinish(yourName, yourEmail, group.getName());
                                            }

                                            @Override
                                            public void onError(String error) {
                                                showLoading(false);
                                                Toast.makeText(JoinGroupActivity.this,
                                                        "Error: " + error, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            showLoading(false);
                            Toast.makeText(JoinGroupActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    showLoading(false);
                    Toast.makeText(JoinGroupActivity.this, "Invalid group code!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(JoinGroupActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendJoinNotification(String newMemberName, String newMemberEmail,
                                      String groupName, String creatorName, String creatorEmail) {
        String subject = "New member joined " + groupName;
        String message = "Hi " + creatorName + ",\n\n" +
                "📢 Great news!\n\n" +
                newMemberName + " (" + newMemberEmail + ") has joined your group '" +
                groupName + "'.\n\n" +
                "You can now split expenses with them!\n\n" +
                "Happy tracking! 🎉";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("message/rfc822");
        shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{creatorEmail});
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        try {
            startActivity(Intent.createChooser(shareIntent, "Notify creator via..."));
        } catch (Exception e) {
            Log.e(TAG, "Cannot send notification: " + e.getMessage());
        }
    }

    private void saveUserAndFinish(String userName, String userEmail, String groupName) {
        SharedPreferences prefs = getSharedPreferences("ExpenseApp", MODE_PRIVATE);
        prefs.edit()
                .putString("current_user_name", userName)
                .putString("current_user_email", userEmail)
                .apply();

        Toast.makeText(this, "✅ Joined: " + groupName, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnJoin.setEnabled(!show);
    }
}