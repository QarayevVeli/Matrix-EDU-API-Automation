package az.matrix.api.http;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ApiLogFilter implements Filter {

    private static final Logger LOG = LogManager.getLogger(ApiLogFilter.class);

    @Override
    public Response filter(FilterableRequestSpecification request, FilterableResponseSpecification response, FilterContext context) {
        boolean secret = request.getURI().contains("/Auth/");
        String body = secret || request.getBody() == null ? "" : abbreviate(String.valueOf((Object) request.getBody()));
        String uri = mask(request.getURI());
        LOG.info("Sorğu: {} {} {}", request.getMethod(), uri, body);
        long started = System.currentTimeMillis();
        Response result = context.next(request, response);
        LOG.info("Cavab: {} {} ({} ms) {}", result.getStatusCode(), request.getMethod(), System.currentTimeMillis() - started,
                secret ? "" : abbreviate(result.asString()));
        return result;
    }

    private String mask(String uri) {
        return uri.replaceAll("(?i)(access_token|token)=[^&]+", "$1=***");
    }

    private String abbreviate(String text) {
        return text.length() > 200 ? text.substring(0, 200) + "..." : text;
    }
}
