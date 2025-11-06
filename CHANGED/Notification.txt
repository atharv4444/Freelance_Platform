public class Notification {
    private String id;
    private String title;
    private String message;
    private String timestamp;
    private String type; // "PROJECT", "MESSAGE", "PAYMENT", "REVIEW", etc.
    private String color; // For color coding
    private boolean isRead;

    public Notification(String title, String message, String type, String timestamp) {
        this.id = java.util.UUID.randomUUID().toString();
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.isRead = false;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    
    // Setters
    public void setRead(boolean read) { isRead = read; }
}