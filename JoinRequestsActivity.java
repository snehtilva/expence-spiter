package com.example.expenseapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.adapters.JoinRequestAdapter;
import com.example.expenseapp.database.ExpenseRepository;
import com.example.expenseapp.models.Group;
import com.example.expenseapp.models.JoinRequest;

import java.util.ArrayList;
import java.util.List;

public class JoinRequestsActivity extends AppCompatActivity implements JoinRequestAdapter.OnRequestActionListener {
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private JoinRequestAdapter adapter;
    private ExpenseRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_requests);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressBar = findViewById(R.id.progressBar);

        repository = new ExpenseRepository(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadJoinRequests();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJoinRequests();
    }

    private void loadJoinRequests() {
        showLoading(true);

        repository.getMyJoinRequests(new ExpenseRepository.RepositoryCallback<List<JoinRequest>>() {
            @Override
            public void onSuccess(List<JoinRequest> requests) {
                showLoading(false);

                if (requests.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    adapter = new JoinRequestAdapter(JoinRequestsActivity.this, requests, JoinRequestsActivity.this);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(JoinRequestsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApprove(JoinRequest request, int position) {
        showLoading(true);

        // Check if member already exists
        repository.getGroupMembers(request.getGroupId(), new ExpenseRepository.RepositoryCallback<List<com.example.expenseapp.models.Member>>() {
            @Override
            public void onSuccess(List<com.example.expenseapp.models.Member> existingMembers) {
                boolean memberExists = false;

                for (com.example.expenseapp.models.Member member : existingMembers) {
                    if (member.getEmail() != null &&
                            member.getEmail().equalsIgnoreCase(request.getRequesterEmail())) {
                        memberExists = true;
                        break;
                    }
                }

                if (memberExists) {
                    // Member already exists, just update request status
                    updateRequestToApproved(request);
                } else {
                    // Add new member
                    addMemberAndUpdateRequest(request);
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(JoinRequestsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMemberAndUpdateRequest(JoinRequest request) {
        repository.addMemberWithEmail(request.getGroupId(), request.getRequesterName(),
                request.getRequesterEmail(), new ExpenseRepository.RepositoryCallback<com.example.expenseapp.models.Member>() {
                    @Override
                    public void onSuccess(com.example.expenseapp.models.Member member) {
                        updateRequestToApproved(request);
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        Toast.makeText(JoinRequestsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateRequestToApproved(JoinRequest request) {
        repository.updateJoinRequestStatus(request.getId(), "approved",
                new ExpenseRepository.RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        showLoading(false);
                        Toast.makeText(JoinRequestsActivity.this,
                                "✅ " + request.getRequesterName() + " approved!",
                                Toast.LENGTH_SHORT).show();
                        loadJoinRequests();

                        // Send notification to requester
                        sendApprovalNotification(request);
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        Toast.makeText(JoinRequestsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onReject(JoinRequest request, int position) {
        showLoading(true);

        repository.updateJoinRequestStatus(request.getId(), "rejected",
                new ExpenseRepository.RepositoryCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean success) {
                        showLoading(false);
                        Toast.makeText(JoinRequestsActivity.this,
                                "❌ Request rejected", Toast.LENGTH_SHORT).show();
                        loadJoinRequests();
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        Toast.makeText(JoinRequestsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendApprovalNotification(JoinRequest request) {
        // Get group name
        repository.getGroupById(request.getGroupId(), new ExpenseRepository.RepositoryCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (group != null) {
                    // Optional: Send email/notification to requester
                    // For now, they'll see it when they open the app
                }
            }

            @Override
            public void onError(String error) {
                // Silently fail
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}

// Location: app/src/main/java/com/example/expenseapp/JoinRequestsActivity.java