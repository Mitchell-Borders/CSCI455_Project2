package Utils;

import java.util.ArrayList;
import java.util.List;

public class AsciiTable {
    private List<String> headers = new ArrayList<>();
    private List<String[]> rows = new ArrayList<>();

    public void setHeaders(String... headers) {
        for (String header : headers) {
            this.headers.add(header);
        }
    }

    public void addRow(String... columns) {
        if (columns.length != headers.size()) {
            throw new IllegalArgumentException("Number of columns must match the number of headers.");
        }
        rows.add(columns);
    }

    public void printTable() {
        int numColumns = headers.size();
        int[] columnWidths = calculateColumnWidths();
    
        String horizontalLine = generateHorizontalLine(columnWidths);
    
        for (int i = 0; i < numColumns; i++) {
            System.out.print("+");
            System.out.print("-".repeat(columnWidths[i] + 2));
        }
        System.out.println("+");
        
        for (int i = 0; i < numColumns; i++) {
            System.out.printf("| %-" + columnWidths[i] + "s ", headers.get(i));
        }
        System.out.println("|");
        
        System.out.println(horizontalLine);
    
        for (String[] row : rows) {
            for (int i = 0; i < numColumns; i++) {
                System.out.printf("| %-" + columnWidths[i] + "s ", row[i]);
            }
            System.out.println("|");
            System.out.println(horizontalLine);
        }
    }
    

    private int[] calculateColumnWidths() {
        int numColumns = headers.size();
        int[] columnWidths = new int[numColumns];

        for (int i = 0; i < numColumns; i++) {
            columnWidths[i] = headers.get(i).length();
        }

        for (String[] row : rows) {
            for (int i = 0; i < numColumns; i++) {
                if (row[i].length() > columnWidths[i]) {
                    columnWidths[i] = row[i].length();
                }
            }
        }

        return columnWidths;
    }

    private String generateHorizontalLine(int[] columnWidths) {
        StringBuilder line = new StringBuilder();
        for (int width : columnWidths) {
            line.append("+").append("-".repeat(width + 2));
        }
        line.append("+");
        return line.toString();
    }
}

