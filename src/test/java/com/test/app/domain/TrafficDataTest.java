package com.test.app.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TrafficDataTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TrafficData.class);
        TrafficData trafficData1 = new TrafficData();
        trafficData1.setId("id1");
        TrafficData trafficData2 = new TrafficData();
        trafficData2.setId(trafficData1.getId());
        assertThat(trafficData1).isEqualTo(trafficData2);
        trafficData2.setId("id2");
        assertThat(trafficData1).isNotEqualTo(trafficData2);
        trafficData1.setId(null);
        assertThat(trafficData1).isNotEqualTo(trafficData2);
    }
}
