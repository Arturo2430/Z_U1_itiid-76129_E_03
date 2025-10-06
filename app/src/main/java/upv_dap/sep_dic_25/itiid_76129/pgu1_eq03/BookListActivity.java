package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BookListActivity extends AppCompatActivity {

    DBHelper db;
    BookAdapter adapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_book_list);

        db = new DBHelper(this);

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(db.getAllBooks());
        rv.setAdapter(adapter);

        Button btnAdd = findViewById(R.id.btnAdd);
        Button btnBorrow = findViewById(R.id.btnBorrowReturn);
        Button btnStats = findViewById(R.id.btnStats);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddBookActivity.class)));
        btnBorrow.setOnClickListener(v -> startActivity(new Intent(this, BorrowReturnActivity.class)));
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, StaticsActivity.class)));
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
