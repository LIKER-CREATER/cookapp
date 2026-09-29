package com.example.cookapp.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IngredientParser {

    private static final Pattern NUMBER_THEN_UNIT = Pattern.compile("^(\\d+)(.+)$");
    private static final Pattern NAME_THEN_NUMBER_UNIT = Pattern.compile("^(.+?)(\\d+)(.*)$");

    public static ParsedIngredient parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new ParsedIngredient("", 1, "");
        }

        String text = raw.trim();

        // 适量 → name="xxx", quantity=1, unit="适量"
        int shiLiangIdx = text.indexOf("适量");
        if (shiLiangIdx != -1) {
            String name = text.replace("适量", "").trim();
            if (name.isEmpty()) name = text;
            return new ParsedIngredient(name, 1, "适量");
        }

        // 格式1: "五花肉500g" → name="五花肉", quantity=500, unit="g"
        Matcher m1 = NAME_THEN_NUMBER_UNIT.matcher(text);
        if (m1.find()) {
            String name = m1.group(1).trim();
            int quantity;
            String unit;
            try {
                quantity = Integer.parseInt(m1.group(2));
                unit = m1.group(3).trim();
            } catch (NumberFormatException e) {
                return new ParsedIngredient(text, 1, "");
            }
            return new ParsedIngredient(name, quantity, unit);
        }

        // 格式2: "500g五花肉" → name="五花肉", quantity=500, unit="g"
        Matcher m2 = NUMBER_THEN_UNIT.matcher(text);
        if (m2.matches()) {
            String numberStr = m2.group(1);
            String rest = m2.group(2).trim();
            try {
                int quantity = Integer.parseInt(numberStr);
                String name = rest;
                String unit = "";
                if (!rest.isEmpty()) {
                    char last = rest.charAt(rest.length() - 1);
                    if (Character.isLetter(last)) {
                        unit = rest.substring(rest.length() - 1);
                        name = rest.substring(0, rest.length() - 1).trim();
                    }
                }
                return new ParsedIngredient(name, quantity, unit);
            } catch (NumberFormatException e) {
                // fall through
            }
        }

        return new ParsedIngredient(text, 1, "");
    }

    public static class ParsedIngredient {
        public final String name;
        public final int quantity;
        public final String unit;

        public ParsedIngredient(String name, int quantity, String unit) {
            this.name = name;
            this.quantity = quantity;
            this.unit = unit;
        }
    }
}
