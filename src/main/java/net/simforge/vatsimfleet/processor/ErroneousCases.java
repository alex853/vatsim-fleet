package net.simforge.vatsimfleet.processor;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ErroneousCases {
    private static final Queue<CaseInfo> queue = new LinkedBlockingQueue<>();
    private static final Map<String, CaseInfo> cases = new TreeMap<>();

    public static void report(final String callsign, String regNo, String report) {
        final String caseCode = callsign.substring(0, Math.min(2, callsign.length()));

        synchronized (cases) {
            CaseInfo caseInfo = cases.computeIfAbsent(caseCode, c -> new CaseInfo(caseCode));
            caseInfo = caseInfo.report(callsign, regNo, report);
            cases.put(caseCode, caseInfo);
            if (caseInfo.getCount() >= 10) {
                cases.remove(caseCode);
                queue.add(caseInfo);
            }
        }
    }

    public static CaseInfo getNext() {
        return queue.poll();
    }

    public static class CaseInfo {
        private final String caseCode;
        private final String caseContent;
        private final int count;

        CaseInfo(final String caseCode) {
            this.caseCode = caseCode;
            this.caseContent = "";
            this.count = 0;
        }

        CaseInfo(final String caseCode, final String caseContent, final int count) {
            this.caseCode = caseCode;
            this.caseContent = caseContent;
            this.count = count;
        }

        CaseInfo report(String callsign, String regNo, String report) {
            final String newContent = this.caseContent
                    + String.format("Callsign %s / RegNo %s / Report %s\n", callsign, regNo, report);
            return new CaseInfo(caseCode, newContent, count + 1);
        }

        public int getCount() {
            return count;
        }

        public String getCaseCode() {
            return caseCode;
        }

        public String getCaseContent() {
            return caseContent;
        }
    }
}
