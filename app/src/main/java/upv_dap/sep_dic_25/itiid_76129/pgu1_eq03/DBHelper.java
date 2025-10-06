package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "library.db";
    public static final int DB_VERSION = 1;

    public static final String T_USERS = "users";
    public static final String T_BOOKS = "books";

    public static final String STATUS_AVAILABLE = "AVAILABLE";
    public static final String STATUS_BORROWED  = "BORROWED";

    public DBHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_USERS + " (" +
                "username TEXT PRIMARY KEY," +
                "password TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_BOOKS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "author TEXT," +
                "genre TEXT," +
                "status TEXT NOT NULL DEFAULT '" + STATUS_AVAILABLE + "')");

        // usuario por defecto
        ContentValues v = new ContentValues();
        v.put("username", "admin");
        v.put("password", "admin");
        db.insert(T_USERS, null, v);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + T_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        onCreate(db);
    }

    public boolean validateUser(String user, String pass) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + T_USERS +
                " WHERE username=? AND password=?", new String[]{user, pass});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    public long insertBook(Book b) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", b.title);
        v.put("author", b.author);
        v.put("genre", b.genre);
        v.put("status", b.status == null ? STATUS_AVAILABLE : b.status);
        return db.insert(T_BOOKS, null, v);
    }

    public List<Book> getAllBooks() {
        ArrayList<Book> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,title,author,genre,status FROM " + T_BOOKS + " ORDER BY title", null);
        while (c.moveToNext()) {
            Book b = new Book(
                    c.getLong(0),
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4)
            );
            list.add(b);
        }
        c.close();
        return list;
    }

    public List<Book> getBooksByStatus(String status) {
        ArrayList<Book> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,title,author,genre,status FROM " + T_BOOKS + " WHERE status=? ORDER BY title",
                new String[]{status});
        while (c.moveToNext()) {
            list.add(new Book(c.getLong(0), c.getString(1), c.getString(2), c.getString(3), c.getString(4)));
        }
        c.close();
        return list;
    }

    public int updateStatus(long id, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("status", status);
        return db.update(T_BOOKS, v, "id=?", new String[]{String.valueOf(id)});
    }

    public int countByStatus(String status) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + T_BOOKS + " WHERE status=?", new String[]{status});
        int n = 0;
        if (c.moveToFirst()) n = c.getInt(0);
        c.close();
        return n;
    }
}
