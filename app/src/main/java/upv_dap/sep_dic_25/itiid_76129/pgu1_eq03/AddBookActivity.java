package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class AddBookActivity extends BaseActivity {
    DBHelper db;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_book);
        setupBottomNav(R.id.menu_add);

        db = new DBHelper(this);

        // Inputs
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etAuthor = findViewById(R.id.etAuthor);
        EditText etGenre = findViewById(R.id.etGenre);
        Spinner spStatus = findViewById(R.id.spStatus);
        Button btnSave = findViewById(R.id.btnSave);

        spStatus.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{DBHelper.STATUS_AVAILABLE, DBHelper.STATUS_BORROWED}));

        // Btn Listener
        btnSave.setOnClickListener(v -> {
            String strTitle = etTitle.getText().toString().trim();
            String strAuthor = etAuthor.getText().toString().trim();
            String strGenre = etGenre.getText().toString().trim();
            String strStatus = spStatus.getSelectedItem().toString();
            if (strTitle.isEmpty()) { etTitle.setError("Required"); return; }
            if (strAuthor.isEmpty()) { etAuthor.setError("Required"); return; }
            if (strGenre.isEmpty()) { etGenre.setError("Required"); return; }

            Book b1 = new Book(0, strTitle, strAuthor, strGenre, strStatus);
            db.insertBook(b1);

            etTitle.setText("");
            etAuthor.setText("");
            etGenre.setText("");
            Toast.makeText(this, "Book " + strTitle + " was saving", Toast.LENGTH_SHORT).show();
        });
    }
}
