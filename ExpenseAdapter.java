package com.example.expenseapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.R;
import com.example.expenseapp.models.Expense;
import com.example.expenseapp.models.Member;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {
    private Context context;
    private List<Expense> expenses;
    private List<Member> members;
    private OnExpenseClickListener listener;

    public interface OnExpenseClickListener {
        void onExpenseClick(Expense expense);
        void onExpenseDelete(Expense expense, int position);
    }

    public ExpenseAdapter(Context context, List<Expense> expenses, List<Member> members, OnExpenseClickListener listener) {
        this.context = context;
        this.expenses = expenses;
        this.members = members;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.expenseDescription.setText(expense.getDescription());
        holder.expenseAmount.setText(String.format(Locale.getDefault(), "₹%.2f", expense.getAmount()));

        String paidByName = getMemberName(expense.getPaidByMemberId());
        holder.expensePaidBy.setText("Paid by: " + paidByName);
        holder.expenseDate.setText(formatDate(expense.getDate()));

        // Click to edit
        holder.itemView.setOnClickListener(v -> listener.onExpenseClick(expense));

        // Long press for options
        holder.itemView.setOnLongClickListener(v -> {
            showOptionsDialog(expense, position);
            return true;
        });
    }

    private void showOptionsDialog(Expense expense, int position) {
        String[] options = {"Edit", "Delete"};

        new AlertDialog.Builder(context)
                .setTitle("Expense Options")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        listener.onExpenseClick(expense);
                    } else if (which == 1) {
                        confirmDelete(expense, position);
                    }
                })
                .show();
    }

    private void confirmDelete(Expense expense, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    listener.onExpenseDelete(expense, position);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    private String getMemberName(String memberId) {
        for (Member member : members) {
            if (member.getId().equals(memberId)) {
                return member.getName();
            }
        }
        return "Unknown";
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    public void removeExpense(int position) {
        expenses.remove(position);
        notifyItemRemoved(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView expenseDescription, expenseAmount, expensePaidBy, expenseDate;

        ViewHolder(View itemView) {
            super(itemView);
            expenseDescription = itemView.findViewById(R.id.expenseDescription);
            expenseAmount = itemView.findViewById(R.id.expenseAmount);
            expensePaidBy = itemView.findViewById(R.id.expensePaidBy);
            expenseDate = itemView.findViewById(R.id.expenseDate);
        }
    }
}