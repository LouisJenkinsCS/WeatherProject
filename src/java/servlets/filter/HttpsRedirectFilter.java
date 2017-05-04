
package servlets.filter;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utilities.PropertyManager;

/**
 * This filter converts all HTTP requests to HTTPS requests.
 * @author cmr98507
 * @author Joseph Picataggio
 */
public class HttpsRedirectFilter implements Filter {

    private FilterConfig filterConfig = null;
    /**
     * Handles HTTPS redirection
     */
    public HttpsRedirectFilter() {
    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String getProtocol = req.getScheme();
        String portNumber = Integer.toString(req.getServerPort());
        StringBuffer url = req.getRequestURL();

        if (getProtocol.toLowerCase().equals("http")) {

            // Set response content type
            response.setContentType("text/html");

            url.replace(0, 4, "https");
            int portStartIndex = url.indexOf(portNumber);
            url.replace(portStartIndex, portStartIndex+4,PropertyManager.getProperty("HTTPSPort"));
            String site = url.toString();
            res.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            res.setHeader("Location", site);
        }

        // Pass request back down the filter chain
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
    }

    @Override
    public void destroy() {
        this.filterConfig = null;
    }
}
