package net.simforge.vatsimfleet.processor;

import java.util.LinkedList;
import java.util.Queue;

public class ErrorneousCases {
    private static final Queue<String> cases = new LinkedList<>();

    public static void report(final String _case) {
        synchronized (cases) {
            cases.add(_case);
        }
    }

    public static String getNext() {
        return cases.poll();
    }
}
