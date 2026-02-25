package com.farmo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.farmo.R;
import com.farmo.activities.farmerActivities.FarmerOrderManagementActivity;
import com.farmo.R;
import com.farmo.model.Order;
import com.farmo.model.BazarProduct;
import com.farmo.model.Order;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private final List<Order> orders;
    private int expandedPosition = -1;
    private OnOrderActionListener actionListener;

    public OrderAdapter(FarmerOrderManagementActivity context, List<com.farmo.model.Order> displayedOrders, Context context1, List<Order> orders) {
        this.context = context1;
        this.orders = orders;
    }

    public interface OnOrderActionListener {
        void onAccept(Order order, int position);
        void onReject(Order order, int position);
    }

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Order order = orders.get(position);

        holder.tvOrderId.setText(order.getId());
        holder.tvProduct.setText("Product: " + order.getProduct());
        holder.tvQuantity.setText("Qty: " + order.getQuantity());
        holder.tvPrice.setText(String.format("$%.2f", order.getTotalPrice()));
        holder.tvDate.setText(order.getDate());
        holder.tvStatus.setText(order.getStatus());

        // Customer info below card
        holder.tvCustomerInfo.setText(order.getCustomerName() + " • ID: " + order.getUserId());

        // Color-code status badge
        switch (order.getStatus()) {
            case "Accepted":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"));
                break;
            case "Rejected":
                holder.tvStatus.setBackgroundColor(Color.parseColor("#F44336"));
                break;
            case "Pending":
            default:
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"));
                break;
        }

        // Handle expand/collapse
        boolean isExpanded = position == expandedPosition;
        holder.layoutDetails.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        if (isExpanded) {
            // Populate details
            holder.tvDetailName.setText("Customer: " + order.getCustomerName());
            holder.tvDetailUserId.setText("User ID: " + order.getUserId());
            holder.tvDetailAddress.setText("Shipping Address: " + order.getShippingAddress());
            holder.tvDetailDelivery.setText("Expected Delivery: " + order.getExpectedDeliveryDate());

            // Show/hide buttons based on status
            if (order.getStatus().equals("Pending")) {
                holder.layoutButtons.setVisibility(View.VISIBLE);
            } else {
                holder.layoutButtons.setVisibility(View.GONE);
            }
        }

        // Click listener for expand/collapse
        holder.itemView.setOnClickListener(v -> {
            if (expandedPosition == position) {
                // Collapse
                expandedPosition = -1;
                notifyItemChanged(position);
            } else {
                // Expand
                int prevExpandedPosition = expandedPosition;
                expandedPosition = position;
                notifyItemChanged(prevExpandedPosition);
                notifyItemChanged(position);
            }
        });

        // Accept button
        holder.btnAccept.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onAccept(order, position);
            }
        });

        // Reject button
        holder.btnReject.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onReject(order, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvProduct, tvQuantity, tvPrice, tvDate, tvStatus, tvCustomerInfo;
        LinearLayout layoutDetails, layoutButtons;
        TextView tvDetailName, tvDetailUserId, tvDetailAddress, tvDetailDelivery;
        Button btnAccept, btnReject;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvProduct = itemView.findViewById(R.id.tvProduct);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCustomerInfo = itemView.findViewById(R.id.tvCustomerInfo);

            // Detail section
            layoutDetails = itemView.findViewById(R.id.layoutDetails);
            layoutButtons = itemView.findViewById(R.id.layoutButtons);
            tvDetailName = itemView.findViewById(R.id.tvDetailName);
            tvDetailUserId = itemView.findViewById(R.id.tvDetailUserId);
            tvDetailAddress = itemView.findViewById(R.id.tvDetailAddress);
            tvDetailDelivery = itemView.findViewById(R.id.tvDetailDelivery);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }

    public static class BazarProductAdapter extends RecyclerView.Adapter<BazarProductAdapter.ProductViewHolder> {

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
}