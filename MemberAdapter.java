package com.example.expenseapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.R;
import com.example.expenseapp.models.Member;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    private List<Member> members;

    public MemberAdapter(List<Member> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Member member = members.get(position);
        holder.memberName.setText(member.getName());
        holder.memberCheckbox.setVisibility(View.VISIBLE);
        holder.memberCheckbox.setChecked(member.isSelected());

        holder.memberCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            member.setSelected(isChecked);
        });

        holder.itemView.setOnClickListener(v -> {
            holder.memberCheckbox.setChecked(!holder.memberCheckbox.isChecked());
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView memberName;
        CheckBox memberCheckbox;

        ViewHolder(View itemView) {
            super(itemView);
            memberName = itemView.findViewById(R.id.memberName);
            memberCheckbox = itemView.findViewById(R.id.memberCheckbox);
        }
    }
}