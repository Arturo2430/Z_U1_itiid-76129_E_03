package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.VH> {

    private List<Book> data;

    public BookAdapter(List<Book> data) { this.data = data; }

    public void setData(List<Book> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Book b = data.get(pos);
        h.title.setText(b.getTitle());
        h.author.setText(b.getAuthor());
        h.genre.setText(b.getGenre());

        boolean isAvailable = DBHelper.STATUS_AVAILABLE.equals(b.getStatus());
        Context ctx = h.itemView.getContext();

        h.chipStatus.setText(isAvailable ? "Available" : "Borrowed");

        int bg = ContextCompat.getColor(ctx,
                isAvailable ? R.color.status_available : R.color.status_borrowed);
        h.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(bg));
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, author, genre;
        Chip chipStatus;
        VH (View v) {
            super(v);
            title = v.findViewById(R.id.tvTitle);
            author = v.findViewById(R.id.tvAuthor);
            genre = v.findViewById(R.id.tvGenre);
            chipStatus = v.findViewById(R.id.chipStatus);
        }
    }
}
