package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

public class Book {
    public int id;
    public String title;
    public String author;
    public String genre;
    public int status; // 0 available, 1 borrowed

    public Book(int id, String title, String author, String genre, int status) {
        this.id = id; this.title = title; this.author = author; this.genre = genre; this.status = status;
    }
    public Book(String title, String author, String genre, int status) {
        this(0, title, author, genre, status);
    }
}
