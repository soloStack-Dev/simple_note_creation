package com.example.demo.Model;

/**
 * The three note categories offered on the Note page.
 */
public enum Category {

    DAILY_ROUTINE("Daily Routine"),
    MEMORIES("Memories"),
    SOMETHING_ELSE("Something Else");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Lenient lookup used when binding form input. Falls back to SOMETHING_ELSE so a
     * stale or hand-crafted value can never crash the request.
     */
    public static Category from(String raw) {
        if (raw != null) {
            for (Category category : values()) {
                if (category.name().equalsIgnoreCase(raw.trim())) {
                    return category;
                }
            }
        }
        return SOMETHING_ELSE;
    }
}
