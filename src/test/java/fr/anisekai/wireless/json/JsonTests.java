package fr.anisekai.wireless.json;

import fr.anisekai.wireless.api.json.AnisekaiJson;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

@DisplayName("JSON")
@Tags({@Tag("unit-test"), @Tag("json")})
public class JsonTests {

    @Test
    @DisplayName("JSON | Put value in object")
    public void testPutSubInJson() {

        AnisekaiJson json = new AnisekaiJson();
        json.put("one.two", "three");

        Assertions.assertTrue(json.hasInTree("one"));
        Assertions.assertTrue(json.hasInTree("one.two"));
        Assertions.assertEquals("three", json.readString("one.two"));
    }

    @Test
    @DisplayName("JSON | Implicit Conversion instance")
    public void testImplicitConversion() {

        JSONObject original = new JSONObject();
        JSONObject sub      = new JSONObject();
        sub.put("two", "three");
        original.put("one", sub);

        AnisekaiJson json     = new AnisekaiJson(original);
        AnisekaiJson instance = json.readJson("one");
        instance.put("four", "five");

        Assertions.assertTrue(json.hasInTree("one.two"));
        Assertions.assertTrue(json.hasInTree("one.four"));
    }

}
