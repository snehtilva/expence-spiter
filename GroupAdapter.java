package com.example.expenseapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.R;
import com.example.expenseapp.models.Group;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    private Context context;
    private List<Group> groups;
    private OnGroupActionListener listener;

    public interface OnGroupActionListener {
        void onGroupClick(Group group);
        void onDeleteGroup(Group group, int position);
    }

    public GroupAdapter(Context context, List<Group> groups, OnGroupActionListener listener) {
        this.context = context;
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.groupName.setText(group.getName());
        holder.groupCode.setText("Code: " + group.getCode());
        holder.groupDate.setText("Created: " + formatDate(group.getCreatedDate()));

        holder.cardView.setOnClickListener(v -> listener.onGroupClick(group));
        holder.cardView.setOnLongClickListener(v -> {
            listener.onDeleteGroup(group, position);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void updateGroups(List<Group> newGroups) {
        this.groups = newGroups;
        notifyDataSetChanged();
    }

    public void removeGroup(int position) {
        groups.remove(position);
        notifyItemRemoved(position);
    }

    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView groupName, groupCode, groupDate;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            groupName = itemView.findViewById(R.id.groupName);
            groupCode = itemView.findViewById(R.id.groupCode);
            groupDate = itemView.findViewById(R.id.groupDate);
        }
    }
}