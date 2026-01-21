package com.example.expenseapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.R;
import com.example.expenseapp.models.Member;

import java.util.List;

public class MemberDisplayAdapter extends RecyclerView.Adapter<MemberDisplayAdapter.ViewHolder> {
    private Context context;
    private List<Member> members;
    private String currentUserName;

    public MemberDisplayAdapter(Context context, List<Member> members) {
        this.context = context;
        this.members = members;
        loadCurrentUserName();
    }

    private void loadCurrentUserName() {
        SharedPreferences prefs = context.getSharedPreferences("ExpenseApp", Context.MODE_PRIVATE);
        currentUserName = prefs.getString("current_user_name", "");
    }

    public void updateCurrentUserName(String userName) {
        this.currentUserName = userName;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member_display, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member member = members.get(position);
        holder.tvMemberName.setText(member.getName());

        // Show "(You)" label ONLY for current user
        if (!currentUserName.isEmpty() && member.getName().equalsIgnoreCase(currentUserName)) {
            holder.tvYouLabel.setVisibility(View.VISIBLE);
        } else {
            holder.tvYouLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void updateMembers(List<Member> newMembers) {
        this.members = newMembers;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvYouLabel;

        ViewHolder(View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvYouLabel = itemView.findViewById(R.id.tvYouLabel);
        }
    }
}