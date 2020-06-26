/*
 * Copyright (c) 2011-2017, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.http.request;

import com.jcabi.http.Wire;
import com.jcabi.immutable.ArrayMap;
import javax.json.Json;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

/**
 * Test case for {@link BaseRequest}.
 * @since 1.0
 */
public final class BaseRequestTest {

    /**
     * Expected exception.
     * @checkstyle VisibilityModifier (3 lines)
     */
    @Rule
    public final transient ExpectedException thrown = ExpectedException.none();

    /**
     * BaseRequest can build the right destination URI.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void buildsDestinationUri() throws Exception {
        final Wire wire = Mockito.mock(Wire.class);
        MatcherAssert.assertThat(
            new BaseRequest(wire, "http://localhost:88/t/f")
                .uri().path("/bar").queryParam("u1", "\u20ac")
                .queryParams(new ArrayMap<String, String>().with("u2", ""))
                .userInfo("hey:\u20ac")
                .back().uri().get(),
            Matchers.hasToString(
                "http://hey:%E2%82%AC@localhost:88/t/f/bar?u1=%E2%82%AC&u2="
            )
        );
    }

    /**
     * BaseRequest can set body to JSON.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void printsJsonInBody() throws Exception {
        final Wire wire = Mockito.mock(Wire.class);
        MatcherAssert.assertThat(
            new BaseRequest(wire, "http://localhost:88/x").body().set(
                Json.createObjectBuilder().add("foo", "test 1").build()
            ).get(),
            Matchers.equalTo("{\"foo\":\"test 1\"}")
        );
    }

    /**
     * BaseRequest can include the port number.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void includesPort() throws Exception {
        final Wire wire = Mockito.mock(Wire.class);
        MatcherAssert.assertThat(
            // @checkstyle MagicNumber (2 lines)
            new BaseRequest(wire, "http://localhost")
                .uri().port(8080).back().uri().get(),
            Matchers.hasToString("http://localhost:8080/")
        );
    }

    /**
     * FakeRequest can identify itself uniquely.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void identifiesUniquely() throws Exception {
        final Wire wire = Mockito.mock(Wire.class);
        MatcherAssert.assertThat(
            new BaseRequest(wire, "").header("header-1", "value-1"),
            Matchers.not(
                Matchers.equalTo(
                    new BaseRequest(wire, "").header("header-2", "value-2")
                )
            )
        );
        MatcherAssert.assertThat(
            new BaseRequest(wire, ""),
            Matchers.equalTo(new BaseRequest(wire, ""))
        );
    }

    /**
     * Throws exception when using formParam on multipartbody without
     * content-type defined.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void exceptionWhenMissingContentType() throws Exception {
        final Wire wire = Mockito.mock(Wire.class);
        this.thrown.expect(IllegalStateException.class);
        this.thrown.expectMessage(
            BaseRequestTest.boundaryErrorMesg()
        );
        new BaseRequest(wire, "")
            .multipartBody()
            .formParam("a", "value")
            .back();
    }

    /**
     * Throws exception when using formParam on multipartbody without
     * boundary provided in content-type defined.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void exceptionWhenMissingBoundary() throws Exception {
        final Wire wire = Mockito.mock(Wire.class);
        this.thrown.expect(IllegalStateException.class);
        this.thrown.expectMessage(
            BaseRequestTest.boundaryErrorMesg()
        );
        new BaseRequest(wire, "")
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.MULTIPART_FORM_DATA
            )
            .multipartBody()
            .formParam("b", "val")
            .back();
    }

    /**
     * Boundary error message.
     * @return Message error as String.
     */
    private static String boundaryErrorMesg() {
        return "Content-Type: multipart/form-data requires boundary";
    }
}
