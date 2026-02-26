package com.farmo.activities.commonActivities;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.farmo.R;

import java.util.List;

public class ConnectionAdapter extends RecyclerView.Adapter<ConnectionAdapter.ConnectionViewHolder> {

    private static final String TAG = "ConnectionAdapter";

    public interface OnConnectionActionListener {
        void onDelete(Connection connection, int position);
        void onAccept(Connection connection, int position);
        void onReject(Connection connection, int position);
        void onCancelRequest(Connection connection, int position);
    }

    private final Context                    context;
    private final List<Connection>           list;
    private final OnConnectionActionListener listener;

    public ConnectionAdapter(Context context, List<Connection> list,
                             OnConnectionActionListener listener) {
        this.context  = context;
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_connection, parent, false);
            return new ConnectionViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "onCreateViewHolder error: " + e.getMessage(), e);
            Toast.makeText(context, "Item inflate error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            throw e;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionViewHolder holder, int position) {
        try {
            Connection conn = list.get(position);
            holder.tvFullName.setText(conn.getFullName());
            holder.tvUserId.setText("ID: " + conn.getUserId());

            try {
                Glide.with(context)
                        .load(conn.getProfilePic())
                        .apply(new RequestOptions()
                                .transform(new CircleCrop())
                                .placeholder(R.drawable.ic_default_avatar)
                                .error(R.drawable.ic_default_avatar)
                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(holder.ivProfilePic);
            } catch (Exception glideEx) {
                Log.w(TAG, "Glide error at " + position + ": " + glideEx.getMessage());
                holder.ivProfilePic.setImageResource(R.drawable.ic_default_avatar);
            }

            // Reset
            holder.layoutActionsConnected.setVisibility(View.GONE);
            holder.layoutActionsPending.setVisibility(View.GONE);
            holder.layoutActionsSent.setVisibility(View.GONE);
            holder.divider.setVisibility(View.GONE);

            switch (conn.getStatus()) {
                case CONNECTED:
                    holder.divider.setVisibility(View.VISIBLE);
                    holder.layoutActionsConnected.setVisibility(View.VISIBLE);
                    holder.btnDelete.setOnClickListener(v -> {
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_ID && listener != null)
                            listener.onDelete(list.get(pos), pos);
                    });
                    break;
                case PENDING:
                    holder.divider.setVisibility(View.VISIBLE);
                    holder.layoutActionsPending.setVisibility(View.VISIBLE);
                    holder.btnAccept.setOnClickListener(v -> {
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_ID && listener != null)
                            listener.onAccept(list.get(pos), pos);
                    });
                    holder.btnReject.setOnClickListener(v -> {
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_ID && listener != null)
                            listener.onReject(list.get(pos), pos);
                    });
                    break;
                case SENT:
                    holder.divider.setVisibility(View.VISIBLE);
                    holder.layoutActionsSent.setVisibility(View.VISIBLE);
                    holder.btnCancelRequest.setOnClickListener(v -> {
                        int pos = holder.getAdapterPosition();
                        if (pos != RecyclerView.NO_ID && listener != null)
                            listener.onCancelRequest(list.get(pos), pos);
                    });
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "onBindViewHolder error at " + position + ": " + e.getMessage(), e);
            Toast.makeText(context, "Bind error [" + position + "]: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ConnectionViewHolder extends RecyclerView.ViewHolder {
        ImageView    ivProfilePic;
        TextView     tvFullName, tvUserId;
        View         divider;
        LinearLayout layoutActionsConnected, layoutActionsPending, layoutActionsSent;
        Button       btnDelete, btnCancelRequest;
        LinearLayout btnAccept, btnReject;   // LinearLayout styled as buttons

        ConnectionViewHolder(@NonNull View v) {
            super(v);
            try {
                ivProfilePic           = v.findViewById(R.id.iv_profile_pic);
                tvFullName             = v.findViewById(R.id.tv_full_name);
                tvUserId               = v.findViewById(R.id.tv_user_id);
                divider                = v.findViewById(R.id.divider);
                layoutActionsConnected = v.findViewById(R.id.layout_actions_connected);
                layoutActionsPending   = v.findViewById(R.id.layout_actions_pending);
                layoutActionsSent      = v.findViewById(R.id.layout_actions_sent);
                btnDelete              = v.findViewById(R.id.btn_delete);
                btnAccept              = v.findViewById(R.id.btn_accept);
                btnReject              = v.findViewById(R.id.btn_reject);
                btnCancelRequest       = v.findViewById(R.id.btn_cancel_request);

                if (ivProfilePic           == null) throw new NullPointerException("iv_profile_pic missing in item_connection.xml");
                if (tvFullName             == null) throw new NullPointerException("tv_full_name missing");
                if (tvUserId               == null) throw new NullPointerException("tv_user_id missing");
                if (divider                == null) throw new NullPointerException("divider missing");
                if (layoutActionsConnected == null) throw new NullPointerException("layout_actions_connected missing");
                if (layoutActionsPending   == null) throw new NullPointerException("layout_actions_pending missing");
                if (layoutActionsSent      == null) throw new NullPointerException("layout_actions_sent missing");
                if (btnDelete              == null) throw new NullPointerException("btn_delete missing");
                if (btnAccept              == null) throw new NullPointerException("btn_accept missing");
                if (btnReject              == null) throw new NullPointerException("btn_reject missing");
                if (btnCancelRequest       == null) throw new NullPointerException("btn_cancel_request missing");
            } catch (Exception e) {
                Log.e("ConnectionViewHolder", "Init error: " + e.getMessage(), e);
                throw e;
            }
        }
    }
}