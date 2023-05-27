package com.test.app.domain;

import java.io.Serializable;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A CronScheduler.
 */
@Document(collection = "cron_scheduler")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CronScheduler implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull(message = "must not be null")
    @Field("name")
    private String name;

    @Field("cron_expression")
    private String cronExpression;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public CronScheduler id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public CronScheduler name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCronExpression() {
        return this.cronExpression;
    }

    public CronScheduler cronExpression(String cronExpression) {
        this.setCronExpression(cronExpression);
        return this;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CronScheduler)) {
            return false;
        }
        return id != null && id.equals(((CronScheduler) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CronScheduler{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", cronExpression='" + getCronExpression() + "'" +
            "}";
    }
}
