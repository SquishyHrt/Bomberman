package fr.paolo.wattebled.bomberman.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MapParsing {
    public static List<String> decodeMap(List<String> map) {
        List<String> res = new ArrayList<>();
        for (String line : map) {
            StringBuilder lineRes = new StringBuilder();
            for (int i = 0; i < line.length(); i += 2) {
                int count = Character.getNumericValue(line.charAt(i));
                for (int j = 0; j < count; ++j) {
                    lineRes.append(line.charAt(i + 1));
                }
            }
            res.add(lineRes.toString());
        }
        return res;
    }

    public static List<String> encodeMap(List<String> map) {
        List<String> res = new ArrayList<>();
        for (String line : map) {
            int count = 1;
            StringBuilder lineRes = new StringBuilder();
            for (int i = 0; i < line.length(); ++i) {
                char c = line.charAt(i);
                if (count == 9) {
                    lineRes.append(count).append(c);
                    count = 1;
                } else if (i + 1 < line.length() && line.charAt(i + 1) == c) {
                    count++;
                } else {
                    lineRes.append(count).append(c);
                    count = 1;
                }
            }
            res.add(lineRes.toString());
        }
        return res;
    }

    public static List<String> getMap(String path) {
        List<String> map;
        try {
            map = Files.readAllLines(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}