package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.content.Intent;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupBottomNav(@IdRes int selectedId) {
        BottomNavigationView bottom = findViewById(R.id.bottomNav);
        if (bottom == null) return;

        bottom.setSelectedItemId(selectedId);

        bottom.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.menu_books) {
                if (!(BaseActivity.this instanceof BookListActivity)) {
                    startActivity(new Intent(BaseActivity.this, BookListActivity.class));
                    finish();
                }
                return true;
            }
            if (id == R.id.menu_add) {
                if (!(BaseActivity.this instanceof AddBookActivity)) {
                    startActivity(new Intent(BaseActivity.this, AddBookActivity.class));
                    finish();
                }
                return true;
            }
            if (id == R.id.menu_borrow_return) {
                if (!(BaseActivity.this instanceof BorrowReturnActivity)) {
                    startActivity(new Intent(BaseActivity.this, BorrowReturnActivity.class));
                    finish();
                }
                return true;
            }
            if (id == R.id.menu_stats) {
                if (!(BaseActivity.this instanceof StaticsActivity)) {
                    startActivity(new Intent(BaseActivity.this, StaticsActivity.class));
                    finish();
                }
                return true;
            }
            return false;
        });
    }
}
