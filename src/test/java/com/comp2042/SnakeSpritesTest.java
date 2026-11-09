package com.comp2042;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class SnakeSpritesTest {
    @ParameterizedTest
    @CsvSource({"1,2,2,3,0", "1,2,2,1,90", "2,1,3,2,180", "3,2,2,3,270"})
    void cornersFollowTheirTwoNeighbours(int tailX, int tailY, int headX, int headY, int rotation) {
        var parts = SnakeSprites.layout(List.of(new Point(tailX, tailY), new Point(2, 2)),
                new Point(headX, headY), 90, 6, 6);
        assertEquals(SnakeSprites.Kind.CORNER, parts.get(1).kind());
        assertEquals(rotation, parts.get(1).rotation());
    }

    @ParameterizedTest
    @CsvSource({"5,3,0,3,90", "0,3,5,3,270", "3,0,3,5,0", "3,5,3,0,180"})
    void tailsPointTowardTheHeadAcrossWrappedEdges(int tailX, int tailY, int headX, int headY, int rotation) {
        var parts = SnakeSprites.layout(List.of(new Point(tailX, tailY)), new Point(headX, headY), rotation, 6, 6);
        assertEquals(rotation, parts.get(0).rotation());
        assertEquals(SnakeSprites.Kind.TAIL, parts.get(0).kind());
        assertEquals(SnakeSprites.Kind.HEAD, parts.get(1).kind());
    }

    @Test
    void straightSegmentsShareTheBoardAxis() {
        var parts = SnakeSprites.layout(List.of(new Point(0, 1), new Point(1, 1)), new Point(2, 1), 90, 6, 6);
        assertEquals(SnakeSprites.Kind.BODY, parts.get(1).kind());
        assertEquals(90, parts.get(1).rotation());
    }

    @Test
    void restartCannotRetainCornersFromAnEarlierSnake() {
        SnakeSprites.layout(List.of(new Point(1, 2), new Point(2, 2)), new Point(2, 3), 180, 6, 6);
        var fresh = SnakeSprites.layout(List.of(new Point(4, 3)), new Point(3, 3), 270, 6, 6);
        assertEquals(List.of(SnakeSprites.Kind.TAIL, SnakeSprites.Kind.HEAD), fresh.stream().map(SnakeSprites.Part::kind).toList());
    }

    @Test
    void longSnakeProducesOneInstructionPerCellWithoutMutatingTheSnapshot() {
        List<Point> body = new ArrayList<>();
        for (int x = 0; x < 1000; x++) body.add(new Point(x, 0));
        var parts = SnakeSprites.layout(body, new Point(1000, 0), 90, 1001, 1);
        assertEquals(1001, parts.size());
        assertEquals(1000, body.size());
        body.get(0).setLocation(100, 100);
        assertEquals(0, parts.get(0).x());
        assertThrows(UnsupportedOperationException.class, () -> parts.clear());
    }
}
