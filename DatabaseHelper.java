package com.example.expenseapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ExpenseApp.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_GROUPS = "groups";
    public static final String TABLE_MEMBERS = "members";
    public static final String TABLE_EXPENSES = "expenses";
    public static final String TABLE_SETTLEMENTS = "settlements";

    public static final String COLUMN_GROUP_ID = "id";
    public static final String COLUMN_GROUP_NAME = "name";
    public static final String COLUMN_GROUP_CODE = "code";
    public static final String COLUMN_GROUP_CREATED_AT = "created_at";

    public static final String COLUMN_MEMBER_ID = "id";
    public static final String COLUMN_MEMBER_GROUP_ID = "group_id";
    public static final String COLUMN_MEMBER_NAME = "name";
    public static final String COLUMN_MEMBER_JOINED_AT = "joined_at";

    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_EXPENSE_GROUP_ID = "group_id";
    public static final String COLUMN_EXPENSE_DESCRIPTION = "description";
    public static final String COLUMN_EXPENSE_AMOUNT = "amount";
    public static final String COLUMN_EXPENSE_PAID_BY_ID = "paid_by_id";
    public static final String COLUMN_EXPENSE_PAID_BY_NAME = "paid_by_name";
    public static final String COLUMN_EXPENSE_PARTICIPANTS = "participants";
    public static final String COLUMN_EXPENSE_CREATED_AT = "created_at";

    public static final String COLUMN_SETTLEMENT_ID = "id";
    public static final String COLUMN_SETTLEMENT_GROUP_ID = "group_id";
    public static final String COLUMN_SETTLEMENT_FROM_MEMBER = "from_member";
    public static final String COLUMN_SETTLEMENT_TO_MEMBER = "to_member";
    public static final String COLUMN_SETTLEMENT_AMOUNT = "amount";
    public static final String COLUMN_SETTLEMENT_IS_PAID = "is_paid";
    public static final String COLUMN_SETTLEMENT_CREATED_AT = "created_at";
    public static final String COLUMN_SETTLEMENT_PAID_AT = "paid_at";

    private static final String CREATE_TABLE_GROUPS =
            "CREATE TABLE " + TABLE_GROUPS + "(" +
                    COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_GROUP_NAME + " TEXT NOT NULL," +
                    COLUMN_GROUP_CODE + " TEXT UNIQUE NOT NULL," +
                    COLUMN_GROUP_CREATED_AT + " INTEGER NOT NULL" +
                    ")";

    private static final String CREATE_TABLE_MEMBERS =
            "CREATE TABLE " + TABLE_MEMBERS + "(" +
                    COLUMN_MEMBER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_MEMBER_GROUP_ID + " INTEGER NOT NULL," +
                    COLUMN_MEMBER_NAME + " TEXT NOT NULL," +
                    COLUMN_MEMBER_JOINED_AT + " INTEGER NOT NULL," +
                    "FOREIGN KEY(" + COLUMN_MEMBER_GROUP_ID + ") REFERENCES " +
                    TABLE_GROUPS + "(" + COLUMN_GROUP_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_EXPENSES =
            "CREATE TABLE " + TABLE_EXPENSES + "(" +
                    COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_EXPENSE_GROUP_ID + " INTEGER NOT NULL," +
                    COLUMN_EXPENSE_DESCRIPTION + " TEXT NOT NULL," +
                    COLUMN_EXPENSE_AMOUNT + " REAL NOT NULL," +
                    COLUMN_EXPENSE_PAID_BY_ID + " INTEGER NOT NULL," +
                    COLUMN_EXPENSE_PAID_BY_NAME + " TEXT NOT NULL," +
                    COLUMN_EXPENSE_PARTICIPANTS + " TEXT NOT NULL," +
                    COLUMN_EXPENSE_CREATED_AT + " INTEGER NOT NULL," +
                    "FOREIGN KEY(" + COLUMN_EXPENSE_GROUP_ID + ") REFERENCES " +
                    TABLE_GROUPS + "(" + COLUMN_GROUP_ID + ")" +
                    ")";

    private static final String CREATE_TABLE_SETTLEMENTS =
            "CREATE TABLE " + TABLE_SETTLEMENTS + "(" +
                    COLUMN_SETTLEMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_SETTLEMENT_GROUP_ID + " INTEGER NOT NULL," +
                    COLUMN_SETTLEMENT_FROM_MEMBER + " TEXT NOT NULL," +
                    COLUMN_SETTLEMENT_TO_MEMBER + " TEXT NOT NULL," +
                    COLUMN_SETTLEMENT_AMOUNT + " REAL NOT NULL," +
                    COLUMN_SETTLEMENT_IS_PAID + " INTEGER DEFAULT 0," +
                    COLUMN_SETTLEMENT_CREATED_AT + " INTEGER NOT NULL," +
                    COLUMN_SETTLEMENT_PAID_AT + " INTEGER," +
                    "FOREIGN KEY(" + COLUMN_SETTLEMENT_GROUP_ID + ") REFERENCES " +
                    TABLE_GROUPS + "(" + COLUMN_GROUP_ID + ")" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_GROUPS);
        db.execSQL(CREATE_TABLE_MEMBERS);
        db.execSQL(CREATE_TABLE_EXPENSES);
        db.execSQL(CREATE_TABLE_SETTLEMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_TABLE_SETTLEMENTS);
        }
    }
}