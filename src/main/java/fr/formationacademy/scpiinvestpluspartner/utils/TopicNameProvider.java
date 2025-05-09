package fr.formationacademy.scpiinvestpluspartner.utils;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import static fr.formationacademy.scpiinvestpluspartner.utils.Constants.*;


@Component
public class TopicNameProvider {
    private final String groupTopic;
    private String scpiInvestRequestTopic;
    private String scpiInvestPartnerResponseTopic;

    public TopicNameProvider(Environment environment) {
        String activeProfile = getActiveProfile(environment);
        this.scpiInvestRequestTopic = SCPI_REQUEST_TOPIC + "-" + activeProfile;
        this.scpiInvestPartnerResponseTopic = SCPI_PARTNER_RESPONSE_TOPIC + "-" + activeProfile;
        this.groupTopic = SCPI_PARTNER_GROUP + "-" + activeProfile;
    }

    private String getActiveProfile(Environment environment) {
        String[] profiles = environment.getActiveProfiles();
        return profiles.length > 0 ? profiles[0] : "UNDEFINED";
    }

    public String getScpiInvestRequestTopic() {
        return scpiInvestRequestTopic;
    }

    public String getScpiInvestPartnerResponseTopic() {
        return scpiInvestPartnerResponseTopic;
    }

    public String getGroupTopic() {
        return groupTopic;
    }
}
