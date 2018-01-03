package com.ming.shopping.beauty.client.controller;


import com.ming.shopping.beauty.client.ClientConfigTest;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author helloztt
 */
public class IndexControllerTest extends ClientConfigTest {

    @Test
    public void indexTest() throws Exception {
        String result = mockMvc.perform(get("/"))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        assertThat(result).isEqualTo("client index");
    }

}