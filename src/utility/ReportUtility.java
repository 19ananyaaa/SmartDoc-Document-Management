package utility;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import model.Document;
import model.User;

/**
 * Utility class to generate enterprise PDF and CSV reports for administrative review.
 * Integrates OpenPDF (com.lowagie.text) for rich PDF layout formatting.
 * Features an automatic CSV/HTML table compiler fallback if OpenPDF is missing in classpath,
 * preventing server crashes and ensuring runtime continuity.
 */
public class ReportUtility {

    private static boolean openPdfAvailable = false;

    static {
        try {
            Class.forName("com.lowagie.text.Document");
            openPdfAvailable = true;
            System.out.println("OpenPDF library successfully loaded.");
        } catch (ClassNotFoundException e) {
            System.err.println("OpenPDF library not detected in classpath. Using clean CSV/HTML report fallback.");
            openPdfAvailable = false;
        }
    }

    /**
     * Generates a PDF/CSV User status report.
     */
    public static void generateUserReport(List<User> users, OutputStream out, String format) throws IOException {
        if ("csv".equalsIgnoreCase(format) || !openPdfAvailable) {
            generateUserCsv(users, out);
        } else {
            generateUserPdf(users, out);
        }
    }

    /**
     * Generates a PDF/CSV storage usage report.
     */
    public static void generateStorageReport(List<User> users, dao.UserDAO userDAO, OutputStream out, String format) throws IOException {
        if ("csv".equalsIgnoreCase(format) || !openPdfAvailable) {
            generateStorageCsv(users, userDAO, out);
        } else {
            generateStoragePdf(users, userDAO, out);
        }
    }

    /**
     * Generates a PDF/CSV file upload activity report.
     */
    public static void generateUploadReport(List<Document> documents, OutputStream out, String format) throws IOException {
        if ("csv".equalsIgnoreCase(format) || !openPdfAvailable) {
            generateUploadCsv(documents, out);
        } else {
            generateUploadPdf(documents, out);
        }
    }

    // ==========================================
    // PDF GENERATION LOGIC (OpenPDF)
    // ==========================================

    private static void generateUserPdf(List<User> users, OutputStream out) throws IOException {
        try {
            com.lowagie.text.Document doc = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
            doc.open();

            // Document Metadata
            doc.addTitle("Smart Document Platform - User Report");
            doc.addSubject("Generated User Management List");

            // Header Style
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.NORMAL);

            doc.add(new com.lowagie.text.Paragraph("Smart Document Management System", titleFont));
            doc.add(new com.lowagie.text.Paragraph("SYSTEM USER REPORT - Generated: " + new java.util.Date() + "\n\n"));

            // Table setup: ID, Username, Email, Role, Status, Created Date
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.5f, 3.5f, 1.5f, 1.5f, 2.5f});

            // Table headers
            String[] headers = {"ID", "Username", "Email", "Role", "Status", "Registered At"};
            for (String header : headers) {
                com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(header, headerFont));
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                cell.setPadding(6);
                table.addCell(cell);
            }

            // Table Data
            for (User u : users) {
                table.addCell(new com.lowagie.text.Phrase(String.valueOf(u.getId()), dataFont));
                table.addCell(new com.lowagie.text.Phrase(u.getUsername(), dataFont));
                table.addCell(new com.lowagie.text.Phrase(u.getEmail(), dataFont));
                table.addCell(new com.lowagie.text.Phrase(u.getRole(), dataFont));
                table.addCell(new com.lowagie.text.Phrase(u.getStatus(), dataFont));
                table.addCell(new com.lowagie.text.Phrase(u.getCreatedAt().toString(), dataFont));
            }

            doc.add(table);
            doc.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF User report", e);
        }
    }

    private static void generateStoragePdf(List<User> users, dao.UserDAO userDAO, OutputStream out) throws IOException {
        try {
            com.lowagie.text.Document doc = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
            doc.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.NORMAL);

            doc.add(new com.lowagie.text.Paragraph("Smart Document Management System", titleFont));
            doc.add(new com.lowagie.text.Paragraph("STORAGE USAGE REPORT - Generated: " + new java.util.Date() + "\n\n"));

            // Table setup: ID, Username, Email, Storage Limit, Storage Used, Usage %
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 2.5f, 3.5f, 2.0f, 2.0f, 1.5f});

            String[] headers = {"ID", "Username", "Email", "Limit", "Used Space", "Usage %"};
            for (String header : headers) {
                com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(header, headerFont));
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                cell.setPadding(6);
                table.addCell(cell);
            }

            for (User u : users) {
                long limit = u.getStorageLimit();
                long used = userDAO.getUsedStorage(u.getId());
                double percent = limit > 0 ? ((double) used / limit) * 100 : 0;

                table.addCell(new com.lowagie.text.Phrase(String.valueOf(u.getId()), dataFont));
                table.addCell(new com.lowagie.text.Phrase(u.getUsername(), dataFont));
                table.addCell(new com.lowagie.text.Phrase(u.getEmail(), dataFont));
                table.addCell(new com.lowagie.text.Phrase(formatBytes(limit), dataFont));
                table.addCell(new com.lowagie.text.Phrase(formatBytes(used), dataFont));
                table.addCell(new com.lowagie.text.Phrase(String.format("%.2f %%", percent), dataFont));
            }

            doc.add(table);
            doc.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF Storage report", e);
        }
    }

    private static void generateUploadPdf(List<Document> documents, OutputStream out) throws IOException {
        try {
            com.lowagie.text.Document doc = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
            doc.open();

            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font headerFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font dataFont = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9, com.lowagie.text.Font.NORMAL);

            doc.add(new com.lowagie.text.Paragraph("Smart Document Management System", titleFont));
            doc.add(new com.lowagie.text.Paragraph("UPLOAD CONFIGURATION REPORT - Generated: " + new java.util.Date() + "\n\n"));

            // Table setup: ID, Doc Name, Owner, Category, Extension, Size, Upload Date
            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 3.0f, 2.0f, 2.0f, 1.0f, 1.5f, 2.5f});

            String[] headers = {"ID", "Doc Name", "Owner", "Category", "Ext", "Size", "Upload Date"};
            for (String header : headers) {
                com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(header, headerFont));
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                cell.setPadding(6);
                table.addCell(cell);
            }

            for (Document d : documents) {
                table.addCell(new com.lowagie.text.Phrase(String.valueOf(d.getId()), dataFont));
                table.addCell(new com.lowagie.text.Phrase(d.getName(), dataFont));
                table.addCell(new com.lowagie.text.Phrase(d.getOwnerUsername() != null ? d.getOwnerUsername() : "Unknown", dataFont));
                table.addCell(new com.lowagie.text.Phrase(d.getCategoryName() != null ? d.getCategoryName() : "None", dataFont));
                table.addCell(new com.lowagie.text.Phrase(d.getFileExtension(), dataFont));
                table.addCell(new com.lowagie.text.Phrase(d.getFormattedFileSize(), dataFont));
                table.addCell(new com.lowagie.text.Phrase(d.getUploadDate().toString(), dataFont));
            }

            doc.add(table);
            doc.close();
        } catch (Exception e) {
            throw new IOException("Error creating PDF Upload report", e);
        }
    }

    // ==========================================
    // CSV GENERATION LOGIC
    // ==========================================

    private static void generateUserCsv(List<User> users, OutputStream out) {
        PrintWriter pw = new PrintWriter(out);
        pw.println("ID,Username,Email,Role,Status,Registered At");
        for (User u : users) {
            pw.println(String.format("%d,%s,%s,%s,%s,%s",
                u.getId(),
                escapeCsv(u.getUsername()),
                escapeCsv(u.getEmail()),
                u.getRole(),
                u.getStatus(),
                u.getCreatedAt()
            ));
        }
        pw.flush();
    }

    private static void generateStorageCsv(List<User> users, dao.UserDAO userDAO, OutputStream out) {
        PrintWriter pw = new PrintWriter(out);
        pw.println("ID,Username,Email,Storage Limit (Bytes),Storage Used (Bytes),Usage (%)");
        for (User u : users) {
            long limit = u.getStorageLimit();
            long used = userDAO.getUsedStorage(u.getId());
            double percent = limit > 0 ? ((double) used / limit) * 100 : 0;
            pw.println(String.format("%d,%s,%s,%d,%d,%.2f%%",
                u.getId(),
                escapeCsv(u.getUsername()),
                escapeCsv(u.getEmail()),
                limit,
                used,
                percent
            ));
        }
        pw.flush();
    }

    private static void generateUploadCsv(List<Document> documents, OutputStream out) {
        PrintWriter pw = new PrintWriter(out);
        pw.println("ID,Document Name,Owner Username,Category,Extension,Size (Bytes),Upload Date");
        for (Document d : documents) {
            pw.println(String.format("%d,%s,%s,%s,%s,%d,%s",
                d.getId(),
                escapeCsv(d.getName()),
                escapeCsv(d.getOwnerUsername()),
                escapeCsv(d.getCategoryName()),
                d.getFileExtension(),
                d.getFileSize(),
                d.getUploadDate()
            ));
        }
        pw.flush();
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
