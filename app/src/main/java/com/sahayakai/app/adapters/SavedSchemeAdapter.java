package com.sahayakai.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sahayakai.app.R;
import com.sahayakai.app.models.Scheme;

import java.util.List;

/**
 * SavedSchemeAdapter — shows saved schemes with View Details and Remove buttons.
 */
public class SavedSchemeAdapter extends RecyclerView.Adapter<SavedSchemeAdapter.SavedViewHolder> {

    public interface OnViewClickListener {
        void onView(Scheme scheme);
    }
    public interface OnRemoveClickListener {
        void onRemove(Scheme scheme);
    }

    private Context context;
    private List<Scheme> schemes;
    private OnViewClickListener viewListener;
    private OnRemoveClickListener removeListener;

    public SavedSchemeAdapter(Context context, List<Scheme> schemes,
                              OnViewClickListener viewListener,
                              OnRemoveClickListener removeListener) {
        this.context = context;
        this.schemes = schemes;
        this.viewListener = viewListener;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public SavedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_saved_scheme, parent, false);
        return new SavedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavedViewHolder holder, int position) {
        Scheme scheme = schemes.get(position);
        holder.tvName.setText(scheme.getName() != null ? scheme.getName() : "");
        holder.tvCategory.setText(scheme.getCategory() != null ? scheme.getCategory() : "");
        holder.tvDeadline.setText(scheme.getDeadline() != null ?
                "📅 " + scheme.getDeadline() : "Deadline: N/A");

        holder.btnView.setOnClickListener(v -> viewListener.onView(scheme));
        holder.btnRemove.setOnClickListener(v -> removeListener.onRemove(scheme));
    }

    @Override
    public int getItemCount() { return schemes.size(); }

    static class SavedViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvDeadline;
        Button btnView, btnRemove;
        SavedViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_scheme_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDeadline = itemView.findViewById(R.id.tv_deadline);
            btnView = itemView.findViewById(R.id.btn_view_details);
            btnRemove = itemView.findViewById(R.id.btn_remove);
        }
    }
}
