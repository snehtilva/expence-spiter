package com.example.expenseapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expenseapp.R;
import com.example.expenseapp.database.ExpenseRepository;
import com.example.expenseapp.models.Group;
import com.example.expenseapp.models.JoinRequest;

import java.util.List;

public class JoinRequestAdapter extends RecyclerView.Adapter<JoinRequestAdapter.ViewHolder> {
    private Context context;
    private List<JoinRequest> requests;
    private OnRequestActionListener listener;
    private ExpenseRepository repository;

    public interface OnRequestActionListener {
        void onApprove(JoinRequest request, int position);
        void onReject(JoinRequest request, int position);
    }

    public JoinRequestAdapter(Context context, List<JoinRequest> requests, OnRequestActionListener listener) {
        this.context = context;
        this.requests = requests;
        this.listener = listener;
        this.repository = new ExpenseRepository(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_join_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        JoinRequest request = requests.get(position);

        holder.tvName.setText(request.getRequesterName());
        holder.tvEmail.setText(request.getRequesterEmail());
        holder.tvStatus.setText("Status: " + request.getStatus());

        // Load group name
        loadGroupName(request.getGroupId(), holder.tvGroupName);

        if (request.getStatus().equals("pending")) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.tvStatus.setVisibility(View.GONE);

            holder.btnApprove.setOnClickListener(v -> listener.onApprove(request, position));
            holder.btnReject.setOnClickListener(v -> listener.onReject(request, position));
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.VISIBLE);
        }
    }

    private void loadGroupName(String groupId, TextView tvGroupName) {
        repository.getGroupById(groupId, new ExpenseRepository.RepositoryCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (group != null) {
                    tvGroupName.setText(group.getName());
                } else {
                    tvGroupName.setText("Unknown Group");
                }
            }

            @Override
            public void onError(String error) {
                tvGroupName.setText("Unknown Group");
            }
        });
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvGroupName, tvStatus;
        Button btnApprove, btnReject;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}

