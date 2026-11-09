package com.comp2042;

import java.awt.Point;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class BoardLayoutTest {
    @ParameterizedTest
    @ValueSource(doubles = {0.5, 1, 1.25, 2, 3})
    void spriteSizeMatchesCellSpacingAtEveryScale(double scale) {
        BoardLayout layout = new BoardLayout(20, 20, 20).fitted(400 * scale, 400 * scale);
        var first = layout.bounds(new Point(0, 0));
        var next = layout.bounds(new Point(1, 1));
        assertEquals(20 * scale, first.size(), 0.000001);
        assertEquals(first.size(), next.x() - first.x(), 0.000001);
        assertEquals(first.size(), next.y() - first.y(), 0.000001);
        var last = layout.bounds(new Point(19, 19));
        assertEquals(layout.width(), last.x() + last.size(), 0.000001);
        assertEquals(layout.height(), last.y() + last.size(), 0.000001);
    }

    @Test
    void fitsActualPlayAreaWithoutOverlappingFooter() {
        BoardLayout layout = new BoardLayout(20, 20, 20).fitted(1920, 900);
        assertEquals(900, layout.width());
        assertEquals(900, layout.height());
        assertEquals(45, layout.cellSize());
    }

    @Test
    void nonSquareBoardsRetainTheirAspectRatio() {
        BoardLayout layout = new BoardLayout(30, 10, 20).fitted(600, 600);
        assertEquals(600, layout.width());
        assertEquals(200, layout.height());
    }

    @Test
    void repeatedFullscreenAndWindowedTransitionsDoNotAccumulateScaling() {
        BoardLayout original = new BoardLayout(20, 20, 20);
        BoardLayout current = original;
        for (int i = 0; i < 10; i++) {
            current = current.fitted(800, 800);
            assertEquals(40, current.cellSize());
            current = current.fitted(400, 400);
            assertEquals(original, current);
        }
    }

    @Test
    void rejectsInvalidSizesRatherThanProducingInvalidCanvasGeometry() {
        assertThrows(IllegalArgumentException.class, () -> new BoardLayout(0, 20, 20));
        assertThrows(IllegalArgumentException.class, () -> new BoardLayout(20, 20, Double.NaN));
        BoardLayout layout = new BoardLayout(20, 20, 20);
        assertThrows(IllegalArgumentException.class, () -> layout.fitted(0, 900));
        assertThrows(IllegalArgumentException.class, () -> layout.fitted(900, Double.POSITIVE_INFINITY));
    }
}
