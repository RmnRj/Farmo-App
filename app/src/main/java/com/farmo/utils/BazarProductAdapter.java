package com.farmo.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.farmo.R;

import java.util.List;

public class BazarProductAdapter extends RecyclerView.Adapter<BazarProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<BazarProduct> products;

    public BazarProductAdapter(Context context, List<BazarProduct> products) {
        this.context = context;
        this.products = products;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.activity_product_list, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        BazarProduct product = products.get(position);
        holder.tvProductName.setText(product.getName());
        holder.tvProductPrice.setText(product.getFormattedPrice());
        holder.tvProductDiscount.setText(product.getFormattedDiscount());
        holder.productRating.setRating((float) product.getRating());
        holder.tvReviewCount.setText("(" + product.getReviewCount() + ")");

        // Placeholder for image loading (Use Glide here if implemented)
        holder.ivProductImage.setImageResource(R.drawable.vegetables);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductPrice, tvProductDiscount, tvReviewCount;
        RatingBar productRating;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductDiscount = itemView.findViewById(R.id.tvProductDiscount);
            productRating = itemView.findViewById(R.id.productRating);
            tvReviewCount = itemView.findViewById(R.id.tvReviewCount);
        }
    }
}