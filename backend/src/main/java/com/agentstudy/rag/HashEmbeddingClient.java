package com.agentstudy.rag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class HashEmbeddingClient implements EmbeddingClient {

    private static final int DIMENSIONS = 64;
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[\\p{L}\\p{N}_-]+");

    @Override
    public List<Double> embed(String text) {
        double[] vector = new double[DIMENSIONS];
        String safeText = text == null ? "" : text.toLowerCase(Locale.ROOT);

        Matcher matcher = TOKEN_PATTERN.matcher(safeText);
        while (matcher.find()) {
            addToken(vector, matcher.group());
        }

        safeText.codePoints()
                .filter(codePoint -> !Character.isWhitespace(codePoint))
                .filter(codePoint -> !isPunctuation(codePoint))
                .forEach(codePoint -> addToken(vector, "char:" + codePoint));

        normalize(vector);
        List<Double> result = new ArrayList<>(DIMENSIONS);
        for (double value : vector) {
            result.add(value);
        }
        return result;
    }

    private void addToken(double[] vector, String token) {
        int hash = token.hashCode();
        int index = Math.floorMod(hash, DIMENSIONS);
        double direction = (hash & 1) == 0 ? 1.0 : -1.0;
        vector[index] += direction;
    }

    private boolean isPunctuation(int codePoint) {
        int type = Character.getType(codePoint);
        return type == Character.CONNECTOR_PUNCTUATION
                || type == Character.DASH_PUNCTUATION
                || type == Character.START_PUNCTUATION
                || type == Character.END_PUNCTUATION
                || type == Character.INITIAL_QUOTE_PUNCTUATION
                || type == Character.FINAL_QUOTE_PUNCTUATION
                || type == Character.OTHER_PUNCTUATION;
    }

    private void normalize(double[] vector) {
        double sum = 0;
        for (double value : vector) {
            sum += value * value;
        }
        if (sum == 0) {
            return;
        }
        double length = Math.sqrt(sum);
        for (int index = 0; index < vector.length; index++) {
            vector[index] = vector[index] / length;
        }
    }
}
