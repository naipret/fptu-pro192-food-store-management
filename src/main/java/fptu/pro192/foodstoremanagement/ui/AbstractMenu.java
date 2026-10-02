package fptu.pro192.foodstoremanagement.ui;

import fptu.pro192.foodstoremanagement.util.ConsoleUtil;
import fptu.pro192.foodstoremanagement.util.TableFormatter;

/**
 * Abstract base class for all console menu views implementing the Template Method Pattern.
 * <p>
 * Standardizes the screen display lifecycle, ASCII frame rendering, input prompting,
 * and navigation conventions (such as option {@code [0]} for return/cancel) across the application.
 */
public abstract class AbstractMenu {

    /**
     * Standard fixed width of menu screens in characters (VT100 80-column standard).
     */
    protected static final int MENU_WIDTH = 80;

    /**
     * Title of the menu view.
     */
    protected final String title;

    /**
     * Constructs an {@link AbstractMenu} with the specified title.
     *
     * @param title The human-readable title of this menu
     */
    public AbstractMenu(String title) {
        this.title = (title == null) ? "" : title;
    }

    /**
     * Template method defining the standard console menu execution lifecycle.
     * <p>
     * Steps executed in loop:
     * <ol>
     *   <li>Clear the console screen.</li>
     *   <li>Render standard header (double line {@code =}).</li>
     *   <li>Render concrete menu body via {@link #renderBody()}.</li>
     *   <li>Render standard footer (single line {@code -}).</li>
     *   <li>Read and validate user selection via {@link ConsoleUtil#readInt(String, int, int)}.</li>
     *   <li>Break loop cleanly if choice is {@code 0}.</li>
     *   <li>Dispatch choice to {@link #handleOption(int)}.</li>
     *   <li>Wait for user acknowledgment via {@link ConsoleUtil#pressEnterToContinue()} if still active.</li>
     * </ol>
     */
    public void display() {
        boolean running = true;
        while (running) {
            ConsoleUtil.clearScreen();
            renderHeader();
            renderBody();
            renderFooter();

            int choice = ConsoleUtil.readInt("Chọn chức năng: ", 0, getMaxOption());
            if (choice == 0) {
                break; // Option 0: Cancel / Return
            }

            running = handleOption(choice);
            if (running) {
                ConsoleUtil.pressEnterToContinue();
            }
        }
    }

    /**
     * Renders the menu header, enclosed with double line ASCII boundaries.
     */
    protected void renderHeader() {
        System.out.println("=" + TableFormatter.repeat('=', MENU_WIDTH - 2) + "=");
        System.out.println(TableFormatter.center(title.toUpperCase(), MENU_WIDTH));
        System.out.println("=" + TableFormatter.repeat('=', MENU_WIDTH - 2) + "=");
    }

    /**
     * Renders the body content containing menu options.
     * <p>
     * Must be implemented by concrete subclasses to display their respective options.
     */
    protected abstract void renderBody();

    /**
     * Renders the menu footer, containing navigation guides and option [0].
     */
    protected void renderFooter() {
        System.out.println("-" + TableFormatter.repeat('-', MENU_WIDTH - 2) + "-");
        System.out.println("[0] Quay lại / Hủy | Nhập số tương ứng để chọn");
        System.out.println("-" + TableFormatter.repeat('-', MENU_WIDTH - 2) + "-");
    }

    /**
     * Handles the user's selected numeric menu option.
     *
     * @param choice The numeric choice entered by user (greater than 0)
     * @return {@code true} to continue running this menu loop, or {@code false} to exit back to caller
     */
    protected abstract boolean handleOption(int choice);

    /**
     * Returns the maximum valid option number for this menu.
     * Defaults to 9 as specified by the standard console template.
     *
     * @return The maximum selectable integer option
     */
    protected int getMaxOption() {
        return 9;
    }
}
