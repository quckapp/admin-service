package com.quckapp.admin.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class EnvironmentChainTest {

    @Test
    void previousOf_dev_returns_local() {
        assertThat(EnvironmentChain.previousOf("dev")).hasValue("local");
    }

    @Test
    void previousOf_local_returns_empty() {
        assertThat(EnvironmentChain.previousOf("local")).isEmpty();
    }

    @Test
    void previousOf_qa_returns_dev() {
        assertThat(EnvironmentChain.previousOf("qa")).hasValue("dev");
    }

    @Test
    void previousOf_production_returns_staging() {
        assertThat(EnvironmentChain.previousOf("production")).hasValue("staging");
    }

    @Test
    void previousOf_live_returns_production() {
        assertThat(EnvironmentChain.previousOf("live")).hasValue("production");
    }

    @ParameterizedTest
    @CsvSource({"uat1,qa", "uat2,qa", "uat3,qa"})
    void previousOf_uatVariants_returns_qa(String uat, String expected) {
        assertThat(EnvironmentChain.previousOf(uat)).hasValue(expected);
    }

    @Test
    void previousOf_staging_returns_uat() {
        assertThat(EnvironmentChain.previousOf("staging")).hasValue("uat");
    }

    @Test
    void isUnrestricted_local_returns_true() {
        assertThat(EnvironmentChain.isUnrestricted("local")).isTrue();
    }

    @Test
    void isUnrestricted_dev_returns_false() {
        assertThat(EnvironmentChain.isUnrestricted("dev")).isFalse();
    }

    @Test
    void normalizeEnvironment_uat1_returns_uat() {
        assertThat(EnvironmentChain.normalize("uat1")).isEqualTo("uat");
    }

    @Test
    void normalizeEnvironment_production_returns_production() {
        assertThat(EnvironmentChain.normalize("production")).isEqualTo("production");
    }

    @ParameterizedTest
    @CsvSource({"dev,qa,true", "qa,dev,false", "dev,staging,false", "staging,live,false", "local,dev,true"})
    void isValidPromotion(String from, String to, boolean expected) {
        assertThat(EnvironmentChain.isValidPromotion(from, to)).isEqualTo(expected);
    }

    @Test
    void nextOf_staging_returns_production() {
        assertThat(EnvironmentChain.nextOf("staging")).hasValue("production");
    }

    @Test
    void nextOf_live_returns_empty() {
        assertThat(EnvironmentChain.nextOf("live")).isEmpty();
    }

    @Test
    void previousOf_unknown_throws() {
        assertThatThrownBy(() -> EnvironmentChain.previousOf("unknown"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
