package Utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.util.Optional;

/**
 * Utility class for displaying alerts in the application
 */
public class AlertUtils {
    
    /**
     * Shows an information alert
     * @param title The title of the alert
     * @param header The header text
     * @param content The content text
     */
    public static void showInfo(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows an error alert
     * @param title The title of the alert
     * @param header The header text
     * @param content The content text
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows a warning alert
     * @param title The title of the alert
     * @param header The header text
     * @param content The content text
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Shows a confirmation alert and returns the result
     * @param title The title of the alert
     * @param header The header text
     * @param content The content text
     * @return Optional result containing the button type that was clicked
     */
    public static Optional<ButtonType> showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert.showAndWait();
    }
} 