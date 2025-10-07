package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StaticsActivity extends BaseActivity {

    DBHelper db;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_statics);
        setupBottomNav(R.id.menu_stats);

        db = new DBHelper(this);

        TextView tvAvail = findViewById(R.id.tvAvail);
        TextView tvBorrowed = findViewById(R.id.tvBorrowed);

        int a = db.countByStatus(DBHelper.STATUS_AVAILABLE);
        int br = db.countByStatus(DBHelper.STATUS_BORROWED);

        tvAvail.setText("Disponibles: " + a);
        tvBorrowed.setText("Prestados: " + br);
    }
}
