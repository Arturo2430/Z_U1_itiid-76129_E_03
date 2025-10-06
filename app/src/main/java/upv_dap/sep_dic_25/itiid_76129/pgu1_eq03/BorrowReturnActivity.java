package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class BorrowReturnActivity extends AppCompatActivity {

    DBHelper db;
    Spinner spBooks;
    TextView tvStatus;

    List<Book> allBooks;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_borrow_return);

        db = new DBHelper(this);
        spBooks = findViewById(R.id.spBooks);
        tvStatus = findViewById(R.id.tvCurrentStatus);
        Button btnBorrow = findViewById(R.id.btnBorrow);
        Button btnReturn = findViewById(R.id.btnReturn);

        refreshBooks();

        spBooks.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                Book b1 = allBooks.get(position);
                tvStatus.setText("Estado actual: " + (DBHelper.STATUS_AVAILABLE.equals(b1.status) ? "Disponible" : "Prestado"));
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        btnBorrow.setOnClickListener(v -> changeStatus(DBHelper.STATUS_BORROWED));
        btnReturn.setOnClickListener(v -> changeStatus(DBHelper.STATUS_AVAILABLE));
    }

    private void refreshBooks() {
        allBooks = db.getAllBooks();
        String[] titles = new String[allBooks.size()];
        for (int i=0;i<allBooks.size();i++) titles[i]=allBooks.get(i).title;
        spBooks.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, titles));
        if (!allBooks.isEmpty())
            tvStatus.setText("Estado actual: " + (DBHelper.STATUS_AVAILABLE.equals(allBooks.get(0).status) ? "Disponible" : "Prestado"));
    }

    private void changeStatus(String newStatus) {
        if (allBooks.isEmpty()) return;
        int idx = spBooks.getSelectedItemPosition();
        Book b1 = allBooks.get(idx);
        db.updateStatus(b1.id, newStatus);
        refreshBooks();
    }
}
