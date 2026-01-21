package com.example.expenseapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.adapters.ExpenseAdapter;
import com.example.expenseapp.adapters.MemberDisplayAdapter;
import com.example.expenseapp.database.ExpenseRepository;
import com.example.expenseapp.models.Expense;
import com.example.expenseapp.models.Group;
import com.example.expenseapp.models.Member;
import com.example.expenseapp.utils.PDFGenerator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class GroupDetailActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseClickListener {
    private static final String TAG = "GroupDetailActivity";

    private TextView tvGroupName, tvGroupCode, tvMembers, emptyView, tvPaidBadge;
    private RecyclerView recyclerView, recyclerViewMembers;
    private FloatingActionButton fabAddExpense;
    private Button btnAddMember, btnSettleUp, btnGeneratePDF, btnExpandMembers;
    private ProgressBar progressBar;

    private ExpenseRepository repository;
    private String groupId;
    private ExpenseAdapter adapter;
    private MemberDisplayAdapter memberAdapter;
    private String currentUserName;
    private boolean membersExpanded = false;
    private List<Member> allMembers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        tvGroupName = findViewById(R.id.tvGroupName);
        tvGroupCode = findViewById(R.id.tvGroupCode);
        tvMembers = findViewById(R.id.tvMembers);
        emptyView = findViewById(R.id.emptyView);
        tvPaidBadge = findViewById(R.id.tvPaidBadge);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerViewMembers = findViewById(R.id.recyclerViewMembers);
        fabAddExpense = findViewById(R.id.fabAddExpense);
        btnAddMember = findViewById(R.id.btnAddMember);
        btnSettleUp = findViewById(R.id.btnSettleUp);
        btnGeneratePDF = findViewById(R.id.btnGeneratePDF);
        btnExpandMembers = findViewById(R.id.btnExpandMembers);
        progressBar = findViewById(R.id.progressBar);

        repository = new ExpenseRepository(this);
        groupId = getIntent().getStringExtra("GROUP_ID");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMembers.setLayoutManager(new LinearLayoutManager(this));

        SharedPreferences prefs = getSharedPreferences("ExpenseApp", MODE_PRIVATE);
        currentUserName = prefs.getString("current_user_name", "");

        Log.d(TAG, "onCreate - Current User: [" + currentUserName + "]");

        fabAddExpense.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddExpenseActivity.class);
            intent.putExtra("GROUP_ID", groupId);
            startActivity(intent);
        });

        btnAddMember.setOnClickListener(v -> showAddMemberDialog());
        btnExpandMembers.setOnClickListener(v -> toggleMembersList());
        btnSettleUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettleUpActivity.class);
            intent.putExtra("GROUP_ID", groupId);
            startActivity(intent);
        });

        btnGeneratePDF.setOnClickListener(v -> generatePDFReport());

        loadGroupDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("ExpenseApp", MODE_PRIVATE);
        currentUserName = prefs.getString("current_user_name", "");
        Log.d(TAG, "onResume - Current User: [" + currentUserName + "]");

        loadGroupDetails();
    }

    private void loadGroupDetails() {
        showLoading(true);

        repository.getGroupById(groupId, new ExpenseRepository.RepositoryCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (group != null) {
                    tvGroupName.setText(group.getName());
                    tvGroupCode.setText("Code: " + group.getCode());

                    repository.getGroupMembers(groupId, new ExpenseRepository.RepositoryCallback<List<Member>>() {
                        @Override
                        public void onSuccess(List<Member> members) {
                            allMembers = members;
                            tvMembers.setText("Members (" + allMembers.size() + "):");

                            Log.d(TAG, "Loaded " + allMembers.size() + " members");

                            updateMembersList();

                            repository.getGroupExpenses(groupId, new ExpenseRepository.RepositoryCallback<List<Expense>>() {
                                @Override
                                public void onSuccess(List<Expense> expenses) {
                                    showLoading(false);

                                    if (expenses.isEmpty()) {
                                        emptyView.setVisibility(View.VISIBLE);
                                        recyclerView.setVisibility(View.GONE);
                                    } else {
                                        emptyView.setVisibility(View.GONE);
                                        recyclerView.setVisibility(View.VISIBLE);

                                        adapter = new ExpenseAdapter(GroupDetailActivity.this, expenses, allMembers, GroupDetailActivity.this);
                                        recyclerView.setAdapter(adapter);
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    showLoading(false);
                                    Toast.makeText(GroupDetailActivity.this, "Error loading expenses: " + error, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            showLoading(false);
                            Toast.makeText(GroupDetailActivity.this, "Error loading members: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    showLoading(false);
                    Toast.makeText(GroupDetailActivity.this, "Group not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(GroupDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleMembersList() {
        membersExpanded = !membersExpanded;
        updateMembersList();
    }

    private void updateMembersList() {
        SharedPreferences prefs = getSharedPreferences("ExpenseApp", MODE_PRIVATE);
        currentUserName = prefs.getString("current_user_name", "");

        Log.d(TAG, "=== UPDATE MEMBERS LIST ===");
        Log.d(TAG, "Current User: [" + currentUserName + "]");

        List<Member> membersToShow;

        if (membersExpanded) {
            membersToShow = allMembers;
            btnExpandMembers.setText("Show Less");
        } else {
            membersToShow = new ArrayList<>();
            for (Member member : allMembers) {
                if (member.getName().equalsIgnoreCase(currentUserName)) {
                    membersToShow.add(member);
                    break;
                }
            }
            btnExpandMembers.setText("Show All");
        }

        if (memberAdapter == null) {
            memberAdapter = new MemberDisplayAdapter(this, membersToShow);
            recyclerViewMembers.setAdapter(memberAdapter);
        } else {
            memberAdapter.updateMembers(membersToShow);
        }

        memberAdapter.updateCurrentUserName(currentUserName);
    }

    private void showAddMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_member_email, null);

        TextInputEditText etName = view.findViewById(R.id.etMemberName);
        TextInputEditText etEmail = view.findViewById(R.id.etMemberEmail);
        Button btnSendInvite = view.findViewById(R.id.btnSendInvite);
        TextView tvShareInfo = view.findViewById(R.id.tvShareInfo);

        AlertDialog dialog = builder.setView(view).create();

        btnSendInvite.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Enter member name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            showLoading(true);
            btnSendInvite.setEnabled(false);

            // UPDATED: Send join request instead of direct add
            repository.createJoinRequest(groupId, name, email,
                    new ExpenseRepository.RepositoryCallback<com.example.expenseapp.models.JoinRequest>() {
                        @Override
                        public void onSuccess(com.example.expenseapp.models.JoinRequest joinRequest) {
                            showLoading(false);
                            btnSendInvite.setEnabled(true);

                            tvShareInfo.setVisibility(View.VISIBLE);
                            tvShareInfo.setText("✅ Join request sent!\n\n" + name + " will receive notification.");

                            Toast.makeText(GroupDetailActivity.this,
                                    "Join request sent to " + name, Toast.LENGTH_LONG).show();

                            dialog.dismiss();
                        }

                        @Override
                        public void onError(String error) {
                            showLoading(false);
                            btnSendInvite.setEnabled(true);
                            Toast.makeText(GroupDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", null);
        dialog.show();
    }

    private void generatePDFReport() {
        String filePath = PDFGenerator.generatePDF(this, groupId);
        if (filePath != null) {
            Toast.makeText(this, "PDF generated successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onExpenseClick(Expense expense) {
        Intent intent = new Intent(this, AddExpenseActivity.class);
        intent.putExtra("GROUP_ID", groupId);
        intent.putExtra("EXPENSE_ID", expense.getId());
        startActivity(intent);
    }

    @Override
    public void onExpenseDelete(Expense expense, int position) {
        showLoading(true);

        repository.deleteExpense(expense.getId(), new ExpenseRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean deleted) {
                showLoading(false);

                if (deleted) {
                    adapter.removeExpense(position);
                    Toast.makeText(GroupDetailActivity.this, "Expense deleted", Toast.LENGTH_SHORT).show();
                    loadGroupDetails();
                } else {
                    Toast.makeText(GroupDetailActivity.this, "Error deleting expense", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(GroupDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}