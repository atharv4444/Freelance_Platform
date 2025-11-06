import java.util.*;
import java.util.stream.Collectors;

public class NotificationManager {
    private final DatabaseManager db;
    private List<Notification> notifications;
    private static final int MAX_NOTIFICATIONS = 100;

    public NotificationManager(DatabaseManager db) {
        this.db = db;
        this.notifications = Collections.synchronizedList(new ArrayList<>());
        loadNotifications();
    }

    // Load notifications from database
    private void loadNotifications() {
        // TODO: Replace with actual database query
        // For now, load sample data
        
        addNotificationInternal(new Notification(
            "🎯 New Project Match",
            "A new project matching your skills has been posted: 'Full Stack Web Development'",
            "PROJECT",
            "2 minutes ago"
        ));
        
        addNotificationInternal(new Notification(
            "💬 New Message",
            "Client John Smith replied to your bid on 'Mobile App Development'",
            "MESSAGE",
            "15 minutes ago"
        ));
        
        addNotificationInternal(new Notification(
            "💰 Payment Released",
            "Payment of $500 has been released for project 'Logo Design'",
            "PAYMENT",
            "1 hour ago"
        ));
        
        addNotificationInternal(new Notification(
            "⭐ New Review",
            "Client rated your work 5 stars on 'Website Redesign' project",
            "REVIEW",
            "3 hours ago"
        ));
        
        addNotificationInternal(new Notification(
            "📋 Project Milestone",
            "Milestone 2 approved for 'E-commerce Platform Development'",
            "MILESTONE",
            "5 hours ago"
        ));
        
        addNotificationInternal(new Notification(
            "🔔 System Update",
            "Platform maintenance scheduled for tonight 11 PM - 2 AM",
            "SYSTEM",
            "1 day ago"
        ));
    }

    // Get all notifications
    public List<Notification> getAllNotifications() {
        return new ArrayList<>(notifications);
    }

    // Get unread count
    public int getUnreadCount() {
        return (int) notifications.stream()
            .filter(n -> !n.isRead())
            .count();
    }

    // Mark as read
    public void markAsRead(String notificationId) {
        notifications.stream()
            .filter(n -> n.getId().equals(notificationId))
            .forEach(n -> n.setRead(true));
        // TODO: Update in database
    }

    // Add new notification (called from other managers) - PUBLIC
    public void addNotification(Notification notification) {
        notifications.add(0, notification); // Add to top
        
        // Keep only last MAX_NOTIFICATIONS
        if (notifications.size() > MAX_NOTIFICATIONS) {
            notifications.remove(MAX_NOTIFICATIONS);
        }
        // TODO: Save to database
    }

    // Internal method for loading initial notifications
    private void addNotificationInternal(Notification notification) {
        notifications.add(notification);
    }

    // Get notifications by type
    public List<Notification> getNotificationsByType(String type) {
        return notifications.stream()
            .filter(n -> n.getType().equals(type))
            .collect(Collectors.toList());
    }

    // Create a new project notification
    public void notifyNewProject(String projectTitle, String projectDescription) {
        Notification notif = new Notification(
            "🎯 New Project Match",
            "A new project matching your skills has been posted: '" + projectTitle + "'",
            "PROJECT",
            "now"
        );
        addNotification(notif);
    }

    // Create a new message notification
    public void notifyNewMessage(String clientName, String projectName) {
        Notification notif = new Notification(
            "💬 New Message",
            "Client " + clientName + " replied to your bid on '" + projectName + "'",
            "MESSAGE",
            "now"
        );
        addNotification(notif);
    }

    // Create a payment notification
    public void notifyPaymentReleased(double amount, String projectName) {
        Notification notif = new Notification(
            "💰 Payment Released",
            "Payment of $" + amount + " has been released for project '" + projectName + "'",
            "PAYMENT",
            "now"
        );
        addNotification(notif);
    }

    // Create a review notification
    public void notifyNewReview(int rating, String projectName) {
        Notification notif = new Notification(
            "⭐ New Review",
            "Client rated your work " + rating + " stars on '" + projectName + "' project",
            "REVIEW",
            "now"
        );
        addNotification(notif);
    }

    // Create a milestone notification
    public void notifyMilestoneApproved(String milestoneName, String projectName) {
        Notification notif = new Notification(
            "📋 Project Milestone",
            milestoneName + " approved for '" + projectName + "'",
            "MILESTONE",
            "now"
        );
        addNotification(notif);
    }

    // Create system notification
    public void notifySystem(String title, String message) {
        Notification notif = new Notification(title, message, "SYSTEM", "now");
        addNotification(notif);
    }

    // Delete notification
    public void deleteNotification(String notificationId) {
        notifications.removeIf(n -> n.getId().equals(notificationId));
        // TODO: Delete from database
    }

    // Clear all notifications - THIS WAS MISSING!
    public void clearAllNotifications() {
        notifications.clear();
        // TODO: Delete all from database for this user
    }

    // Get total notification count
    public int getTotalNotificationCount() {
        return notifications.size();
    }
}