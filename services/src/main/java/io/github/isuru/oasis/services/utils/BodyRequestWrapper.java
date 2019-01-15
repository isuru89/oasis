package io.github.isuru.oasis.services.utils;

import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BodyRequestWrapper extends HttpServletRequestWrapper {

    private byte[] payload;

    /**
     * Create a new CachingRequestWrapper for the given servlet request.
     *
     * @param request the original servlet request
     */
    public BodyRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public byte[] getPayload() throws IOException {
        if (payload == null) {
            payload = IOUtils.toByteArray(getRequest().getInputStream());
        }
        return payload;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(getPayload());
        return new ServletInputStream() {

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener listener) {

            }
        };
    }

    public byte[] getContentAsByteArray() throws IOException {
        return getPayload();
    }

}
