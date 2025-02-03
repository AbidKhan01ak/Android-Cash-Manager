package com.akstudios.cashmanager.Adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.akstudios.cashmanager.R;

import java.util.ArrayList;

public class TransactionAdapter extends ArrayAdapter<Transaction> {

    public TransactionAdapter(Context context, ArrayList<Transaction> transactions) {
        super(context, 0, transactions);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.transaction_item, parent, false);
        }

        TextView transactionText = convertView.findViewById(R.id.transactionText);
        ImageView categoryIcon = convertView.findViewById(R.id.categoryIcon);

        Transaction transaction = getItem(position);

        // Set transaction details
        if (transaction != null) {
            transactionText.setText(transaction.toString());
            categoryIcon.setImageResource(getIconResource(transaction.getCategory()));
        }

        return convertView;
    }

    private int getIconResource(String category) {
        switch (category) {
            case "Food":
                return R.drawable.food_icon;
            case "Travel":
                return R.drawable.travel_icon;
            case "Shopping":
                return R.drawable.shopping_icon;
            case "Rent":
                return R.drawable.rent_icon;
            default:
                return R.drawable.default_icon;
        }
    }
}
