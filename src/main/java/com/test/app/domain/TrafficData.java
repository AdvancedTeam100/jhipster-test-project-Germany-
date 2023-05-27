package com.test.app.domain;

import java.io.Serializable;
import java.time.LocalDate;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * A TrafficData.
 */
@Document(collection = "traffic_data")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TrafficData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @Field("rank")
    private String rank;

    @Field("impressions")
    private Integer impressions;

    @NotNull(message = "must not be null")
    @Field("clicks")
    private Integer clicks;

    @Field("date")
    private LocalDate date;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public TrafficData id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRank() {
        return this.rank;
    }

    public TrafficData rank(String rank) {
        this.setRank(rank);
        return this;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public Integer getImpressions() {
        return this.impressions;
    }

    public TrafficData impressions(Integer impressions) {
        this.setImpressions(impressions);
        return this;
    }

    public void setImpressions(Integer impressions) {
        this.impressions = impressions;
    }

    public Integer getClicks() {
        return this.clicks;
    }

    public TrafficData clicks(Integer clicks) {
        this.setClicks(clicks);
        return this;
    }

    public void setClicks(Integer clicks) {
        this.clicks = clicks;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public TrafficData date(LocalDate date) {
        this.setDate(date);
        return this;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TrafficData)) {
            return false;
        }
        return id != null && id.equals(((TrafficData) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TrafficData{" +
            "id=" + getId() +
            ", rank='" + getRank() + "'" +
            ", impressions=" + getImpressions() +
            ", clicks=" + getClicks() +
            ", date='" + getDate() + "'" +
            "}";
    }
}
