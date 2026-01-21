package com.example.expenseapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.adapters.MemberAdapter;
import com.example.expenseapp.database.ExpenseRepository;
import com.example.expenseapp.models.Expense;
import com.example.expenseapp.models.Member;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private TextInputEditText etDescription, etAmount, etDate;
    private Spinner spinnerPaidBy;
    private RecyclerView recyclerViewParticipants;
    private Button btnAddExpense, btnSelectAll;
    private TextView tvTitle;
    private ProgressBar progressBar;

    private ExpenseRepository repository;
    private String groupId;
    private String expenseId = null;
    private boolean isEditMode = false;
    private List<Member> members;
    private MemberAdapter adapter;
    private ArrayAdapter<String> spinnerAdapter;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        etDescription = findViewById(R.id.etDescription);
        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        spinnerPaidBy = findViewById(R.id.spinnerPaidBy);
        recyclerViewParticipants = findViewById(R.id.recyclerViewParticipants);
        btnAddExpense = findViewById(R.id.btnAddExpense);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        tvTitle = findViewById(R.id.tvTitle);
        progressBar = findViewById(R.id.progressBar);

        repository = new ExpenseRepository(this);
        groupId = getIntent().getStringExtra("GROUP_ID");
        expenseId = getIntent().getStringExtra("EXPENSE_ID");
        isEditMode = expenseId != null;

        selectedDate = Calendar.getInstance();

        if (isEditMode) {
            tvTitle.setText("Edit Expense");
            btnAddExpense.setText("Update Expense");
        }

        setupRecyclerView();
        loadMembers();
        setupDatePicker();

        if (isEditMode) {
            loadExpenseData();
        } else {
            updateDateDisplay();
        }

        btnSelectAll.setOnClickListener(v -> selectAllMembers());
        btnAddExpense.setOnClickListener(v -> saveExpense());
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay();
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        etDate.setText(sdf.format(selectedDate.getTime()));
    }

    private void setupRecyclerView() {
        recyclerViewParticipants.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadMembers() {
        showLoading(true);

        repository.getGroupMembers(groupId, new ExpenseRepository.RepositoryCallback<List<Member>>() {
            @Override
            public void onSuccess(List<Member> loadedMembers) {
                showLoading(false);

                if (loadedMembers.isEmpty()) {
                    Toast.makeText(AddExpenseActivity.this, "No members found", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                members = loadedMembers;

                List<String> memberNames = new ArrayList<>();
                for (Member member : members) {
                    memberNames.add(member.getName());
                }
                spinnerAdapter = new ArrayAdapter<>(AddExpenseActivity.this,
                        android.R.layout.simple_spinner_item, memberNames);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerPaidBy.setAdapter(spinnerAdapter);

                adapter = new MemberAdapter(members);
                recyclerViewParticipants.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(AddExpenseActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExpenseData() {
        showLoading(true);

        repository.getExpenseById(expenseId, new ExpenseRepository.RepositoryCallback<Expense>() {
            @Override
            public void onSuccess(Expense expense) {
                showLoading(false);

                if (expense != null) {
                    etDescription.setText(expense.getDescription());
                    etAmount.setText(String.valueOf(expense.getAmount()));
                    etDate.setText(formatDateForDisplay(expense.getDate()));

                    for (int i = 0; i < members.size(); i++) {
                        if (members.get(i).getId().equals(expense.getPaidByMemberId())) {
                            spinnerPaidBy.setSelection(i);
                            break;
                        }
                    }

                    List<String> participantIds = expense.getParticipants();
                    for (Member member : members) {
                        if (participantIds.contains(member.getId())) {
                            member.setSelected(true);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(AddExpenseActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDateForDisplay(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return outputFormat.format(inputFormat.parse(dateStr));
        } catch (Exception e) {
            return dateStr;
        }
    }

    private void selectAllMembers() {
        boolean allSelected = true;
        for (Member member : members) {
            if (!member.isSelected()) {
                allSelected = false;
                break;
            }
        }

        for (Member member : members) {
            member.setSelected(!allSelected);
        }
        adapter.notifyDataSetChanged();

        btnSelectAll.setText(allSelected ? "Select All" : "Deselect All");
    }

    private void saveExpense() {
        String description = etDescription.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        int paidByPosition = spinnerPaidBy.getSelectedItemPosition();
        String paidByMemberId = members.get(paidByPosition).getId();

        List<String> selectedParticipants = new ArrayList<>();
        for (Member member : members) {
            if (member.isSelected()) {
                selectedParticipants.add(member.getId());
            }
        }

        if (selectedParticipants.isEmpty()) {
            Toast.makeText(this, "Select at least one participant", Toast.LENGTH_SHORT).show();
            return;
        }

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String dateString = dbFormat.format(selectedDate.getTime());

        showLoading(true);

        if (isEditMode) {
            repository.updateExpense(expenseId, description, amount, paidByMemberId,
                    selectedParticipants, dateString, new ExpenseRepository.RepositoryCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean success) {
                            showLoading(false);

                            if (success) {
                                Toast.makeText(AddExpenseActivity.this, "Expense updated", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(AddExpenseActivity.this, "Error updating expense", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(String error) {
                            showLoading(false);
                            Toast.makeText(AddExpenseActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            repository.addExpense(groupId, description, amount, paidByMemberId,
                    selectedParticipants, dateString, new ExpenseRepository.RepositoryCallback<Expense>() {
                        @Override
                        public void onSuccess(Expense expense) {
                            showLoading(false);
                            Toast.makeText(AddExpenseActivity.this, "Expense added", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(String error) {
                            showLoading(false);
                            Toast.makeText(AddExpenseActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnAddExpense.setEnabled(!show);
    }
}