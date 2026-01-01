package sy.grapheditor.core.skins.defaults.utils;

import javafx.geometry.Point2D;
import sy.grapheditor.api.utils.Arrow;

/**
 * Utils for drawing arrows. Used by connection and tail skins.
 */
public class ArrowUtils {

    /**
     * Draws the given arrow from the start to end points with the given offset from either end.
     *
     * @param arrow an {@link Arrow} to be drawn
     * @param start the start position
     * @param end the end position
     * @param offset an offset from start and end positions
     */
    public static void draw(final Arrow arrow, final Point2D start, final Point2D end, final double offset) {
        final double deltaX = end.getX() - start.getX();
        final double deltaY = end.getY() - start.getY();

        final double angle = Math.atan2(deltaX, deltaY);

        final double startX = start.getX() + offset * Math.sin(angle);
        final double startY = start.getY() + offset * Math.cos(angle);

        final double endX = end.getX() - offset * Math.sin(angle);
        final double endY = end.getY() - offset * Math.cos(angle);

        arrow.setStart(startX, startY);
        arrow.setEnd(endX, endY);
        arrow.draw();

        if (Math.hypot(deltaX, deltaY) < 2 * offset) {
            arrow.setVisible(false);
        } else {
            arrow.setVisible(true);
        }
    }

    public static void drawHeadLorR(final Arrow arrow, final Point2D start, final Point2D end, boolean ifSmart,
                                    final double offset) {
        final double deltaX = ifSmart ? 5 : end.getX() - start.getX();
        final double deltaY = 0;

        final double startX = deltaX > 0 ? end.getX() - 5 - offset : end.getX() + 5 + offset; //start.getX() + offset * Math.sin(angle);
        final double startY = end.getY();

        final double endX = deltaX > 0 ? end.getX() - offset : end.getX() + offset;
        final double endY = end.getY();

        arrow.setStart(startX, startY);
        arrow.setEnd(endX, endY);
        arrow.drawOnlyHead();

        if (Math.hypot(deltaX, deltaY) < 2 * offset) {
            arrow.setVisible(false);
        } else {
            arrow.setVisible(true);
        }
    }

}
