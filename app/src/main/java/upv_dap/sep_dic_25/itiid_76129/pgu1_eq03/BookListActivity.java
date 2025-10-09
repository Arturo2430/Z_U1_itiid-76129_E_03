package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class BookListActivity extends BaseActivity {

    DBHelper db;
    BookAdapter adapter;
    RadioGroup rg;
    Button btnExport, btnImport;
    TextView tvCount;

    private static final int WRITE_REQUEST_CODE = 101;
    private static final int READ_REQUEST_CODE = 102;

    enum ExportFormat { CSV, TXT }
    private ExportFormat pendingExportFormat = ExportFormat.CSV;
    private enum ImportMode { ADD_ONLY, REPLACE_ALL }
    private ImportMode pendingImportMode = ImportMode.ADD_ONLY;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_book_list);
        setupBottomNav(R.id.menu_books);

        db = new DBHelper(this);


        tvCount = findViewById(R.id.tv_count);

        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookAdapter(db.getBooksByAvailable());
        rv.setAdapter(adapter);

        rg = findViewById(R.id.rg_filter);
        rg.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = findViewById(checkedId);
            if (rb == null) return;
            switch (rb.getText().toString()) {
                case "Available":
                    adapter = new BookAdapter(db.getBooksByAvailable());
                    rv.setAdapter(adapter);
                    updateCount();
                    break;
                case "Borrowed":
                    adapter = new BookAdapter(db.getBooksByBorrowed());
                    rv.setAdapter(adapter);
                    updateCount();
                    break;
                case "Show All":
                    adapter = new BookAdapter(db.getAllBooks());
                    rv.setAdapter(adapter);
                    updateCount();
                    break;
            }
        });

        btnExport = findViewById(R.id.btn_export);
        btnImport = findViewById(R.id.btn_import);

        btnExport.setOnClickListener(v -> showExportChooser());
        btnImport.setOnClickListener(v -> showImportModeChooser());

        updateCount();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        List<Book> books = db.getAllBooks();
        adapter.setData(books);
        updateCount();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;

        Uri uri = data.getData();
        if (requestCode == READ_REQUEST_CODE) {
            importFromUri(uri, pendingImportMode);
        } else if (requestCode == WRITE_REQUEST_CODE) {
            exportToUri(uri);
        }
    }

    public void updateCount() {
        int total = db.getAllBooks().size();
        int showing = adapter.getItemCount();
        tvCount.setText("Showing " + showing + " of " + total);
    }

    private void showExportChooser() {
        final String[] items = new String[]{"CSV (.csv)", "Text (.txt)"};
        new AlertDialog.Builder(this)
                .setTitle("Export format")
                .setItems(items, (d, which) -> {
                    pendingExportFormat = (which == 0) ? ExportFormat.CSV : ExportFormat.TXT;
                    createDocumentForExport();
                })
                .show();
    }

    private void showImportModeChooser() {
        final String[] options = new String[]{"Add only (skip duplicates)", "Replace all (clear & import)"};
        new AlertDialog.Builder(this)
                .setTitle("Import mode")
                .setItems(options, (d, which) -> {
                    pendingImportMode = (which == 0) ? ImportMode.ADD_ONLY : ImportMode.REPLACE_ALL;
                    openFileForImport();
                })
                .show();
    }

    private void createDocumentForExport() {
        String mime = (pendingExportFormat == ExportFormat.CSV) ? "text/csv" : "text/plain";
        String ext  = (pendingExportFormat == ExportFormat.CSV) ? ".csv" : ".txt";
        String name = "books-" + new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(new Date()) + ext;

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mime);
        intent.putExtra(Intent.EXTRA_TITLE, name);
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    private void exportToUri(Uri uri) {
        if (uri == null) { Toast.makeText(this, "Invalid file", Toast.LENGTH_SHORT).show(); return; }
        try {
            List<Book> books = db.getAllBooks();
            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                FileHelper.writeCsv(os, books, true);
            }
            Toast.makeText(this, "Exported " + books.size() + " books", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Import Files
    private void openFileForImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private void importFromUri(Uri uri, ImportMode mode) {
        if (uri == null) {
            Toast.makeText(this, "Invalid file", Toast.LENGTH_SHORT).show();
            return;
        }

        try (InputStream is = getContentResolver().openInputStream(uri)) {
            List<Book> imported = FileHelper.readCsv(is);
            if (imported == null) imported = new ArrayList<>();

            int inserted = 0, skipped = 0, deleted = 0;

            if (mode == ImportMode.REPLACE_ALL) {
                deleted = db.deleteAllBooks();
                for (Book b : imported) {
                    db.insertBook(b);
                    inserted++;
                }
                Toast.makeText(this, "Replaced: " + inserted + " books (deleted " + deleted + ")", Toast.LENGTH_LONG).show();

            } else {
                List<Book> existing = db.getAllBooks();
                HashSet<String> keys = new HashSet<>();
                for (Book e : existing) keys.add(makeKey(e));

                for (Book b : imported) {
                    String key = makeKey(b);
                    if (keys.add(key)) {
                        db.insertBook(b);
                        inserted++;
                    } else {
                        skipped++;
                    }
                }
                Toast.makeText(this, "Added " + inserted + " new, skipped " + skipped + " duplicates", Toast.LENGTH_LONG).show();
            }

            refresh();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String makeKey(Book b) {
        return normalize(b.getTitle()) + "|" + normalize(b.getAuthor()) + "|" + normalize(b.getGenre());
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}
