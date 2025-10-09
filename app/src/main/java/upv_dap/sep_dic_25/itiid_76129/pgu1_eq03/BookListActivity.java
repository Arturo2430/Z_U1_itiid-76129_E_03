package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    private static final int PERMISSION_REQUEST_EXPORT = 201;
    private static final int PERMISSION_REQUEST_IMPORT = 202;

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

        btnExport.setOnClickListener(v -> {
            if (checkStoragePermission(PERMISSION_REQUEST_EXPORT)) {
                showExportChooser();
            }
        });

        btnImport.setOnClickListener(v -> {
            if (checkStoragePermission(PERMISSION_REQUEST_IMPORT)) {
                showImportModeChooser();
            }
        });


        updateCount();
    }

    /**
     * Checks and requests storage permissions based on Android version
     */
    private boolean checkStoragePermission(int requestCode) {
        // Android 10+ uses Scoped Storage, no permissions needed for ACTION_CREATE_DOCUMENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }

        // Android 6-9 requires explicit permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == PERMISSION_REQUEST_EXPORT) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            requestCode);
                    return false;
                }
            } else if (requestCode == PERMISSION_REQUEST_IMPORT) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            requestCode);
                    return false;
                }
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSION_REQUEST_EXPORT) {
                showExportChooser();
            } else if (requestCode == PERMISSION_REQUEST_IMPORT) {
                showImportModeChooser();
            }
        } else {
            Toast.makeText(this, "Storage permissions are required to access files",
                    Toast.LENGTH_LONG).show();
        }
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
        if (resultCode != RESULT_OK || data == null) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Operation cancelled", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        Uri uri = data.getData();
        if (uri == null) {
            Toast.makeText(this, "Could not get file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get persistent URI permissions
        try {
            if (requestCode == READ_REQUEST_CODE) {
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
                importFromUri(uri, pendingImportMode);
            } else if (requestCode == WRITE_REQUEST_CODE) {
                getContentResolver().takePersistableUriPermission(uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                exportToUri(uri);
            }
        } catch (SecurityException e) {
            // Some devices don't support persistent permissions, continue anyway
            if (requestCode == READ_REQUEST_CODE) {
                importFromUri(uri, pendingImportMode);
            } else if (requestCode == WRITE_REQUEST_CODE) {
                exportToUri(uri);
            }
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
        try {
            String mime = (pendingExportFormat == ExportFormat.CSV) ? "text/csv" : "text/plain";
            String ext  = (pendingExportFormat == ExportFormat.CSV) ? ".csv" : ".txt";
            String name = "books-" + new SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(new Date()) + ext;

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(mime);
            intent.putExtra(Intent.EXTRA_TITLE, name);

            // Add flags for compatibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            }

            startActivityForResult(intent, WRITE_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error creating document: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void exportToUri(Uri uri) {
        if (uri == null) {
            Toast.makeText(this, "Invalid file", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            List<Book> books = db.getAllBooks();
            if (books.isEmpty()) {
                Toast.makeText(this, "No books to export", Toast.LENGTH_SHORT).show();
                return;
            }

            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                if (os == null) {
                    throw new Exception("Could not open file for writing");
                }

                // Export based on selected format
                if (pendingExportFormat == ExportFormat.CSV) {
                    FileHelper.writeCsv(os, books, true);
                } else {
                    FileHelper.writeTxt(os, books);
                }

                Toast.makeText(this, "Exported " + books.size() + " books",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Export failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
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
            if (is == null) {
                throw new Exception("Could not open file for reading");
            }

            List<Book> imported = FileHelper.readCsv(is);
            if (imported == null) imported = new ArrayList<>();

            if (imported.isEmpty()) {
                Toast.makeText(this, "No books found in file",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            int inserted = 0, skipped = 0, deleted = 0;

            if (mode == ImportMode.REPLACE_ALL) {
                deleted = db.deleteAllBooks();
                for (Book b : imported) {
                    db.insertBook(b);
                    inserted++;
                }
                Toast.makeText(this, "Replaced: " + inserted + " books (deleted " + deleted + ")",
                        Toast.LENGTH_LONG).show();

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
                Toast.makeText(this, "Added " + inserted + " new, skipped " + skipped + " duplicates",
                        Toast.LENGTH_LONG).show();
            }

            refresh();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Import failed: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private String makeKey(Book b) {
        return normalize(b.getTitle()) + "|" + normalize(b.getAuthor()) + "|" + normalize(b.getGenre());
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }
}
