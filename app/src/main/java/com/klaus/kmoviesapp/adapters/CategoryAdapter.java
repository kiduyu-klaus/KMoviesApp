package com.klaus.kmoviesapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.klaus.kmoviesapp.R;
import com.klaus.kmoviesapp.models.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    
    private final Context context;
    private final List<Category> categories;
    private final OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(Context context, List<Category> categories, OnCategoryClickListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.categoryName.setText(category.getName());

        // Highlight selected category
        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(
                context.getResources().getColor(R.color.selected_background, null)
            );
        } else {
            holder.cardView.setCardBackgroundColor(
                context.getResources().getColor(R.color.card_background, null)
            );
        }

        holder.cardView.setOnClickListener(v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });

        // Focus handling for TV
        holder.cardView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start();
                holder.categoryName.setTextColor(
                    context.getResources().getColor(R.color.accent, null)
                );
            } else {
                v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start();
                holder.categoryName.setTextColor(
                    context.getResources().getColor(R.color.text_primary, null)
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.categoryCard);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}
