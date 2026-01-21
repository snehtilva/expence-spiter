package com.example.expenseapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.adapters.GroupAdapter;
import com.example.expenseapp.database.ExpenseRepository;
import com.example.expenseapp.models.Group;
import com.example.expenseapp.models.JoinRequest;
import com.example.expenseapp.models.Member;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GroupAdapter.OnGroupActionListener {
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private ExpenseRepository repository;
    private View emptyView;
    private TextView tvWelcome, tvBadge;
    private Button btnCreateGroup, btnJoinGroup, btnChangeUser;
    private FloatingActionButton fabRequests;
    private ProgressBar progressBar;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.emptyView);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvBadge = findViewById(R.id.tvBadge);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
        btnJoinGroup = findViewById(R.id.btnJoinGroup);
        btnChangeUser = findViewById(R.id.btnChangeUser);
        fabRequests = findViewById(R.id.fabRequests);
        progressBar = findViewById(R.id.progressBar);

        repository = new ExpenseRepository(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        updateUI();

        btnCreateGroup.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateGroupActivity.class);
            startActivity(intent);
        });

        btnJoinGroup.setOnClickListener(v -> {
            Intent intent = new Intent(this, JoinGroupActivity.class);
            startActivity(intent);
        });

        btnChangeUser.setOnClickListener(v -> changeUser());

        fabRequests.setOnClickListener(v -> {
            Intent intent = new Intent(this, JoinRequestsActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        loadPendingRequestsCount();
    }

    private void updateUI() {
        SharedPreferences prefs = getSharedPreferences("ExpenseApp", MODE_PRIVATE);
        currentUserName = prefs.getString("current_user_name", "");

        if (!currentUserName.isEmpty()) {
            tvWelcome.setText("Welcome, " + currentUserName + "!");
            btnChangeUser.setVisibility(View.VISIBLE);
            loadUserGroups();
        } else {
            tvWelcome.setText("Expense Splitter");
            btnChangeUser.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    private void loadPendingRequestsCount() {
        repository.getMyJoinRequests(new ExpenseRepository.RepositoryCallback<List<JoinRequest>>() {
            @Override
            public void onSuccess(List<JoinRequest> requests) {
                int pendingCount = 0;
                for (JoinRequest req : requests) {
                    if (req.getStatus().equals("pending")) {
                        pendingCount++;
                    }
                }

                if (pendingCount > 0) {
                    tvBadge.setVisibility(View.VISIBLE);
                    tvBadge.setText(String.valueOf(pendingCount));
                } else {
                    tvBadge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String error) {
                tvBadge.setVisibility(View.GONE);
            }
        });
    }

    private void loadUserGroups() {
        if (currentUserName == null || currentUserName.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        showLoading(true);

        repository.getAllGroups(new ExpenseRepository.RepositoryCallback<List<Group>>() {
            @Override
            public void onSuccess(List<Group> allGroups) {
                if (allGroups.isEmpty()) {
                    showLoading(false);
                    emptyView.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    return;
                }

                filterUserGroups(allGroups);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterUserGroups(List<Group> allGroups) {
        List<Group> userGroups = new ArrayList<>();
        final int[] checkedCount = {0};

        for (Group group : allGroups) {
            repository.getGroupMembers(group.getId(), new ExpenseRepository.RepositoryCallback<List<Member>>() {
                @Override
                public void onSuccess(List<Member> members) {
                    boolean isMember = false;
                    for (Member member : members) {
                        if (member.getName().equalsIgnoreCase(currentUserName)) {
                            isMember = true;
                            break;
                        }
                    }

                    if (isMember) {
                        userGroups.add(group);
                    }

                    checkedCount[0]++;

                    if (checkedCount[0] == allGroups.size()) {
                        displayGroups(userGroups);
                    }
                }

                @Override
                public void onError(String error) {
                    checkedCount[0]++;
                    if (checkedCount[0] == allGroups.size()) {
                        displayGroups(userGroups);
                    }
                }
            });
        }
    }

    private void displayGroups(List<Group> groups) {
        showLoading(false);

        if (groups.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            if (adapter == null) {
                adapter = new GroupAdapter(MainActivity.this, groups, MainActivity.this);
                recyclerView.setAdapter(adapter);
            } else {
                adapter.updateGroups(groups);
            }
        }
    }

    private void changeUser() {
        new AlertDialog.Builder(this)
                .setTitle("Switch User")
                .setMessage("Current user: " + currentUserName + "\n\nReset and use different account?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("ExpenseApp", MODE_PRIVATE);
                    prefs.edit()
                            .remove("current_user_name")
                            .remove("current_user_email")
                            .putBoolean("user_setup_complete", false)
                            .apply();

                    // Restart app
                    Intent intent = new Intent(this, WelcomeSetupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onGroupClick(Group group) {
        Intent intent = new Intent(this, GroupDetailActivity.class);
        intent.putExtra("GROUP_ID", group.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteGroup(Group group, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage("Delete '" + group.getName() + "'?\n\nAll data will be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    showLoading(true);

                    repository.deleteGroup(group.getId(), new ExpenseRepository.RepositoryCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean deleted) {
                            showLoading(false);
                            if (deleted) {
                                Toast.makeText(MainActivity.this, "Group deleted", Toast.LENGTH_SHORT).show();
                                loadUserGroups();
                            } else {
                                Toast.makeText(MainActivity.this, "Error deleting group", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            showLoading(false);
                            Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}

// Location: app/src/main/java/com/example/expenseapp/MainActivity.java