package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookListActivity extends BaseActivity {

    DBHelper db;
    BookAdapter adapter;
    RadioGroup rg;
    Button btnExport, btnImport;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_book_list);
        setupBottomNav(R.id.menu_books);

        db = new DBHelper(this);

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(db.getBooksByAvailable());
        rv.setAdapter(adapter);

        rg = findViewById(R.id.rg_filter);
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb= findViewById(checkedId);

            switch (rb.getText().toString()) {
                case "Available":
                    adapter = new BookAdapter(db.getBooksByAvailable());
                    rv.setAdapter(adapter);
                    break;
                case "Borrowed":
                    adapter = new BookAdapter(db.getBooksByBorrowed());
                    rv.setAdapter(adapter);
                    break;
                case "Show All":
                    adapter = new BookAdapter(db.getAllBooks());
                    rv.setAdapter(adapter);
                    break;
            }
        });

//        btnExport.findViewById(R.id.btn_export);
//        btnImport.findViewById(R.id.btn_import);
//
//        btnExport.setOnClickListener(v -> {
//            // TODO: exportar
//        });
//
//       btnImport.setOnClickListener(v -> {
//            // TODO: importar
//        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<Book> books = db.getAllBooks();
        adapter.setData(books);
    }
}
