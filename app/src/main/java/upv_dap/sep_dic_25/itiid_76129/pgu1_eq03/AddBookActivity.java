package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;

public class AddBookActivity extends BaseActivity {
    DBHelper db;
    AutoCompleteTextView acAuthor;
    AutoCompleteTextView acGenre;
    ArrayAdapter<String> authorAdapter;
    ArrayAdapter<String> genreAdapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_add_book);
        setupBottomNav(R.id.menu_add);

        db = new DBHelper(this);

        TextInputEditText etTitle = findViewById(R.id.etTitle);
        acAuthor = findViewById(R.id.acAuthor);
        acGenre = findViewById(R.id.acGenre);
        AutoCompleteTextView spStatus = findViewById(R.id.spStatus);
        Button btnSave = findViewById(R.id.btnSave);

        loadAuthorSuggestions();
        loadGenreSuggestions();

        String[] statuses = {DBHelper.STATUS_AVAILABLE, DBHelper.STATUS_BORROWED};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, statuses);
        spStatus.setAdapter(statusAdapter);

        btnSave.setOnClickListener(v -> {
            String strTitle = etTitle.getText().toString().trim();
            String strAuthor = acAuthor.getText().toString().trim();
            String strGenre = acGenre.getText().toString().trim();
            String strStatus = spStatus.getText().toString().trim();

            // Validaciones
            if (strTitle.isEmpty()) {
                etTitle.setError("Required");
                etTitle.requestFocus();
                return;
            }
            if (strAuthor.isEmpty()) {
                acAuthor.setError("Required");
                acAuthor.requestFocus();
                return;
            }
            if (strGenre.isEmpty()) {
                acGenre.setError("Required");
                acGenre.requestFocus();
                return;
            }
            if (strStatus.isEmpty()) {
                spStatus.setError("Required");
                spStatus.requestFocus();
                return;
            }

            Book b1 = new Book(0, strTitle, strAuthor, strGenre, strStatus);
            db.insertBook(b1);

            etTitle.setText("");
            acAuthor.setText("");
            acGenre.setText("");
            spStatus.setText("", false);

            loadAuthorSuggestions();
            loadGenreSuggestions();

            Toast.makeText(this, "Book '" + strTitle + "' was saved", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadAuthorSuggestions() {
        ArrayList<String> authorsFromDB = db.getAllAuthors();
        if (authorsFromDB.isEmpty()) {
            authorsFromDB.add("Gabriel García Márquez");
            authorsFromDB.add("J.K. Rowling");
            authorsFromDB.add("Stephen King");
            authorsFromDB.add("George Orwell");
            authorsFromDB.add("Jane Austen");
            authorsFromDB.add("Haruki Murakami");
        }
        authorAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, authorsFromDB);
        acAuthor.setAdapter(authorAdapter);
        acAuthor.setThreshold(1);
    }

    private void loadGenreSuggestions() {
        ArrayList<String> genresFromDB = db.getAllGenres();
        if (genresFromDB.isEmpty()) {
            genresFromDB.add("Fiction");
            genresFromDB.add("Non-Fiction");
            genresFromDB.add("Science Fiction");
            genresFromDB.add("Fantasy");
            genresFromDB.add("Mystery");
            genresFromDB.add("Romance");
            genresFromDB.add("Thriller");
            genresFromDB.add("Biography");
            genresFromDB.add("History");
            genresFromDB.add("Self-Help");
            genresFromDB.add("Poetry");
            genresFromDB.add("Horror");
        }
        genreAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, genresFromDB);
        acGenre.setAdapter(genreAdapter);
        acGenre.setThreshold(1);
    }
}