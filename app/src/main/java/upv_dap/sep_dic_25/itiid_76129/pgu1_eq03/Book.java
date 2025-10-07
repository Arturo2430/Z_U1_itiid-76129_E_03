package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

public class Book {
    private long id;
    private String title;
    private String author;
    private String genre;
    private String status;

    // Empty construct
    public Book() {}

    // Book Construct
    public Book(long id, String title, String author, String genre, String status) {
        this.id = id; this.title = title; this.author = author; this.genre = genre; this.status = status;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}