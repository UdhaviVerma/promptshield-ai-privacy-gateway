package com.udhav.promptshield.service;

import com.udhav.promptshield.domain.DataCategory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DetectionService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\w)(?:\\+?\\d[\\d\\-() ]{7,}\\d)(?!\\w)");
    private static final Pattern CARD_PATTERN = Pattern.compile("(?<!\\d)(?:\\d[ -]?){13,19}(?!\\d)");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(?i)(sk-[A-Za-z0-9]{20,}|AIza[0-9A-Za-z\\-_]{30,}|ghp_[A-Za-z0-9]{30,})");

    public List<DetectionMatch> detect(String prompt) {
        List<DetectionMatch> matches = new ArrayList<>();
        collect(prompt, EMAIL_PATTERN, DataCategory.EMAIL, matches);
        collectCards(prompt, matches);
        collect(prompt, API_KEY_PATTERN, DataCategory.API_KEY, matches);
        collect(prompt, PHONE_PATTERN, DataCategory.PHONE, matches);
        matches.sort(Comparator.comparingInt(DetectionMatch::start));
        return deduplicateOverlaps(matches);
    }

    private void collect(String text, Pattern pattern, DataCategory category, List<DetectionMatch> matches) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matches.add(new DetectionMatch(category, matcher.group(), matcher.start(), matcher.end()));
        }
    }

    private void collectCards(String text, List<DetectionMatch> matches) {
        Matcher matcher = CARD_PATTERN.matcher(text);
        while (matcher.find()) {
            String candidate = matcher.group();
            String digitsOnly = candidate.replaceAll("[^0-9]", "");
            if (digitsOnly.length() >= 13 && digitsOnly.length() <= 19 && isValidLuhn(digitsOnly)) {
                matches.add(new DetectionMatch(DataCategory.PAYMENT_CARD, candidate, matcher.start(), matcher.end()));
            }
        }
    }

    private List<DetectionMatch> deduplicateOverlaps(List<DetectionMatch> sorted) {
        List<DetectionMatch> deduped = new ArrayList<>();
        int lastEnd = -1;
        for (DetectionMatch match : sorted) {
            if (match.start() >= lastEnd) {
                deduped.add(match);
                lastEnd = match.end();
            }
        }
        return deduped;
    }

    boolean isValidLuhn(String digits) {
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = digits.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }
}
