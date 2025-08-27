package Utils.download;

// create a csv export become a object (i need to make it generic)
public class CsvExport {
    private String fileName;
    private String[] headers;
    private String[][] data;

    public CsvExport(String fileName, String[] headers, String[][] data) {
        this.fileName = fileName;
        this.headers = headers;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public String[] getHeaders() {
        return headers;
    }

    public String[][] getData() {
        return data;
    }
}
