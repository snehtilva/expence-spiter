package com.example.expenseapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.R;
import com.example.expenseapp.models.Member;
import com.example.expenseapp.models.Settlement;

import java.util.List;
import java.util.Locale;

public class SettlementAdapter extends RecyclerView.Adapter<SettlementAdapter.ViewHolder> {
    private List<Settlement> settlements;
    private List<Member> members;
    private OnSettlementActionListener listener;
    private boolean isHistoryMode;

    public interface OnSettlementActionListener {
        void onPaymentToggle(Settlement settlement, int position);
    }

    public SettlementAdapter(List<Settlement> settlements, List<Member> members,
                             OnSettlementActionListener listener, boolean isHistoryMode) {
        this.settlements = settlements;
        this.members = members;
        this.listener = listener;
        this.isHistoryMode = isHistoryMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_settlement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Settlement settlement = settlements.get(position);

        String fromName = getMemberName(settlement.getFromMemberId());
        String toName = getMemberName(settlement.getToMemberId());

        holder.tvFrom.setText("From: " + fromName);
        holder.tvTo.setText("To: " + toName);
        holder.tvAmount.setText(String.format(Locale.getDefault(), "Amount: ₹%.2f", settlement.getAmount()));

        String status = settlement.getStatus();

        // Apply styling based on status
        if (status.equals("paid")) {
            holder.itemView.setBackgroundColor(Color.parseColor("#C8E6C9"));
            holder.tvFrom.setTextColor(Color.parseColor("#1B5E20"));
            holder.tvTo.setTextColor(Color.parseColor("#1B5E20"));
            holder.tvAmount.setTextColor(Color.parseColor("#2E7D32"));
            holder.tvStatus.setTextColor(Color.parseColor("#1B5E20"));
            holder.tvStatus.setText("✓ PAID (tap to undo)");
            holder.tvStatus.setTextSize(14);
            holder.tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);

        } else if (status.equals("cancelled")) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFCDD2"));
            holder.tvFrom.setTextColor(Color.parseColor("#B71C1C"));
            holder.tvTo.setTextColor(Color.parseColor("#B71C1C"));
            holder.tvAmount.setTextColor(Color.parseColor("#C62828"));
            holder.tvStatus.setTextColor(Color.parseColor("#B71C1C"));
            holder.tvStatus.setText("✗ CANCELLED");
            holder.tvStatus.setTextSize(14);
            holder.tvStatus.setTypeface(null, android.graphics.Typeface.BOLD);

        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_settlement_pending);
            holder.tvFrom.setTextColor(Color.BLACK);
            holder.tvTo.setTextColor(Color.BLACK);
            holder.tvAmount.setTextColor(Color.BLACK);
            holder.tvStatus.setTextColor(Color.parseColor("#F57C00"));
            holder.tvStatus.setText("PENDING (tap to pay)");
            holder.tvStatus.setTextSize(14);
            holder.tvStatus.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        // Click handler
        holder.itemView.setOnClickListener(v -> {
            if (!status.equals("cancelled")) {
                listener.onPaymentToggle(settlement, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return settlements.size();
    }

    private String getMemberName(String memberId) {
        for (Member member : members) {
            if (member.getId().equals(memberId)) {
                return member.getName();
            }
        }
        return "Unknown";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFrom, tvTo, tvAmount, tvDate, tvStatus;

        ViewHolder(View itemView) {
            super(itemView);
            tvFrom = itemView.findViewById(R.id.tvFrom);
            tvTo = itemView.findViewById(R.id.tvTo);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}