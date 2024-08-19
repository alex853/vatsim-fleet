package net.simforge.vatsimfleet.app;

import net.simforge.vatsimfleet.processor.ErroneousCases;
import net.simforge.vatsimfleet.processor.TrelloSender;
import net.simforge.vatsimfleet.processor.VatsimFleetProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Scheduled(fixedRate = 100)
    public void processReport() {
        VatsimFleetProcessor.processOneReport();
    }

    @Scheduled(fixedRate = 30000)
    public void sendTrello() {
        final ErroneousCases.CaseInfo caseInfo = ErroneousCases.getNext();
        if (caseInfo == null) {
            return;
        }

        final String name = String.format("[VATSIM-FLEET] Case %s", caseInfo.getCaseCode());
        final String description = caseInfo.getCaseContent();

        log.warn("Creating Trello card {}", name);
        TrelloSender.send(name,description);
    }
}
