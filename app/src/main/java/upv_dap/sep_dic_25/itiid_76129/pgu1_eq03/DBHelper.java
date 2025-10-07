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

    // Constructor for DBHelper.
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the 'users' table
        db.execSQL("CREATE TABLE " + T_USERS + " (username TEXT PRIMARY KEY, password TEXT NOT NULL)");

        // Create the 'books' table
        db.execSQL("CREATE TABLE " + T_BOOKS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, author TEXT, genre TEXT, status TEXT NOT NULL DEFAULT " + " '" + STATUS_AVAILABLE + "')");

        // Insert a default 'admin' user
        ContentValues v = new ContentValues();
        v.put("username", "admin");
        v.put("password", "admin");
        db.insert(T_USERS, null, v);
    }

    // Called when the database needs to be upgraded. Drop the existing tables and recreate them.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS " + T_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);

        onCreate(db);
    }

    /**
     * Validates a user's credentials
     * @param user The username String from the EditText
     * @param pass The password String from the EditText
     * @return Returns true if the username and password match a record in the 'users' table, false otherwise
     */
    public boolean validateUser(String user, String pass) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM " + T_USERS + " WHERE username=? AND password=?", new String[]{user, pass});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    /**
     * Inserts a new book into the 'books' table
     * @param b The Book object to be inserted
     * @return The row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insertBook(Book b) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("title", b.getTitle());
        v.put("author", b.getAuthor());
        v.put("genre", b.getGenre());
        v.put("status", b.getStatus() == null ? STATUS_AVAILABLE : b.getStatus());
        return db.insert(T_BOOKS, null, v);
    }

    // Returns all books from the 'books' table, ordered by title
    public List<Book> getAllBooks() {
        ArrayList<Book> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,title,author,status FROM " + T_BOOKS + " ORDER BY title", null);
        while (c.moveToNext()) {
            Book b = new Book(
                    c.getLong(0),    // id
                    c.getString(1),  // title
                    c.getString(2),  // author
                    c.getString(3), // genre
                    c.getString(3)   // status
            );
            list.add(b);
        }
        c.close();
        return list;
    }

    // Returns all books from the 'books' table that have the 'AVAILABLE' status
    public List<Book> getBooksByAvailable() {
        ArrayList<Book> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,title,author,genre,status FROM " + T_BOOKS + " WHERE status=? ORDER BY title", new String[]{STATUS_AVAILABLE});

        while (c.moveToNext()) {
            Book b = new Book(
                    c.getLong(0),    // id
                    c.getString(1),  // title
                    c.getString(2),  // author
                    c.getString(3),  // genre
                    c.getString(4)   // status
            );
            list.add(b);
        }

        c.close();
        return list;
    }

    // Returns all books from the 'books' table that have the 'BORROWED' status.
    public List<Book> getBooksByBorrowed() {
        ArrayList<Book> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,title,author,genre,status FROM " + T_BOOKS + " WHERE status=? ORDER BY title", new String[]{STATUS_BORROWED});

        while (c.moveToNext()) {
            Book b = new Book(
                    c.getLong(0),    // id
                    c.getString(1),  // title
                    c.getString(2),  // author
                    c.getString(3),  // genre
                    c.getString(4)   // status
            );
            list.add(b);
        }

        c.close();
        return list;
    }


    /**
     * Updates the status of a specific book
     * @param id The ID of the book to update
     * @param status The new status for the book
     * @return The number of rows affected
     */
    public int updateStatus(long id, String status) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("status", status);
        return db.update(T_BOOKS, v, "id=?", new String[]{String.valueOf(id)});
    }

    /**
     * Counts the number of books with a given status.
     * @param status The status to count books by
     * @return The number of books with the specified status
     */
    public int countByStatus(String status) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + T_BOOKS + " WHERE status=?", new String[]{status});
        int n = 0;
        if (c.moveToFirst()) {
            n = c.getInt(0);
        }
        c.close();
        return n;
    }
}