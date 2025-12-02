package com.neviswealth.searchservice.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugUtilTest {

    @Test
    void slugifiesDomains() {
        assertThat(SlugUtil.slugify("Nevis-Wealth.com")).isEqualTo("neviswealth");
        assertThat(SlugUtil.slugify("  Example Domain! ")).isEqualTo("exampledomain");
        assertThat(SlugUtil.slugify(null)).isEqualTo("");
    }
}
