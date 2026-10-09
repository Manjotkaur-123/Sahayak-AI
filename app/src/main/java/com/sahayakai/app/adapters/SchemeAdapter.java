package com.sahayakai.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sahayakai.app.R;
import com.sahayakai.app.models.Scheme;

import java.util.List;

/**
 * SchemeAdapter — displays scheme cards in RecyclerView.
 *
 * Each card shows:
 * - Scheme name and category tag
 * - Summary
 * - Match status badge
 * - View Details button
 */
public class SchemeAdapter extends RecyclerView.Adapter<SchemeAdapter.SchemeViewHolder> {

    public interface OnSchemeClickListener {
        void onSchemeClick(Scheme scheme);
    }

    private Context context;
    private List<Scheme> schemes;
    private OnSchemeClickListener listener;

    public SchemeAdapter(Context context, List<Scheme> schemes, OnSchemeClickListener listener) {
        this.context = context;
        this.schemes = schemes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SchemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_scheme_card, parent, false);
        return new SchemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SchemeViewHolder holder, int position) {
        Scheme scheme = schemes.get(position);

        holder.tvName.setText(scheme.getName());
        holder.tvCategory.setText(scheme.getCategory() != null ? scheme.getCategory() : "");
        holder.tvSummary.setText(scheme.getSummary() != null ?
                (scheme.getSummary().length() > 120 ?
                        scheme.getSummary().substring(0, 120) + "…" :
                        scheme.getSummary()) : "");

        // Match status and percentage
        if (scheme.getMatchStatus() != null) {
            holder.tvMatchStatus.setText(scheme.getMatchStatus() + " — " + scheme.getMatchPercentage() + "%");
            // Color coding
            int color;
            if (scheme.getMatchStatus().contains("Likely")) {
                color = ContextCompat.getColor(context, R.color.status_eligible);
            } else if (scheme.getMatchStatus().contains("Possibly")) {
                color = ContextCompat.getColor(context, R.color.status_possibly);
            } else if (scheme.getMatchStatus().contains("Needs")) {
                color = ContextCompat.getColor(context, R.color.status_verify);
            } else {
                color = ContextCompat.getColor(context, R.color.status_not_eligible);
            }
            holder.tvMatchStatus.setTextColor(color);
        }

        // Deadline
        if (scheme.getDeadline() != null && !scheme.getDeadline().isEmpty()) {
            holder.tvDeadline.setVisibility(View.VISIBLE);
            holder.tvDeadline.setText("📅 " + scheme.getDeadline());
        } else {
            holder.tvDeadline.setVisibility(View.GONE);
        }

        // Click handlers
        holder.btnDetails.setOnClickListener(v -> listener.onSchemeClick(scheme));
        holder.cardView.setOnClickListener(v -> listener.onSchemeClick(scheme));
    }

    @Override
    public int getItemCount() {
        return schemes.size();
    }

    static class SchemeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvName, tvCategory, tvSummary, tvMatchStatus, tvDeadline;
        Button btnDetails;

        SchemeViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_scheme);
            tvName = itemView.findViewById(R.id.tv_scheme_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvSummary = itemView.findViewById(R.id.tv_summary);
            tvMatchStatus = itemView.findViewById(R.id.tv_match_status);
            tvDeadline = itemView.findViewById(R.id.tv_deadline);
            btnDetails = itemView.findViewById(R.id.btn_view_details);
        }
    }
}
