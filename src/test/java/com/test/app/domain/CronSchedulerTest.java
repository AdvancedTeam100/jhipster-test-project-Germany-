package com.test.app.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.app.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CronSchedulerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CronScheduler.class);
        CronScheduler cronScheduler1 = new CronScheduler();
        cronScheduler1.setId("id1");
        CronScheduler cronScheduler2 = new CronScheduler();
        cronScheduler2.setId(cronScheduler1.getId());
        assertThat(cronScheduler1).isEqualTo(cronScheduler2);
        cronScheduler2.setId("id2");
        assertThat(cronScheduler1).isNotEqualTo(cronScheduler2);
        cronScheduler1.setId(null);
        assertThat(cronScheduler1).isNotEqualTo(cronScheduler2);
    }
}
