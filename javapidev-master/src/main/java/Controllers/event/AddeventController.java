package Controllers.event;

import Models.User;

/**
 * Controller for adding events
 * This is a simple class to fix import issues
 */
public class AddeventController {
    
    private User currentUser;
    
    /**
     * Set the current user
     * 
     * @param user The current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
} 