package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddBookActivity extends AppCompatActivity {

    DBHelper db;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_book);

        db = new DBHelper(this);

        EditText etTitle = findViewById(R.id.etTitle);
        EditText etAuthor = findViewById(R.id.etAuthor);
        EditText etGenre  = findViewById(R.id.etGenre);
        Spinner spStatus  = findViewById(R.id.spStatus);
        Button btnSave    = findViewById(R.id.btnSave);

        spStatus.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{DBHelper.STATUS_AVAILABLE, DBHelper.STATUS_BORROWED}));

        btnSave.setOnClickListener(v -> {
            String t = etTitle.getText().toString().trim();
            if (t.isEmpty()) { etTitle.setError("Requerido"); return; }
            Book b1 = new Book(0, t,
                    etAuthor.getText().toString().trim(),
                    etGenre.getText().toString().trim(),
                    spStatus.getSelectedItem().toString());
            db.insertBook(b1);
            Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
