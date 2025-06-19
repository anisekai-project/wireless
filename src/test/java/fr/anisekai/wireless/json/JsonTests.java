package fr.anisekai.wireless.json;

import fr.anisekai.wireless.api.json.AnisekaiArray;
import fr.anisekai.wireless.api.json.AnisekaiJson;
import fr.anisekai.wireless.api.json.exceptions.JSONValidationException;
import fr.anisekai.wireless.api.json.validation.JsonArrayObjectRule;
import fr.anisekai.wireless.api.json.validation.JsonArrayRule;
import fr.anisekai.wireless.api.json.validation.JsonObjectRule;
import fr.anisekai.wireless.api.json.validation.JsonRule;
import org.json.JSONObject;
import org.junit.jupiter.api.*;

@DisplayName("JSON")
@Tags({@Tag("unit-test"), @Tag("json")})
@TestMethodOrder(MethodOrderer.DisplayName.class)
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

    @Test
    @DisplayName("JSON Validation | JSON Object Rule")
    public void testJsonObjectRule() {

        AnisekaiJson json1 = new AnisekaiJson();
        json1.put("string", "hi");
        json1.put("integer", 5);

        AnisekaiJson json2 = new AnisekaiJson();
        json2.put("string", 5);
        json2.put("integer", "hi");
        json2.put("optional", true);

        AnisekaiJson json3 = new AnisekaiJson();
        json3.put("optional", "hehe");

        JsonRule validateString   = new JsonObjectRule("string", true, String.class);
        JsonRule validateInteger  = new JsonObjectRule("integer", true, Integer.class, int.class);
        JsonRule validateOptional = new JsonObjectRule("optional", false, Boolean.class, boolean.class);

        Assertions.assertDoesNotThrow(() -> validateString.validate(json1));
        Assertions.assertDoesNotThrow(() -> validateInteger.validate(json1));
        Assertions.assertDoesNotThrow(() -> validateOptional.validate(json1));

        Assertions.assertThrows(JSONValidationException.class, () -> validateString.validate(json2));
        Assertions.assertThrows(JSONValidationException.class, () -> validateInteger.validate(json2));
        Assertions.assertDoesNotThrow(() -> validateOptional.validate(json2));

        Assertions.assertThrows(JSONValidationException.class, () -> validateOptional.validate(json3));
    }

    @Test
    @DisplayName("JSON Validation | JSON Array Object Rule")
    public void testJsonArrayObjectRule() {

        AnisekaiJson valid = new AnisekaiJson();
        valid.put("string", "hello");
        valid.put("integer", 5);

        AnisekaiJson invalid = new AnisekaiJson();
        invalid.put("string", 5);
        invalid.put("integer", "hello");

        AnisekaiArray validArray = new AnisekaiArray();
        validArray.put(valid);

        AnisekaiArray invalidArray = new AnisekaiArray();
        invalidArray.put(invalid);

        AnisekaiJson empty          = new AnisekaiJson();
        AnisekaiJson emptyArray     = new AnisekaiJson().putInTree("array", new AnisekaiArray());
        AnisekaiJson validContent   = new AnisekaiJson().putInTree("array", validArray);
        AnisekaiJson invalidContent = new AnisekaiJson().putInTree("array", invalidArray);

        JsonRule validateString  = new JsonObjectRule("string", true, String.class);
        JsonRule validateInteger = new JsonObjectRule("integer", true, Integer.class, int.class);

        JsonRule arrayRuleAllowEmpty = new JsonArrayObjectRule(
                "array",
                true,
                true,
                new JsonRule[]{validateString, validateInteger}
        );

        JsonRule arrayRule = new JsonArrayObjectRule(
                "array",
                true,
                false,
                new JsonRule[]{validateString, validateInteger}
        );

        Assertions.assertThrows(JSONValidationException.class, () -> arrayRuleAllowEmpty.validate(empty));
        Assertions.assertDoesNotThrow(() -> arrayRuleAllowEmpty.validate(emptyArray));
        Assertions.assertDoesNotThrow(() -> arrayRuleAllowEmpty.validate(validContent));
        Assertions.assertThrows(JSONValidationException.class, () -> arrayRuleAllowEmpty.validate(invalidContent));

        Assertions.assertThrows(JSONValidationException.class, () -> arrayRule.validate(empty));
        Assertions.assertThrows(JSONValidationException.class, () -> arrayRule.validate(emptyArray));
        Assertions.assertDoesNotThrow(() -> arrayRule.validate(validContent));
        Assertions.assertThrows(JSONValidationException.class, () -> arrayRule.validate(invalidContent));
    }

    @Test
    @DisplayName("JSON Validation | JSON Array Rule")
    public void testJsonArrayRule() {

        AnisekaiArray strings = new AnisekaiArray();
        AnisekaiArray ints    = new AnisekaiArray();
        AnisekaiArray empty   = new AnisekaiArray();

        strings.put("1").put("2").put("3");
        ints.put(1).put(2).put(3);

        AnisekaiJson stringArray  = new AnisekaiJson().putInTree("array", strings);
        AnisekaiJson integerArray = new AnisekaiJson().putInTree("array", ints);
        AnisekaiJson emptyArray   = new AnisekaiJson().putInTree("array", empty);

        JsonRule stringRule  = new JsonArrayRule("array", true, false, String.class);
        JsonRule integerRule = new JsonArrayRule("array", true, false, int.class, Integer.class);

        Assertions.assertDoesNotThrow(() -> stringRule.validate(stringArray));
        Assertions.assertThrows(JSONValidationException.class, () -> stringRule.validate(integerArray));
        Assertions.assertThrows(JSONValidationException.class, () -> stringRule.validate(emptyArray));

        Assertions.assertThrows(JSONValidationException.class, () -> integerRule.validate(stringArray));
        Assertions.assertDoesNotThrow(() -> integerRule.validate(integerArray));
        Assertions.assertThrows(JSONValidationException.class, () -> integerRule.validate(emptyArray));
    }

}
