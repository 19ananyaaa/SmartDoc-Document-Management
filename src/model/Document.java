package model;

import java.sql.Timestamp;

/**
 * Model class representing an uploaded Document.
 */
public class Document {
    private int id;
    private String name;
    private String filePath;
    private long fileSize;
    private String fileExtension;
    private int categoryId;
    private String categoryName; // Helper for UI display
    private int ownerId;
    private String ownerUsername; // Helper for UI display
    private String ocrText;
    private boolean isFavorite;
    private Timestamp uploadDate;

    // Constructors
    public Document() {}

    public Document(int id, String name, String filePath, long fileSize, String fileExtension, 
                    int categoryId, int ownerId, String ocrText, boolean isFavorite, Timestamp uploadDate) {
        this.id = id;
        this.name = name;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.fileExtension = fileExtension;
        this.categoryId = categoryId;
        this.ownerId = ownerId;
        this.ocrText = ocrText;
        this.isFavorite = isFavorite;
        this.uploadDate = uploadDate;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getOcrText() {
        return ocrText;
    }

    public void setOcrText(String ocrText) {
        this.ocrText = ocrText;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Timestamp getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Timestamp uploadDate) {
        this.uploadDate = uploadDate;
    }

    /**
     * Formats file size in readable format (KB, MB, GB).
     * @return readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) return fileSize + " B";
        int exp = (int) (Math.log(fileSize) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", fileSize / Math.pow(1024, exp), pre);
    }
}
