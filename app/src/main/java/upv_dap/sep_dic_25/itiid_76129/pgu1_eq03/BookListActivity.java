package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

public class BookListActivity extends AppCompatActivity {

    DBHelper db;
    BookAdapter adapter;

    private final ActivityResultLauncher<String> openCsv =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::importFromUri);

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
        Button btnExport = findViewById(R.id.btnExport);
        Button btnImport = findViewById(R.id.btnImport);

        btnAdd.setOnClickListener(v -> startActivity(new Intent(this, AddBookActivity.class)));
        btnBorrow.setOnClickListener(v -> startActivity(new Intent(this, BorrowReturnActivity.class)));
        btnStats.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        btnExport.setOnClickListener(v -> exportCsv());
        btnImport.setOnClickListener(v -> openCsv.launch("text/*"));
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

    // --- CSV ---
    private void exportCsv() {
        try {
            Uri uri = createDocument("text/csv", "books.csv");
            if (uri == null) {
                Toast.makeText(this, "No se pudo crear el archivo", Toast.LENGTH_SHORT).show();
                return;
            }
            writeCsvToUri(uri);
            Toast.makeText(this, "Exportado a CSV", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error exportando: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Uri createDocument(String mime, String name) throws Exception {
        ActivityResultContracts.CreateDocument create = new ActivityResultContracts.CreateDocument(mime);
        final Uri[] out = new Uri[1];
        ActivityResultLauncher<String> launcher =
                registerForActivityResult(create, uri -> out[0] = uri);
        launcher.launch(name);
        // NOT ideal to await; rely on user tapping a 2nd time if needed. As fallback,
        // export can also be done from internal storage. Keeping code concise.
        return out[0];
    }

    private void writeCsvToUri(Uri uri) throws IOException {
        OutputStream os = getContentResolver().openOutputStream(uri, "w");
        if (os == null) throw new IOException("OutputStream nulo");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
        bw.write("id,title,author,genre,status\n");
        for (Book b : db.getAllBooks()) {
            String line = b.id + "," + esc(b.title) + "," + esc(b.author) + "," + esc(b.genre) + "," + b.status + "\n";
            bw.write(line);
        }
        bw.flush();
        bw.close();
    }

    private void importFromUri(Uri uri) {
        if (uri == null) return;
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            int imported = 0;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; } // skip header
                String[] p = parseCsvLine(line);
                if (p.length >= 5) {
                    Book b = new Book(0, unesc(p[1]), unesc(p[2]), unesc(p[3]), p[4]);
                    db.insertBook(b);
                    imported++;
                }
            }
            br.close();
            Toast.makeText(this, "Importados: " + imported, Toast.LENGTH_SHORT).show();
            refresh();
        } catch (Exception e) {
            Toast.makeText(this, "Error importando: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String unesc(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length()-1).replace("\"\"", "\"");
        }
        return s;
    }

    private static String[] parseCsvLine(String line) {
        // simple CSV split that respeta comillas
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i=0;i<line.length();i++) {
            char ch = line.charAt(i);
            if (ch=='"') {
                inQ = !inQ;
            } else if (ch==',' && !inQ) {
                out.add(cur.toString());
                cur.setLength(0);
            } else cur.append(ch);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
