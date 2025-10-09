package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;
import java.util.Objects;

public class BorrowReturnActivity extends BaseActivity {

    DBHelper db;
    MaterialAutoCompleteTextView spBooks;
    Chip tvStatus;

    List<Book> allBooks;
    int selectedIndex = -1;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_borrow_return);
        setupBottomNav(R.id.menu_borrow_return);

        db = new DBHelper(this);
        spBooks = findViewById(R.id.spBooks);
        tvStatus = findViewById(R.id.tvCurrentStatus);
        Button btnBorrow = findViewById(R.id.btnBorrow);
        Button btnReturn = findViewById(R.id.btnReturn);

        refreshBooks();

        spBooks.setOnItemClickListener((parent, view, position, id) -> {
            selectedIndex = position;
            Book b1 = allBooks.get(position);
            applyStatus(b1.getStatus());
        });

        btnBorrow.setOnClickListener(v -> changeStatus(DBHelper.STATUS_BORROWED));
        btnReturn.setOnClickListener(v -> changeStatus(DBHelper.STATUS_AVAILABLE));
    }

    private void refreshBooks() {
        allBooks = db.getAllBooks();

        String[] titles = new String[allBooks.size()];
        for (int i = 0; i < allBooks.size(); i++) titles[i] = allBooks.get(i).getTitle();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
                titles
        );
        spBooks.setAdapter(adapter);

        if (!allBooks.isEmpty()) {
            if (selectedIndex < 0 || selectedIndex >= allBooks.size()) selectedIndex = 0;
            spBooks.setText(titles[selectedIndex], false);
            applyStatus(allBooks.get(selectedIndex).getStatus());
        } else {
            selectedIndex = -1;
            tvStatus.setText("Current status: -");
            tvStatus.setChipBackgroundColor(ColorStateList.valueOf(
                    MaterialColors.getColor(tvStatus, com.google.android.material.R.attr.colorSurfaceVariant)
            ));
            tvStatus.setTextColor(MaterialColors.getColor(tvStatus, com.google.android.material.R.attr.colorOnSurface));
        }
    }

    private void changeStatus(String newStatus) {
        if (allBooks == null || allBooks.isEmpty()) return;

        int idx = (selectedIndex >= 0 && selectedIndex < allBooks.size()) ? selectedIndex : 0;
        Book b1 = allBooks.get(idx);

        if (Objects.equals(newStatus, b1.getStatus())) {
            return;
        }
        db.updateStatus(b1.getId(), newStatus);
        Toast.makeText(this, "Status changes to: " + newStatus, Toast.LENGTH_SHORT).show();
        refreshBooks();
    }

    private void applyStatus(String status) {
        boolean isAvailable = DBHelper.STATUS_AVAILABLE.equals(status);
        int bg = ContextCompat.getColor(this, isAvailable ? R.color.status_available : R.color.status_borrowed);
        tvStatus.setText(status);
        tvStatus.setChipBackgroundColor(ColorStateList.valueOf(bg));
    }
}
