package sy.databus.view.utils;

import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * A few icons.
 *
 * <p>
 * Uses Font Awesome by Dave Gandy - http://fontawesome.io.
 * </p>
 */
public enum AwesomeIcon {

    /**
     * A plus icon.
     */
    PLUS(0xf067),

    /**
     * A times / cross icon.
     */
    TIMES(0xf00d),

    /**
     * A map icon.
     */
    MAP(0xf03e),

    UP_TRI(0xF0D8),

    DOWN_TRI(0xF0D7),

    DELETE(0xF00D),

    UP_FOLD_LINE (0xF077),

    DOWN_FOLD_LINE (0xF078),

    OPEN_FILE (0xF07C),

    DISPLAY_FILE_READER(0xF0CA);

    private static final String STYLE_CLASS = "icon"; //$NON-NLS-1$
    private static final String FONT_AWESOME = "FontAwesome"; //$NON-NLS-1$
    private int unicode;

    private AwesomeIcon(final int pUnicode) {
        this.unicode = pUnicode;
    }

    public Node node() {

        final Text text = new Text(String.valueOf((char) unicode));
        text.getStyleClass().setAll(STYLE_CLASS);
        text.setFont(Font.font(FONT_AWESOME));

        return text;
    }
}
