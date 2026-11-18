package co.edu.unicauca.questionbank.infrastructure.jpa.mapper;

import co.edu.unicauca.questionbank.domain.model.Competency;
import co.edu.unicauca.questionbank.domain.model.Topic;
import co.edu.unicauca.questionbank.domain.model.User;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.CompetencyRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.TopicRecord;
import co.edu.unicauca.questionbank.infrastructure.jpa.record.UserRecord;

/** Converts users, topics and competencies between domain objects and JPA records. */
public final class CatalogMapper {

    private CatalogMapper() {
    }

    /**
     * @param record JPA record
     * @return domain user
     */
    public static User toDomain(UserRecord record) {
        return new User(record.getId(), record.getUsername(), record.getFullName(),
                record.getEmail(), record.getRole());
    }

    /**
     * @param user domain user
     * @return new JPA record
     */
    public static UserRecord toRecord(User user) {
        return new UserRecord(user.getUsername(), user.getFullName(), user.getEmail(), user.getRole());
    }

    /**
     * @param record JPA record
     * @return domain topic
     */
    public static Topic toDomain(TopicRecord record) {
        return new Topic(record.getId(), record.getName());
    }

    /**
     * @param topic domain topic
     * @return new JPA record
     */
    public static TopicRecord toRecord(Topic topic) {
        return new TopicRecord(topic.getName());
    }

    /**
     * @param record JPA record
     * @return domain competency
     */
    public static Competency toDomain(CompetencyRecord record) {
        return new Competency(record.getId(), record.getCode(), record.getName());
    }

    /**
     * @param competency domain competency
     * @return new JPA record
     */
    public static CompetencyRecord toRecord(Competency competency) {
        return new CompetencyRecord(competency.getCode(), competency.getName());
    }
}
