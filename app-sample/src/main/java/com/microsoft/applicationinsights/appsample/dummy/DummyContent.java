package com.microsoft.applicationinsights.appsample.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 */
public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    static {
        addItem(new DummyItem("1", "Enable page view tracking"));
        addItem(new DummyItem("2", "Disable page view tracking"));
        addItem(new DummyItem("3", "Enable session management"));
        addItem(new DummyItem("4", "Disable session management"));
        addItem(new DummyItem("5", "Send handled exception"));
        addItem(new DummyItem("6", "Crash the App!"));
        addItem(new DummyItem("7", "Trigger Synchronize"));
        addItem(new DummyItem("8", "Track event"));
        addItem(new DummyItem("9", "Send Metric"));
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String content;

        public DummyItem(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
