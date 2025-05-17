package tech.kitucode.banking.web.util;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

public class PaginationUtil {
    public static HttpHeaders generatePaginationHttpHeaders(Page page, String baseUrl) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("X-Total-Items", String.valueOf(page.getTotalElements()));

        String link = "";
        // not the last page - add uri for next page
        if ((page.getNumber() + 1) < page.getTotalPages()) {
            link = "<" + generateUri(baseUrl, page.getNumber() + 1, page.getSize()) + ">; rel=\"next\",";
        }

        // prev link
        if ((page.getNumber()) > 0) {
            link += "<" + generateUri(baseUrl, page.getNumber() + 1, page.getSize()) + ">; rel=\"prev\",";
        }

        // last and first link
        int lastPage = 0;
        if (page.getTotalPages() > 0) {
            lastPage = page.getTotalPages() - 1;
        }
        link += "<" + generateUri(baseUrl, lastPage, page.getSize()) + ">; rel=\"last\",";
        link += "<" + generateUri(baseUrl, 0, page.getSize()) + ">; rel=\"first\",";
        httpHeaders.add(HttpHeaders.LINK, link);
        return httpHeaders;
    }

    private static String generateUri(String baseUrl, int page, int size) {
        return UriComponentsBuilder.fromUriString(baseUrl).queryParam("page", page).queryParam("size", size).toUriString();
    }
}
