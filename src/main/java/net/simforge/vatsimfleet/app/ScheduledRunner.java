package net.simforge.vatsimfleet.app;

import net.simforge.vatsimfleet.processor.VatsimFleetProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledRunner {
    private static final Logger log = LoggerFactory.getLogger(ScheduledRunner.class);

    @Scheduled(fixedRate = 1000)
    public void run() {
        VatsimFleetProcessor.processOneReport();
    }
}
