package fptu.pro192.foodstoremanagement.view;

import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.TableFormatter;
import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * Abstract base class for all 80-column interactive console menus,
 * implementing the Template Method pattern for menu frame rendering,
 * option selection, action dispatch, and screen navigation pauses.
 */
public abstract class Menu {

    public static final String DIVIDER = TableFormatter.repeat('-', TableFormatter.SCREEN_WIDTH);
    private static final String DOUBLE_BORDER = TableFormatter.repeat('=', TableFormatter.SCREEN_WIDTH);

    /**
     * Prints an empty line followed by the standard 80-character divider.
     */
    public static void printDivider() {
        System.out.println();
        System.out.println(DIVIDER);
    }

    /**
     * Uppercase menu screen title centered in the 80-column header banner.
     */
    protected final String title;

    /**
     * Ordered array of menu option labels corresponding to choices 1..N.
     */
    protected final String[] options;

    /**
     * Constructs a Menu with a title and an array of option labels.
     *
     * @param title   the non-empty menu title
     * @param options the non-empty array of option descriptions for choices 1..N
     * @throws IllegalArgumentException if title is blank or options is null/empty
     */
    protected Menu(String title, String[] options) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Menu title cannot be null or empty.");
        }
        if (options == null || options.length == 0) {
            throw new IllegalArgumentException("Menu options cannot be null or empty.");
        }
        this.title = title.trim();
        this.options = Arrays.copyOf(options, options.length);
    }

    /**
     * Template Method executing the interactive menu loop until the user
     * selects 0 to exit or return to the parent menu.
     */
    public final void display() {
        boolean running = true;
        while (running) {
            renderMenuFrame();
            try {
                int choice = ConsoleUtil.readInt(
                        "Enter your choice [0-" + this.options.length + "]: ",
                        0,
                        this.options.length);
                if (choice == 0) {
                    onExit();
                    running = false;
                } else {
                    ConsoleUtil.clearScreen();
                    if (shouldShowBreadcrumb()) {
                        printBreadcrumbHeader(this.title + " > [" + choice + "] " + this.options[choice - 1]);
                    }
                    handleChoice(choice);
                }
            } catch (NoSuchElementException e) {
                running = false;
            }
        }
    }

    /**
     * Renders the 80-column ASCII header banner, numbered options 1..N, and
     * option 0 at the bottom.
     */
    protected void renderMenuFrame() {
        ConsoleUtil.clearScreen();
        System.out.println(DOUBLE_BORDER);
        System.out.println(TableFormatter.center(this.title, TableFormatter.SCREEN_WIDTH));
        System.out.println(DOUBLE_BORDER);
        for (int i = 0; i < this.options.length; i++) {
            System.out.printf("  [%d] %s%n", i + 1, this.options[i]);
        }
        System.out.printf("  [0] %s%n", getZeroOptionLabel());
        System.out.println(DIVIDER);
    }

    /**
     * Dispatches the user's selected menu option (1..N).
     *
     * @param choice the validated 1-based option index selected by the user
     */
    protected abstract void handleChoice(int choice);

    /**
     * Hook method controlling whether an automatic breadcrumb banner is rendered
     * before executing a chosen menu action. Defaults to true. MainMenu overrides
     * this to return false so that top-level dispatching does not print a
     * breadcrumb.
     *
     * @return true to show automatic breadcrumb header, false otherwise
     */
    protected boolean shouldShowBreadcrumb() {
        return true;
    }

    /**
     * Hook method returning the label for option [0]. Defaults to
     * "Back to Main Menu" and is overridden in MainMenu to "Exit".
     *
     * @return the display label for choice 0
     */
    protected String getZeroOptionLabel() {
        return "Back to Main Menu";
    }

    /**
     * Hook method invoked when the user selects 0 to exit the menu.
     */
    protected void onExit() {
    }

    /**
     * Prints a formatted 80-column section header banner for a sub-operation
     * featuring a centered breadcrumb navigation indicator.
     *
     * @param breadcrumb the hierarchical navigation trail (e.g. "FOOD STORE
     *                   MANAGEMENT SYSTEM > [1]
     *                   PRODUCTS > [3] Add product")
     */
    protected void printBreadcrumbHeader(String breadcrumb) {
        System.out.println(DOUBLE_BORDER);
        System.out.println(TableFormatter.center(breadcrumb, TableFormatter.SCREEN_WIDTH));
        System.out.println(DOUBLE_BORDER);
    }
}
