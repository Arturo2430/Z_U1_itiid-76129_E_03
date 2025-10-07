package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = new DBHelper(this);

        EditText etUser = findViewById(R.id.etUser);
        EditText etPass = findViewById(R.id.etPass);
        Button btn = findViewById(R.id.btnLogin);

        btn.setOnClickListener(v -> {
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString().trim();
            if (db.validateUser(u, p)) {
                startActivity(new Intent(this, BookListActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid username/password", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
