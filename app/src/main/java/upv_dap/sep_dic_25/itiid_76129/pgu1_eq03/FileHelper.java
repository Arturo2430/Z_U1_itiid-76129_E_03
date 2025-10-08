package upv_dap.sep_dic_25.itiid_76129.pgu1_eq03;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class FileHelper {
    private FileHelper() {}

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    /**
     * Writes a list of books to an OutputStream in CSV format
     * @param os The OutputStream to write to
     * @param books The list of books to write
     * @param includeHeader Whether to include a header row in the CSV
     * @throws IOException If an I/O error occurs
     */
    public static void writeCsv(OutputStream os, List<Book> books, boolean includeHeader) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, UTF8))) {
            if (includeHeader) {
                bw.write("title,author,genre,status");
                bw.newLine();
            }
            for (Book b : books) {
                bw.write(escapeCsv(b.getTitle())); bw.write(',');
                bw.write(escapeCsv(nullToEmpty(b.getAuthor()))); bw.write(',');
                bw.write(escapeCsv(nullToEmpty(b.getGenre()))); bw.write(',');
                bw.write(escapeCsv(normalizeStatus(b.getStatus())));
                bw.newLine();
            }
            bw.flush();
        }
    }

    /**
     * Reads a CSV from an InputStream and returns a list of books
     * @param is The InputStream to read from
     * @return A list of books
     * @throws IOException If an I/O error occurs
     */
    public static List<Book> readCsv(InputStream is) throws IOException {
        List<Book> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF8))) {
            String first = br.readLine();
            if (first == null) return list;

            boolean hasHeader = isHeader(first);
            if (!hasHeader) {
                addBookFromLine(list, first);
            }

            String line;
            while ((line = br.readLine()) != null) {
                addBookFromLine(list, line);
            }
        }
        return list;
    }

    /**
     * Parses a line from a CSV file and adds it as a book to the list
     * @param list The list of books to add to
     * @param line The line to parse
     */
    private static void addBookFromLine(List<Book> list, String line) {
        if (line == null) return;
        line = line.trim();
        if (line.isEmpty()) return;

        ArrayList<String> cols = splitCsvLine(line);
        if (cols.isEmpty()) return;

        String title, author, genre, status;
        long id = 0;

        if (cols.size() >= 5) {
            // id, title, author, genre, status
            id = parseLongSafe(cols.get(0));
            title  = cols.get(1);
            author = getOrEmpty(cols, 2);
            genre  = getOrEmpty(cols, 3);
            status = getOrEmpty(cols, 4);
        } else {
            // title, author, genre, status
            title  = cols.get(0);
            author = getOrEmpty(cols, 1);
            genre  = getOrEmpty(cols, 2);
            status = getOrEmpty(cols, 3);
        }

        if (isEmpty(title)) return;

        Book b = new Book();
        b.setId(id);
        b.setTitle(title);
        b.setAuthor(author);
        b.setGenre(genre);
        b.setStatus(normalizeStatus(status));
        list.add(b);
    }

    // Checks if a line is a header by looking for common column names
    private static boolean isHeader(String line) {
        ArrayList<String> cols = splitCsvLine(line);
        if (cols.isEmpty()) return false;
        for (String c : cols) {
            String k = c.trim().toLowerCase();
            if (k.equals("title") || k.equals("author") || k.equals("genre") || k.equals("status")) {
                return true;
            }
        }
        return false;
    }

    // Normalizes different status strings to a standard format
    private static String normalizeStatus(String s) {
        if (s == null) return DBHelper.STATUS_AVAILABLE;
        String x = s.trim().toUpperCase();
        if (x.equals("AVAILABLE")) return DBHelper.STATUS_AVAILABLE;
        if (x.equals("BORROWED"))  return DBHelper.STATUS_BORROWED;
        if (x.equals("BORROW"))    return DBHelper.STATUS_BORROWED;
        if (x.equals("AVAIL"))     return DBHelper.STATUS_AVAILABLE;
        return DBHelper.STATUS_AVAILABLE;
    }

    // Escapes a string for use in a CSV file
    private static String escapeCsv(String in) {
        if (in == null) return "";
        boolean mustQuote = in.contains(",") || in.contains("\"") || in.contains("\n") || in.contains("\r");
        String out = in.replace("\"", "\"\"");
        return mustQuote ? ("\"" + out + "\"") : out;
    }

    // Splits a line from a CSV file into columns
    private static ArrayList<String> splitCsvLine(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == ',') {
                    out.add(cur.toString());
                    cur.setLength(0);
                } else if (ch == '"') {
                    inQuotes = true;
                } else {
                    cur.append(ch);
                }
            }
        }
        out.add(cur.toString());
        return out;
    }

    // Returns an empty string if the input is null
    private static String nullToEmpty(String s) { return s == null ? "" : s; }

    // Checks if a string is null or empty
    private static boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }

    // Gets an element from a list or returns an empty string if the index is out of bounds
    private static String getOrEmpty(List<String> l, int idx) { return idx < l.size() ? l.get(idx) : ""; }

    // Safely parses a string to a long, returning 0 on failure
    private static long parseLongSafe(String v) {
        try { return Long.parseLong(v.trim()); } catch (Exception e) { return 0; }
    }
}