package com.example.expenseapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.adapters.SettlementAdapter;
import com.example.expenseapp.database.ExpenseRepository;
import com.example.expenseapp.models.Expense;
import com.example.expenseapp.models.Member;
import com.example.expenseapp.models.Settlement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SettleUpActivity extends AppCompatActivity implements SettlementAdapter.OnSettlementActionListener {
    private static final String TAG = "SettleUpActivity";

    private TextView tvTotalExpense, tvPaymentStats, tvSettlementHeader;
    private RecyclerView recyclerViewSettlements;
    private ProgressBar progressBar;

    private ExpenseRepository repository;
    private String groupId;
    private SettlementAdapter adapter;
    private List<Member> members;
    private List<Expense> expenses;
    private boolean isCreatingSettlements = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settle_up);

        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvPaymentStats = findViewById(R.id.tvPaymentStats);
        tvSettlementHeader = findViewById(R.id.tvSettlementHeader);
        recyclerViewSettlements = findViewById(R.id.recyclerViewSettlements);
        progressBar = findViewById(R.id.progressBar);

        repository = new ExpenseRepository(this);
        groupId = getIntent().getStringExtra("GROUP_ID");

        recyclerViewSettlements.setLayoutManager(new LinearLayoutManager(this));

        calculateAndDisplaySettlements();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isCreatingSettlements) {
            calculateAndDisplaySettlements();
        }
    }

    private void calculateAndDisplaySettlements() {
        showLoading(true);

        // Load expenses
        repository.getGroupExpenses(groupId, new ExpenseRepository.RepositoryCallback<List<Expense>>() {
            @Override
            public void onSuccess(List<Expense> loadedExpenses) {
                expenses = loadedExpenses;

                // Load members
                repository.getGroupMembers(groupId, new ExpenseRepository.RepositoryCallback<List<Member>>() {
                    @Override
                    public void onSuccess(List<Member> loadedMembers) {
                        members = loadedMembers;

                        // Load existing settlements
                        repository.getGroupSettlements(groupId, new ExpenseRepository.RepositoryCallback<List<Settlement>>() {
                            @Override
                            public void onSuccess(List<Settlement> existingSettlements) {
                                // Delete all pending settlements to recalculate
                                deletePendingSettlements(existingSettlements);
                            }

                            @Override
                            public void onError(String error) {
                                showLoading(false);
                                Toast.makeText(SettleUpActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        Toast.makeText(SettleUpActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(SettleUpActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deletePendingSettlements(List<Settlement> settlements) {
        List<Settlement> pendingSettlements = new ArrayList<>();
        List<Settlement> paidSettlements = new ArrayList<>();

        for (Settlement settlement : settlements) {
            if (settlement.getStatus().equals("pending")) {
                pendingSettlements.add(settlement);
            } else {
                paidSettlements.add(settlement);
            }
        }

        if (pendingSettlements.isEmpty()) {
            processSettlements(paidSettlements);
            return;
        }

        final int[] deletedCount = {0};
        final int totalPending = pendingSettlements.size();

        for (Settlement settlement : pendingSettlements) {
            repository.deleteSettlement(settlement.getId(), new ExpenseRepository.RepositoryCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean deleted) {
                    deletedCount[0]++;
                    if (deletedCount[0] == totalPending) {
                        processSettlements(paidSettlements);
                    }
                }

                @Override
                public void onError(String error) {
                    deletedCount[0]++;
                    if (deletedCount[0] == totalPending) {
                        processSettlements(paidSettlements);
                    }
                }
            });
        }
    }

    private void processSettlements(List<Settlement> paidSettlements) {
        if (members.isEmpty()) {
            showLoading(false);
            return;
        }

        Log.d(TAG, "=== SETTLEMENT CALCULATION ===");

        // Calculate balances from expenses
        Map<String, Double> balances = new HashMap<>();
        double totalExpense = 0;

        for (Expense expense : expenses) {
            totalExpense += expense.getAmount();

            String paidBy = expense.getPaidByMemberId();
            balances.put(paidBy, balances.getOrDefault(paidBy, 0.0) + expense.getAmount());

            int participantCount = expense.getParticipants().size();
            if (participantCount > 0) {
                double sharePerPerson = expense.getAmount() / participantCount;
                for (String participantId : expense.getParticipants()) {
                    balances.put(participantId, balances.getOrDefault(participantId, 0.0) - sharePerPerson);
                }
            }
        }

        Log.d(TAG, "Total Expense: " + totalExpense);

        // Adjust balances based on paid settlements
        double totalPaidAmount = 0;
        for (Settlement paidSettlement : paidSettlements) {
            double amount = paidSettlement.getAmount();
            totalPaidAmount += amount;

            String fromMember = paidSettlement.getFromMemberId();
            balances.put(fromMember, balances.getOrDefault(fromMember, 0.0) + amount);

            String toMember = paidSettlement.getToMemberId();
            balances.put(toMember, balances.getOrDefault(toMember, 0.0) - amount);
        }

        // Display total expense and remaining to settle
        double remainingToSettle = 0;
        for (double balance : balances.values()) {
            if (balance < 0) {
                remainingToSettle += Math.abs(balance);
            }
        }

        tvTotalExpense.setText(String.format(Locale.getDefault(),
                "Total: ₹%.2f | Paid: ₹%.2f | Remaining: ₹%.2f",
                totalExpense, totalPaidAmount, remainingToSettle));

        // Calculate payment stats
        StringBuilder stats = new StringBuilder();
        for (Member member : members) {
            double balance = balances.getOrDefault(member.getId(), 0.0);
            if (Math.abs(balance) > 0.01) {
                if (balance > 0) {
                    stats.append(String.format(Locale.getDefault(), "%s will receive ₹%.2f\n",
                            member.getName(), balance));
                } else {
                    stats.append(String.format(Locale.getDefault(), "%s owes ₹%.2f\n",
                            member.getName(), -balance));
                }
            }
        }
        tvPaymentStats.setText(stats.toString().trim());

        tvSettlementHeader.setText("Settlements:");

        // Generate new pending settlements
        generateOptimalSettlements(balances, paidSettlements);
    }

    private void generateOptimalSettlements(Map<String, Double> balances, List<Settlement> paidSettlements) {
        // CRITICAL: Prevent duplicate execution
        if (isCreatingSettlements) {
            Log.w(TAG, "Already creating settlements, skipping...");
            return;
        }

        isCreatingSettlements = true;

        List<DebtPair> debtors = new ArrayList<>();
        List<DebtPair> creditors = new ArrayList<>();

        for (Member member : members) {
            double balance = balances.getOrDefault(member.getId(), 0.0);
            if (balance < -0.01) {
                debtors.add(new DebtPair(member.getId(), -balance));
            } else if (balance > 0.01) {
                creditors.add(new DebtPair(member.getId(), balance));
            }
        }

        Log.d(TAG, "Debtors: " + debtors.size() + ", Creditors: " + creditors.size());

        if (debtors.isEmpty() || creditors.isEmpty()) {
            Log.d(TAG, "No settlements needed");
            isCreatingSettlements = false;
            displayAllSettlements(paidSettlements, new ArrayList<>());
            return;
        }

        // Calculate all settlements FIRST
        List<SettlementData> settlementsToCreate = new ArrayList<>();

        int i = 0, j = 0;
        while (i < debtors.size() && j < creditors.size()) {
            DebtPair debtor = debtors.get(i);
            DebtPair creditor = creditors.get(j);

            double amount = Math.min(debtor.amount, creditor.amount);

            Log.d(TAG, "Will create: " + getMemberName(debtor.memberId) +
                    " → " + getMemberName(creditor.memberId) + ": ₹" + amount);

            settlementsToCreate.add(new SettlementData(debtor.memberId, creditor.memberId, amount));

            debtor.amount -= amount;
            creditor.amount -= amount;

            if (debtor.amount < 0.01) i++;
            if (creditor.amount < 0.01) j++;
        }

        Log.d(TAG, "Total settlements to create: " + settlementsToCreate.size());

        if (settlementsToCreate.isEmpty()) {
            isCreatingSettlements = false;
            displayAllSettlements(paidSettlements, new ArrayList<>());
            return;
        }

        // Create settlements ONE BY ONE sequentially
        createSettlementsSequentially(settlementsToCreate, 0, new ArrayList<>(), paidSettlements);
    }

    private void createSettlementsSequentially(List<SettlementData> settlementsToCreate,
                                               int index,
                                               List<Settlement> createdSettlements,
                                               List<Settlement> paidSettlements) {
        if (index >= settlementsToCreate.size()) {
            // All done!
            Log.d(TAG, "All " + createdSettlements.size() + " settlements created successfully");
            isCreatingSettlements = false;
            displayAllSettlements(paidSettlements, createdSettlements);
            return;
        }

        SettlementData data = settlementsToCreate.get(index);
        Log.d(TAG, "Creating settlement " + (index + 1) + "/" + settlementsToCreate.size());

        repository.addSettlement(groupId, data.fromMemberId, data.toMemberId, data.amount,
                new ExpenseRepository.RepositoryCallback<Settlement>() {
                    @Override
                    public void onSuccess(Settlement settlement) {
                        createdSettlements.add(settlement);
                        Log.d(TAG, "✓ Created settlement " + (index + 1));

                        // Create next one
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            createSettlementsSequentially(settlementsToCreate, index + 1,
                                    createdSettlements, paidSettlements);
                        }, 100); // Small delay to prevent race conditions
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "✗ Error creating settlement " + (index + 1) + ": " + error);

                        // Continue with next one even on error
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            createSettlementsSequentially(settlementsToCreate, index + 1,
                                    createdSettlements, paidSettlements);
                        }, 100);
                    }
                });
    }

    private void displayAllSettlements(List<Settlement> paidSettlements, List<Settlement> newSettlements) {
        showLoading(false);

        List<Settlement> allSettlements = new ArrayList<>();
        allSettlements.addAll(paidSettlements);
        allSettlements.addAll(newSettlements);

        Log.d(TAG, "Displaying total settlements: " + allSettlements.size());

        adapter = new SettlementAdapter(allSettlements, members, this, false);
        recyclerViewSettlements.setAdapter(adapter);
    }

    @Override
    public void onPaymentToggle(Settlement settlement, int position) {
        String currentStatus = settlement.getStatus();
        String newStatus;

        if (currentStatus.equals("pending")) {
            newStatus = "paid";
        } else if (currentStatus.equals("paid")) {
            newStatus = "pending";
        } else {
            Toast.makeText(this, "This payment is cancelled due to deleted expense",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        repository.updateSettlementStatus(settlement.getId(), newStatus, new ExpenseRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean updated) {
                if (updated) {
                    settlement.setStatus(newStatus);
                    adapter.notifyItemChanged(position);
                    calculateAndDisplaySettlements();
                } else {
                    showLoading(false);
                    Toast.makeText(SettleUpActivity.this, "Error updating settlement", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(SettleUpActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getMemberName(String memberId) {
        for (Member member : members) {
            if (member.getId().equals(memberId)) {
                return member.getName();
            }
        }
        return "Unknown";
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private static class DebtPair {
        String memberId;
        double amount;

        DebtPair(String memberId, double amount) {
            this.memberId = memberId;
            this.amount = amount;
        }
    }

    private static class SettlementData {
        String fromMemberId;
        String toMemberId;
        double amount;

        SettlementData(String fromMemberId, String toMemberId, double amount) {
            this.fromMemberId = fromMemberId;
            this.toMemberId = toMemberId;
            this.amount = amount;
        }
    }
}