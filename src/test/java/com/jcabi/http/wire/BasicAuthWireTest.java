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
package com.jcabi.http.wire;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.DatatypeConverter;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test case for {@link BasicAuthWire}.
 *
 * @since 1.17.1
 */
@RunWith(Parameterized.class)
public final class BasicAuthWireTest {

    /**
     * The format of the credentials as <code>username:password</code>.
     */
    private static final String CRED_FORMAT = "%s:%s";

    /**
     * The encoding to use.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * The charset to use.
     */
    private static final Charset CHARSET = Charset.forName(
        BasicAuthWireTest.ENCODING
    );

    /**
     * The username to use for authentication.
     */
    private final transient String username;

    /**
     * The password to use for authentication.
     */
    private final transient String password;

    /**
     * Creates a new test instance for the given username and password
     * combination.
     *
     * @param username The username to user for authentication
     * @param password The password to user for authentication
     */
    public BasicAuthWireTest(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Test parameters consisting of username and password pairs.
     *
     * @return The username and password parameters used to construct
     *  the test
     */
    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        final Collection<Object[]> parameters = new ArrayList<>(10);
        parameters.add(new String[] {"Alice", "secret"});
        parameters.add(new String[] {"Bob", "s&e+c`ret"});
        parameters.add(new String[] {"user", "\u20ac\u20ac"});
        return parameters;
    }

    /**
     * Tests if the wire generates the authorization header correctly.
     *
     * @throws Exception If something goes wrong
     */
    @Test
    public void testHeader() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final URI uri = UriBuilder.fromUri(container.home()).userInfo(
            String.format(
                BasicAuthWireTest.CRED_FORMAT,
                URLEncoder.encode(this.username, BasicAuthWireTest.ENCODING),
                URLEncoder.encode(this.password, BasicAuthWireTest.ENCODING)
            )
        ).build();
        final String expected = BasicAuthWireTest.expectHeader(
            this.username,
            this.password
        );
        new JdkRequest(uri)
            .through(BasicAuthWire.class)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        MatcherAssert.assertThat(
            container.take().headers().get(HttpHeaders.AUTHORIZATION).get(0),
            Matchers.equalTo(expected)
        );
    }

    /**
     * Creates the expected authorization header value for the
     * given username.
     *
     * @param username The username to create the header for
     * @param password The password to create the header for
     * @return The header value in the form
     *  <code>Basic &lt;base64 of username:password&gt;</code>
     */
    private static String expectHeader(final String username,
        final String password) {
        final String credentials = DatatypeConverter.printBase64Binary(
            String.format(
                BasicAuthWireTest.CRED_FORMAT,
                username,
                password
            ).getBytes(BasicAuthWireTest.CHARSET)
        );
        return String.format("Basic %s", credentials);
    }
}
