package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
        h.title.setText(b.title);
        h.subtitle.setText(b.author + " • " + b.genre + " • " + (DBHelper.STATUS_AVAILABLE.equals(b.status) ? "Disponible" : "Prestado"));
    }

    @Override
    public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView title, subtitle;
        VH(View v) { super(v); title = v.findViewById(R.id.tvTitle); subtitle = v.findViewById(R.id.tvSubtitle); }
    }
}
