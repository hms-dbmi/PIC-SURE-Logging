package edu.harvard.dbmi.avillach.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestSizeLimitFilterTest {

    private RequestSizeLimitFilter filter;
    private MockHttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RequestSizeLimitFilter(RequestSizeLimitFilter.MAX_REQUEST_BYTES);
        response = new MockHttpServletResponse();
        chain = mock(FilterChain.class);
    }

    @Test
    void bodyUnderTheCapPassesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/audit");
        request.setContent("{\"event_type\":\"TEST\"}".getBytes(StandardCharsets.UTF_8));

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void bodyAtExactlyTheCapPassesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/audit");
        request.setContent(new byte[(int) RequestSizeLimitFilter.MAX_REQUEST_BYTES]);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void declaredContentLengthOverTheCapReturns413WithoutReadingTheBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/audit");
        request.setContent(new byte[(int) RequestSizeLimitFilter.MAX_REQUEST_BYTES + 1]);

        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(any(), any());
        assertThat(response.getStatus()).isEqualTo(413);
        assertThat(response.getContentType()).startsWith("application/json");
    }

    @Test
    void chunkedBodyOverTheCapIsRejectedWhileBeingRead() throws Exception {
        // Content-Length -1 (chunked): the declared-length check cannot help.
        byte[] oversized = new byte[(int) RequestSizeLimitFilter.MAX_REQUEST_BYTES + 1024];
        HttpServletRequest request = chunkedRequest(oversized);

        // The wrapper only trips when the body is actually consumed, so the chain reads it.
        doAnswer((Answer<Void>) invocation -> {
            HttpServletRequest wrapped = invocation.getArgument(0);
            wrapped.getInputStream().readAllBytes();
            return null;
        }).when(chain).doFilter(any(), any());

        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
            .isInstanceOf(IOException.class)
            .hasMessageContaining("exceeds");
    }

    private HttpServletRequest chunkedRequest(byte[] body) throws IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getContentLengthLong()).thenReturn(-1L);
        when(request.getInputStream()).thenReturn(servletInputStream(new ByteArrayInputStream(body)));
        return request;
    }

    private ServletInputStream servletInputStream(InputStream delegate) {
        return new ServletInputStream() {
            @Override public int read() throws IOException { return delegate.read(); }
            @Override public boolean isFinished() { return false; }
            @Override public boolean isReady() { return true; }
            @Override public void setReadListener(ReadListener readListener) { }
        };
    }
}
