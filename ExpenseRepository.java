package com.example.expenseapp.database;

import android.content.Context;
import android.util.Log;

import com.example.expenseapp.api.SupabaseClient;
import com.example.expenseapp.api.SupabaseService;
import com.example.expenseapp.models.Expense;
import com.example.expenseapp.models.ExpenseParticipant;
import com.example.expenseapp.models.Group;
import com.example.expenseapp.models.Member;
import com.example.expenseapp.models.Settlement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpenseRepository {
    private static final String TAG = "ExpenseRepository";
    private SupabaseService service;
    private Context context;

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public ExpenseRepository(Context context) {
        this.context = context;
        this.service = SupabaseClient.getService();
    }

    // ==================== GROUP OPERATIONS ====================

    public void createGroup(String name, String code, String creatorName, RepositoryCallback<Group> callback) {
        Group group = new Group(name, code);
        service.createGroup(group).enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Group createdGroup = response.body().get(0);
                    // Add creator as first member
                    addMember(createdGroup.getId(), creatorName, new RepositoryCallback<Member>() {
                        @Override
                        public void onSuccess(Member result) {
                            callback.onSuccess(createdGroup);
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError(error);
                        }
                    });
                } else {
                    callback.onError("Failed to create group");
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createGroupWithEmail(String name, String code, String creatorName, String creatorEmail,
                                     RepositoryCallback<Group> callback) {
        Group group = new Group(name, code);
        service.createGroup(group).enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Group createdGroup = response.body().get(0);
                    // Add creator as first member with email
                    addMemberWithEmail(createdGroup.getId(), creatorName, creatorEmail,
                            new RepositoryCallback<Member>() {
                                @Override
                                public void onSuccess(Member result) {
                                    callback.onSuccess(createdGroup);
                                }

                                @Override
                                public void onError(String error) {
                                    callback.onError(error);
                                }
                            });
                } else {
                    callback.onError("Failed to create group");
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getGroupByCode(String code, RepositoryCallback<Group> callback) {
        service.getGroupByCode("eq." + code, "*").enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getGroupById(String id, RepositoryCallback<Group> callback) {
        service.getGroupById("eq." + id, "*").enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getAllGroups(RepositoryCallback<List<Group>> callback) {
        service.getGroups("created_date.desc").enqueue(new Callback<List<Group>>() {
            @Override
            public void onResponse(Call<List<Group>> call, Response<List<Group>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Group>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteGroup(String groupId, RepositoryCallback<Boolean> callback) {
        service.deleteGroup("eq." + groupId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // ==================== MEMBER OPERATIONS ====================

    public void addMember(String groupId, String name, RepositoryCallback<Member> callback) {
        Member member = new Member(groupId, name);
        Log.d(TAG, "Adding member - Name: " + name);

        service.createMember(member).enqueue(new Callback<List<Member>>() {
            @Override
            public void onResponse(Call<List<Member>> call, Response<List<Member>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    String errorMsg = "Failed to add member. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ", Error: " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += ", Exception: " + e.getMessage();
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Member>> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public void addMemberWithEmail(String groupId, String name, String email,
                                   RepositoryCallback<Member> callback) {
        Member member = new Member(groupId, name, email);
        Log.d(TAG, "Adding member - Name: " + name + ", Email: " + email);

        service.createMember(member).enqueue(new Callback<List<Member>>() {
            @Override
            public void onResponse(Call<List<Member>> call, Response<List<Member>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    String errorMsg = "Failed to add member. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ", Error: " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += ", Exception: " + e.getMessage();
                    }
                    Log.e(TAG, errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Member>> call, Throwable t) {
                Log.e(TAG, "Network failure: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public void getGroupMembers(String groupId, RepositoryCallback<List<Member>> callback) {
        service.getMembers("eq." + groupId, "*", "id.asc").enqueue(new Callback<List<Member>>() {
            @Override
            public void onResponse(Call<List<Member>> call, Response<List<Member>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Member>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getMemberById(String id, RepositoryCallback<Member> callback) {
        service.getMemberById("eq." + id, "*").enqueue(new Callback<List<Member>>() {
            @Override
            public void onResponse(Call<List<Member>> call, Response<List<Member>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Call<List<Member>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // ==================== EXPENSE OPERATIONS ====================

    public void addExpense(String groupId, String description, double amount, String paidByMemberId,
                           List<String> participantIds, String date, RepositoryCallback<Expense> callback) {
        Log.d(TAG, "Adding expense - Amount: " + amount);

        Expense expense = new Expense(groupId, description, amount, paidByMemberId, date);

        service.createExpense(expense).enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Expense createdExpense = response.body().get(0);

                    // Add participants
                    List<ExpenseParticipant> participants = new ArrayList<>();
                    for (String participantId : participantIds) {
                        participants.add(new ExpenseParticipant(createdExpense.getId(), participantId));
                    }

                    service.addParticipants(participants).enqueue(new Callback<List<ExpenseParticipant>>() {
                        @Override
                        public void onResponse(Call<List<ExpenseParticipant>> call, Response<List<ExpenseParticipant>> response) {
                            if (response.isSuccessful()) {
                                callback.onSuccess(createdExpense);
                            } else {
                                callback.onError("Failed to add participants");
                            }
                        }

                        @Override
                        public void onFailure(Call<List<ExpenseParticipant>> call, Throwable t) {
                            callback.onError(t.getMessage());
                        }
                    });
                } else {
                    callback.onError("Failed to create expense");
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateExpense(String expenseId, String description, double amount, String paidByMemberId,
                              List<String> participantIds, String date, RepositoryCallback<Boolean> callback) {
        Expense expense = new Expense(null, description, amount, paidByMemberId, date);

        service.updateExpense("eq." + expenseId, expense).enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful()) {
                    service.deleteParticipants("eq." + expenseId).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            List<ExpenseParticipant> participants = new ArrayList<>();
                            for (String participantId : participantIds) {
                                participants.add(new ExpenseParticipant(expenseId, participantId));
                            }

                            service.addParticipants(participants).enqueue(new Callback<List<ExpenseParticipant>>() {
                                @Override
                                public void onResponse(Call<List<ExpenseParticipant>> call, Response<List<ExpenseParticipant>> response) {
                                    callback.onSuccess(response.isSuccessful());
                                }

                                @Override
                                public void onFailure(Call<List<ExpenseParticipant>> call, Throwable t) {
                                    callback.onError(t.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            callback.onError(t.getMessage());
                        }
                    });
                } else {
                    callback.onError("Failed to update expense");
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteExpense(String expenseId, RepositoryCallback<Boolean> callback) {
        service.deleteExpense("eq." + expenseId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getGroupExpenses(String groupId, RepositoryCallback<List<Expense>> callback) {
        service.getExpenses("eq." + groupId, "*", "date.desc").enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Expense> expenses = response.body();
                    loadParticipantsForExpenses(expenses, callback);
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    private void loadParticipantsForExpenses(List<Expense> expenses, RepositoryCallback<List<Expense>> callback) {
        if (expenses.isEmpty()) {
            callback.onSuccess(expenses);
            return;
        }

        final int[] loadedCount = {0};

        for (Expense expense : expenses) {
            getExpenseParticipants(expense.getId(), new RepositoryCallback<List<String>>() {
                @Override
                public void onSuccess(List<String> participantIds) {
                    expense.setParticipants(participantIds);
                    loadedCount[0]++;

                    if (loadedCount[0] == expenses.size()) {
                        callback.onSuccess(expenses);
                    }
                }

                @Override
                public void onError(String error) {
                    loadedCount[0]++;

                    if (loadedCount[0] == expenses.size()) {
                        callback.onSuccess(expenses);
                    }
                }
            });
        }
    }

    public void getExpenseById(String expenseId, RepositoryCallback<Expense> callback) {
        service.getExpenseById("eq." + expenseId, "*").enqueue(new Callback<List<Expense>>() {
            @Override
            public void onResponse(Call<List<Expense>> call, Response<List<Expense>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Expense expense = response.body().get(0);

                    getExpenseParticipants(expenseId, new RepositoryCallback<List<String>>() {
                        @Override
                        public void onSuccess(List<String> participantIds) {
                            expense.setParticipants(participantIds);
                            callback.onSuccess(expense);
                        }

                        @Override
                        public void onError(String error) {
                            callback.onError(error);
                        }
                    });
                } else {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onFailure(Call<List<Expense>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getExpenseParticipants(String expenseId, RepositoryCallback<List<String>> callback) {
        service.getParticipants("eq." + expenseId, "*").enqueue(new Callback<List<ExpenseParticipant>>() {
            @Override
            public void onResponse(Call<List<ExpenseParticipant>> call, Response<List<ExpenseParticipant>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> participantIds = new ArrayList<>();
                    for (ExpenseParticipant p : response.body()) {
                        participantIds.add(p.getMemberId());
                    }
                    callback.onSuccess(participantIds);
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<ExpenseParticipant>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // ==================== SETTLEMENT OPERATIONS ====================

    public void addSettlement(String groupId, String fromMemberId, String toMemberId, double amount,
                              RepositoryCallback<Settlement> callback) {
        Settlement settlement = new Settlement(groupId, fromMemberId, toMemberId, amount, "pending");

        service.createSettlement(settlement).enqueue(new Callback<List<Settlement>>() {
            @Override
            public void onResponse(Call<List<Settlement>> call, Response<List<Settlement>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Failed to create settlement");
                }
            }

            @Override
            public void onFailure(Call<List<Settlement>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateSettlementStatus(String settlementId, String status, RepositoryCallback<Boolean> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);

        service.updateSettlement("eq." + settlementId, updates).enqueue(new Callback<List<Settlement>>() {
            @Override
            public void onResponse(Call<List<Settlement>> call, Response<List<Settlement>> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<List<Settlement>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteSettlement(String settlementId, RepositoryCallback<Boolean> callback) {
        service.deleteSettlement("eq." + settlementId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                callback.onSuccess(response.isSuccessful());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void getGroupSettlements(String groupId, RepositoryCallback<List<Settlement>> callback) {
        service.getSettlements("eq." + groupId, "*", "date.desc").enqueue(new Callback<List<Settlement>>() {
            @Override
            public void onResponse(Call<List<Settlement>> call, Response<List<Settlement>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<Settlement>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    // ==================== JOIN REQUEST OPERATIONS ====================

    public void createJoinRequest(String groupId, String requesterName, String requesterEmail,
                                  RepositoryCallback<com.example.expenseapp.models.JoinRequest> callback) {
        com.example.expenseapp.models.JoinRequest request =
                new com.example.expenseapp.models.JoinRequest(groupId, requesterName, requesterEmail);

        Log.d(TAG, "Creating join request - Group: " + groupId + ", Name: " + requesterName);

        service.createJoinRequest(request).enqueue(new Callback<List<com.example.expenseapp.models.JoinRequest>>() {
            @Override
            public void onResponse(Call<List<com.example.expenseapp.models.JoinRequest>> call,
                                   Response<List<com.example.expenseapp.models.JoinRequest>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Failed to create join request");
                }
            }

            @Override
            public void onFailure(Call<List<com.example.expenseapp.models.JoinRequest>> call, Throwable t) {
                Log.e(TAG, "Create join request failed: " + t.getMessage());
                callback.onError(t.getMessage());
            }
        });
    }

    public void getMyJoinRequests(RepositoryCallback<List<com.example.expenseapp.models.JoinRequest>> callback) {
        // Get current user email
        String currentEmail = context.getSharedPreferences("ExpenseApp", Context.MODE_PRIVATE)
                .getString("current_user_email", "");

        if (currentEmail.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        Log.d(TAG, "Getting join requests for email: " + currentEmail);

        // Get all groups
        getAllGroups(new RepositoryCallback<List<Group>>() {
            @Override
            public void onSuccess(List<Group> groups) {
                if (groups.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }

                List<com.example.expenseapp.models.JoinRequest> myRequests = new ArrayList<>();
                final int[] processedCount = {0};

                // Check all groups for join requests
                for (Group group : groups) {
                    service.getJoinRequests("eq." + group.getId(), "*", "created_date.desc")
                            .enqueue(new Callback<List<com.example.expenseapp.models.JoinRequest>>() {
                                @Override
                                public void onResponse(Call<List<com.example.expenseapp.models.JoinRequest>> call,
                                                       Response<List<com.example.expenseapp.models.JoinRequest>> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        // Only add requests that belong to current user's email
                                        for (com.example.expenseapp.models.JoinRequest req : response.body()) {
                                            // Show request ONLY if requester_email matches current user email
                                            // AND status is pending
                                            if (req.getRequesterEmail() != null &&
                                                    req.getRequesterEmail().equalsIgnoreCase(currentEmail) &&
                                                    req.getStatus().equals("pending")) {
                                                myRequests.add(req);
                                                Log.d(TAG, "Found request for current user: " + req.getRequesterName());
                                            }
                                        }
                                    }

                                    processedCount[0]++;
                                    if (processedCount[0] == groups.size()) {
                                        Log.d(TAG, "Total requests for " + currentEmail + ": " + myRequests.size());
                                        callback.onSuccess(myRequests);
                                    }
                                }

                                @Override
                                public void onFailure(Call<List<com.example.expenseapp.models.JoinRequest>> call, Throwable t) {
                                    Log.e(TAG, "Failed to fetch requests: " + t.getMessage());
                                    processedCount[0]++;
                                    if (processedCount[0] == groups.size()) {
                                        callback.onSuccess(myRequests);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void updateJoinRequestStatus(String requestId, String status, RepositoryCallback<Boolean> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);

        service.updateJoinRequest("eq." + requestId, updates)
                .enqueue(new Callback<List<com.example.expenseapp.models.JoinRequest>>() {
                    @Override
                    public void onResponse(Call<List<com.example.expenseapp.models.JoinRequest>> call,
                                           Response<List<com.example.expenseapp.models.JoinRequest>> response) {
                        callback.onSuccess(response.isSuccessful());
                    }

                    @Override
                    public void onFailure(Call<List<com.example.expenseapp.models.JoinRequest>> call, Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }
}

// Location: app/src/main/java/com/example/expenseapp/database/ExpenseRepository.java