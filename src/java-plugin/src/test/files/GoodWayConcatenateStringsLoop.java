package fr.cnumr.java.utils;

public class GoodWayConcatenateStringsLoop {

    public String concatenateStrings(String[] strings) {
        StringBuilder result = new StringBuilder();

        for (String string : strings) {
            result.append(string);
        }
        return result.toString();
    }

    public void testConcateOutOfLoop() {
        String result = "";
        result += "another";
    }

    public void testConcateOutOfLoop2() {
        String result = "";
        result = result + "another";
    }

}