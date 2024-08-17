package net.simforge.vatsimfleet.processor;

import net.simforge.commons.legacy.misc.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;

public class TrelloSender {
    private static final Logger log = LoggerFactory.getLogger(TrelloSender.class);

    public static void send(final String name, final String description) {
        try {
            final String url = String.format("https://api.trello.com/1/cards?idList=%s&key=%s&token=%s&name=%s&desc=%s",
                    Settings.get("trello.idList"),
                    Settings.get("trello.apiKey"),
                    Settings.get("trello.token"),
                    URLEncoder.encode(name),
                    URLEncoder.encode(description));

            final HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");

            final int responseCode = con.getResponseCode();

            if (responseCode != 200) {
                log.error("Unable to create card {}, response code {}", name, responseCode);
            }
        } catch (final IOException e) {
            log.error("Error while creating card {} to Trello", name, e);
        }
    }
}
