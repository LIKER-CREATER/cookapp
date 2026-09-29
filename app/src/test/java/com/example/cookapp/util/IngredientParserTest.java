package com.example.cookapp.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IngredientParserTest {
    @Test
    public void parsesNameQuantityAndUnit() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("五花肉500g");

        assertEquals("五花肉", parsed.name);
        assertEquals(500, parsed.quantity);
        assertEquals("g", parsed.unit);
    }

    @Test
    public void parsesChineseUnit() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("土豆2个");

        assertEquals("土豆", parsed.name);
        assertEquals(2, parsed.quantity);
        assertEquals("个", parsed.unit);
    }

    @Test
    public void parsesAsNeededAmount() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("盐适量");

        assertEquals("盐", parsed.name);
        assertEquals(1, parsed.quantity);
        assertEquals("适量", parsed.unit);
    }

    @Test
    public void defaultsForMissingAmount() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse("葱花");

        assertEquals("葱花", parsed.name);
        assertEquals(1, parsed.quantity);
        assertEquals("", parsed.unit);
    }

    @Test
    public void handlesEmptyInput() {
        IngredientParser.ParsedIngredient parsed = IngredientParser.parse(null);

        assertEquals("", parsed.name);
        assertEquals(1, parsed.quantity);
        assertEquals("", parsed.unit);
    }
}
