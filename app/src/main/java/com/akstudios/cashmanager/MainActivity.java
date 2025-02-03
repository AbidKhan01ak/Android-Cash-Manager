package com.akstudios.cashmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.akstudios.cashmanager.Adaptor.Transaction;
import com.akstudios.cashmanager.Adaptor.TransactionAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private TextView balanceText;
    private EditText amountInput;
    private Button addIncomeButton, addExpenseButton, exportButton;
    private ListView transactionListView;
    private Spinner categorySpinner;
    private int balance = 0;
    private int totalSpent = 0;
    private SharedPreferences sharedPreferences;
    private ArrayList<Transaction> transactions;
    private TransactionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        balanceText = findViewById(R.id.balanceText);
        amountInput = findViewById(R.id.amountInput);
        addIncomeButton = findViewById(R.id.addIncomeButton);
        addExpenseButton = findViewById(R.id.addExpenseButton);
        exportButton = findViewById(R.id.exportButton);
        transactionListView = findViewById(R.id.transactionListView);
        categorySpinner = findViewById(R.id.categorySpinner);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("CashManagerPrefs", Context.MODE_PRIVATE);

        // Get balance from SharedPreferences (default to 0 if not available)
        balance = sharedPreferences.getInt("balance", 0);
        balanceText.setText("Balance: $" + balance);

        // Initialize transactions list and adapter
        transactions = loadTransactions();
        adapter = new TransactionAdapter(this, transactions);
        transactionListView.setAdapter(adapter);

        // Set up category spinner
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.transaction_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Button listeners for adding income and expenses
        addIncomeButton.setOnClickListener(v -> addTransaction(true));
        addExpenseButton.setOnClickListener(v -> addTransaction(false));

        // Export button listener
        exportButton.setOnClickListener(v -> exportTransactionsToCSV());
    }

    private void addTransaction(boolean isIncome) {
        String amountStr = amountInput.getText().toString();
        if (!amountStr.isEmpty()) {
            int amount = Integer.parseInt(amountStr);
            String type = isIncome ? "Income" : "Expense";
            String category;

            if (isIncome) {
                // For income, set category to a default or empty string
                category = "";
            } else {
                category = categorySpinner.getSelectedItem().toString();
            }

            String date = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());

            // Create a Transaction object
            Transaction transaction = new Transaction(type, category, amount, date);

            // Save transaction to SharedPreferences
            saveTransaction(transaction);

            // Update balance
            if (isIncome) {
                balance += amount;
            } else {
                balance -= amount;
                totalSpent += amount;
            }

            // Update balance in SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("balance", balance);
            editor.apply();

            // Update balance and transaction list
            balanceText.setText("Balance: $" + balance);

            // Reload transactions from SharedPreferences
            transactions.clear();
            transactions.addAll(loadTransactions());
            adapter.notifyDataSetChanged();  // Notify adapter to update the UI

            // Clear input field
            amountInput.setText("");
        }
    }

    private ArrayList<Transaction> loadTransactions() {
        ArrayList<Transaction> transactions = new ArrayList<>();
        String transactionsJson = sharedPreferences.getString("transactions", "[]");

        try {
            JSONArray jsonArray = new JSONArray(transactionsJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonTransaction = jsonArray.getJSONObject(i);
                String type = jsonTransaction.getString("type");
                String category = jsonTransaction.getString("category");
                int amount = jsonTransaction.getInt("amount");
                String date = jsonTransaction.getString("date");

                transactions.add(new Transaction(type, category, amount, date));
            }

            // Sort transactions by timestamp (newest first)
            Collections.sort(transactions, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return transactions;
    }


    private void saveTransaction(Transaction transaction) {
        JSONArray transactionsArray = new JSONArray();

        // Add the new transaction (this will be sorted when loaded)
        JSONObject jsonTransaction = new JSONObject();
        try {
            jsonTransaction.put("type", transaction.getType());
            jsonTransaction.put("category", transaction.getCategory());
            jsonTransaction.put("amount", transaction.getAmount());
            jsonTransaction.put("date", transaction.getDate());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        transactionsArray.put(jsonTransaction);

        // Add existing transactions after the new one
        for (Transaction t : transactions) {
            JSONObject jsonTransactionOld = new JSONObject();
            try {
                jsonTransactionOld.put("type", t.getType());
                jsonTransactionOld.put("category", t.getCategory());
                jsonTransactionOld.put("amount", t.getAmount());
                jsonTransactionOld.put("date", t.getDate());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            transactionsArray.put(jsonTransactionOld);
        }

        // Save the updated transaction list to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("transactions", transactionsArray.toString());
        editor.apply();
    }


    private void exportTransactionsToCSV() {
        File csvFile = new File(getExternalFilesDir(null), "transactions.csv");

        try (FileOutputStream fileOutputStream = new FileOutputStream(csvFile);
             OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream)) {

            // Write header row
            writer.append("Type,Category,Amount,Date\n");

            // Write each transaction
            for (Transaction transaction : transactions) {
                writer.append(transaction.getType())
                        .append(",")
                        .append(transaction.getCategory())
                        .append(",")
                        .append(String.valueOf(transaction.getAmount()))
                        .append(",")
                        .append(transaction.getDate())
                        .append("\n");
            }

            // Notify the user
            Toast.makeText(this, "CSV file exported successfully!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting CSV.", Toast.LENGTH_SHORT).show();
        }
    }
}
