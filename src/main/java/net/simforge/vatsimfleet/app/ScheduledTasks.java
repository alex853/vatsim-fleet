package net.simforge.vatsimfleet.app;

import net.simforge.vatsimfleet.processor.ErrorneousCases;
import net.simforge.vatsimfleet.processor.TrelloSender;
import net.simforge.vatsimfleet.processor.VatsimFleetProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {
    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    @Scheduled(fixedRate = 1000)
    public void processReport() {
        VatsimFleetProcessor.processOneReport();
    }

    @Scheduled(fixedRate = 30000)
    public void sendTrello() {
        final String message = ErrorneousCases.getNext();
        if (message == null) {
            return;
        }
        log.warn("Sending message {} to Trello", message);
        TrelloSender.send(message);
    }
}
