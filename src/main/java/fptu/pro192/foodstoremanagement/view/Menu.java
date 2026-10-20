package fptu.pro192.foodstoremanagement.view;

import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.TableFormatter;
import java.util.Arrays;

/**
 * Abstract base class for all 80-column interactive console menus,
 * implementing the Template Method pattern for menu frame rendering,
 * option selection, action dispatch, and screen navigation pauses.
 */
public abstract class Menu {

    private static final String DOUBLE_BORDER = TableFormatter.repeat('=', TableFormatter.SCREEN_WIDTH);
    private static final String SINGLE_BORDER = TableFormatter.repeat('-', TableFormatter.SCREEN_WIDTH);

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
    public void display() {
        boolean running = true;
        while (running) {
            renderMenuFrame();
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
                if (shouldPauseAfterAction()) {
                    ConsoleUtil.pressEnterToContinue();
                }
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
        System.out.println(SINGLE_BORDER);
    }

    /**
     * Dispatches the user's selected menu option (1..N).
     *
     * @param choice the validated 1-based option index selected by the user
     */
    protected abstract void handleChoice(int choice);

    /**
     * Hook method controlling whether the menu pauses for the user to press
     * Enter after completing an action. Defaults to true for leaf menus and
     * is overridden to return false in MainMenu.
     *
     * @return true to pause after handleChoice, false otherwise
     */
    protected boolean shouldPauseAfterAction() {
        return true;
    }

    /**
     * Hook method controlling whether an automatic breadcrumb banner is rendered
     * before executing a chosen menu action. Defaults to true. MainMenu overrides
     * this to return false so that top-level dispatching does not print a breadcrumb.
     *
     * @return true to show automatic breadcrumb header, false otherwise
     */
    protected boolean shouldShowBreadcrumb() {
        return true;
    }

    /**
     * Hook method returning the label for option [0]. Defaults to
     * "Back to Main Menu" and is overridden in MainMenu to "Exit Application".
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
     * @param breadcrumb the hierarchical navigation trail (e.g. "MAIN MENU > [1]
     *                   PRODUCTS > [3] Add product")
     */
    protected void printBreadcrumbHeader(String breadcrumb) {
        System.out.println(DOUBLE_BORDER);
        System.out.println(TableFormatter.center(breadcrumb, TableFormatter.SCREEN_WIDTH));
        System.out.println(DOUBLE_BORDER);
    }

    /**
     * Returns the menu title.
     *
     * @return the title string
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns a defensive copy of the menu options array.
     *
     * @return a copy of the options array
     */
    public String[] getOptions() {
        return Arrays.copyOf(this.options, this.options.length);
    }
}
