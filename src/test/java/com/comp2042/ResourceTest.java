package com.comp2042;

import org.junit.jupiter.api.Test;
import javax.xml.parsers.DocumentBuilderFactory;
import javafx.fxml.FXML;
import org.w3c.dom.Element;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/** Verifies packaged resources without starting JavaFX or loading images. */
class ResourceTest {
    @Test
    void menuControllerBindingsAndRuntimeVersionMatchPackagedFxml() throws Exception {
        var factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        try (var input = Main.class.getResourceAsStream("/resources/views/MainMenu.fxml")) {
            var document = factory.newDocumentBuilder().parse(input);
            assertEquals("http://javafx.com/javafx/25", document.getDocumentElement().getAttribute("xmlns"));
            assertEquals(MainMenuController.class.getName(), document.getDocumentElement().getAttribute("fx:controller"));
            Set<String> ids = new HashSet<>();
            var elements = document.getElementsByTagName("*");
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                if (element.hasAttribute("fx:id")) ids.add(element.getAttribute("fx:id"));
                String action = element.getAttribute("onAction");
                if (action.startsWith("#")) assertNotNull(MainMenuController.class.getDeclaredMethod(action.substring(1)));
            }
            for (var field : MainMenuController.class.getDeclaredFields()) {
                if (field.isAnnotationPresent(FXML.class)) assertTrue(ids.contains(field.getName()), field.getName());
            }
        }
    }

    @Test
    void allSnakeSpritesAndMenuResourcesArePackaged() {
        for (SnakeSprites.Kind kind : SnakeSprites.Kind.values()) assertNotNull(Main.class.getResource(kind.resourcePath()));
        for (String resource : new String[] {"MainMenu.fxml", "menu.css", "styling.css", "res/redcoin.png",
                "res/goldcoin.png", "res/rainbowcoin.png", "res/bluecoin.png", "res/silvercoin.png", "res/SnakeLogoWhite.png"}) {
            assertNotNull(Main.class.getResource("/resources/views/" + resource), resource);
        }
    }
}
