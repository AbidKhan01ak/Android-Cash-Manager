package com.akstudios.cashmanager.Database;

import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "CashManagement.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_TRANSACTIONS = "transactions";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_TYPE = "type"; // Income or Expense
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_AMOUNT + " INTEGER, "
                + COLUMN_TYPE + " TEXT, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_DATE + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }

    // Insert a transaction into the database
    public void addTransaction(int amount, String type, String category, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_DATE, date);
        db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
    }

    // Get all transactions
    public ArrayList<String> getAllTransactions() {
        ArrayList<String> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY " + COLUMN_DATE + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                String transaction = cursor.getString(2) + ": $" + cursor.getInt(1) + " (" + cursor.getString(3) + ") - " + cursor.getString(4);
                transactions.add(transaction);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return transactions;
    }

    public int getBalance() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TYPE + " = 'Income'", null);
        int income = 0;
        if (cursor.moveToFirst()) {
            income = cursor.getInt(0);
        }
        cursor.close();

        cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TYPE + " = 'Expense'", null);
        int expense = 0;
        if (cursor.moveToFirst()) {
            expense = cursor.getInt(0);
        }
        cursor.close();

        return income - expense;
    }
}
