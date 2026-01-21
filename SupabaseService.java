package com.example.expenseapp.api;

import com.example.expenseapp.models.Expense;
import com.example.expenseapp.models.ExpenseParticipant;
import com.example.expenseapp.models.Group;
import com.example.expenseapp.models.Member;
import com.example.expenseapp.models.Settlement;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseService {

    // Groups
    @POST("groups")
    Call<List<Group>> createGroup(@Body Group group);

    @GET("groups")
    Call<List<Group>> getGroups(@Query("order") String order);

    @GET("groups")
    Call<List<Group>> getGroupByCode(@Query("code") String code, @Query("select") String select);

    @GET("groups")
    Call<List<Group>> getGroupById(@Query("id") String id, @Query("select") String select);

    @DELETE("groups")
    Call<Void> deleteGroup(@Query("id") String id);

    // Members
    @POST("members")
    Call<List<Member>> createMember(@Body Member member);

    @GET("members")
    Call<List<Member>> getMembers(@Query("group_id") String groupId, @Query("select") String select, @Query("order") String order);

    @GET("members")
    Call<List<Member>> getMemberById(@Query("id") String id, @Query("select") String select);

    // Expenses
    @POST("expenses")
    Call<List<Expense>> createExpense(@Body Expense expense);

    @GET("expenses")
    Call<List<Expense>> getExpenses(@Query("group_id") String groupId, @Query("select") String select, @Query("order") String order);

    @GET("expenses")
    Call<List<Expense>> getExpenseById(@Query("id") String id, @Query("select") String select);

    @PATCH("expenses")
    Call<List<Expense>> updateExpense(@Query("id") String id, @Body Expense expense);

    @DELETE("expenses")
    Call<Void> deleteExpense(@Query("id") String id);

    // Expense Participants
    @POST("expense_participants")
    Call<List<ExpenseParticipant>> addParticipants(@Body List<ExpenseParticipant> participants);

    @GET("expense_participants")
    Call<List<ExpenseParticipant>> getParticipants(@Query("expense_id") String expenseId, @Query("select") String select);

    @DELETE("expense_participants")
    Call<Void> deleteParticipants(@Query("expense_id") String expenseId);

    // Settlements
    @POST("settlements")
    Call<List<Settlement>> createSettlement(@Body Settlement settlement);

    @GET("settlements")
    Call<List<Settlement>> getSettlements(@Query("group_id") String groupId, @Query("select") String select, @Query("order") String order);

    @PATCH("settlements")
    Call<List<Settlement>> updateSettlement(@Query("id") String id, @Body Map<String, Object> updates);

    @DELETE("settlements")
    Call<Void> deleteSettlement(@Query("id") String id);

    @POST("join_requests")
    Call<List<com.example.expenseapp.models.JoinRequest>> createJoinRequest(
            @Body com.example.expenseapp.models.JoinRequest request
    );

    @GET("join_requests")
    Call<List<com.example.expenseapp.models.JoinRequest>> getJoinRequests(
            @Query("group_id") String groupId,
            @Query("select") String select,
            @Query("order") String order
    );

    @PATCH("join_requests")
    Call<List<com.example.expenseapp.models.JoinRequest>> updateJoinRequest(
            @Query("id") String id,
            @Body Map<String, Object> updates
    );
}