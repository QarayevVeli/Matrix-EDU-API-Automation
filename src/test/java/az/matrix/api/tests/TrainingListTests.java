package az.matrix.api.tests;

import az.matrix.api.base.BaseTest;
import az.matrix.api.data.Seed;
import az.matrix.api.http.Endpoints;
import az.matrix.api.http.Specs;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;

public class TrainingListTests extends BaseTest {

    private Seed seed;

    @BeforeClass
    public void prepareData() {
        seed = Seed.get();
    }

    private Response list(String... queryPairs) {
        var request = given().spec(Specs.authorized());
        for (int index = 0; index < queryPairs.length; index += 2) {
            request.queryParam(queryPairs[index], queryPairs[index + 1]);
        }
        return request.when().get(Endpoints.TRAININGS);
    }

    private String prefixFilter() {
        return "name=*" + seed.sortPrefix() + "/i";
    }

    @Test
    public void defaultListReturnsSuccessEnvelope() {
        list().then()
                .statusCode(200)
                .body("isSuccess", equalTo(true))
                .body("statusCode", equalTo(200))
                .body("error", nullValue());
    }

    @Test
    public void defaultListContainsPagingFields() {
        list().then()
                .statusCode(200)
                .body("data.totalCount", greaterThan(0))
                .body("data.pageNumber", equalTo(1))
                .body("data.pageSize", equalTo(10))
                .body("data.totalPages", greaterThan(0))
                .body("data.hasPreviousPage", equalTo(false))
                .body("data", hasKey("hasNextPage"));
    }

    @Test
    public void defaultPageContainsAtMostTenItems() {
        list().then()
                .statusCode(200)
                .body("data.items.size()", lessThanOrEqualTo(10));
    }

    @Test
    public void itemContainsIdNameAndDescription() {
        list().then()
                .statusCode(200)
                .body("data.items[0]", hasKey("id"))
                .body("data.items[0]", hasKey("name"))
                .body("data.items[0]", hasKey("description"));
    }

    @Test
    public void defaultOrderShowsNewestFirst() {
        List<Integer> ids = list("PageSize", "20").then().statusCode(200).extract().path("data.items.id");
        List<Integer> sorted = new ArrayList<>(ids);
        sorted.sort(Collections.reverseOrder());
        Assert.assertEquals(ids, sorted, "Defolt sıralama id üzrə azalan olmalıdır");
    }

    @Test
    public void listResponseIsJson() {
        list().then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @DataProvider(name = "pageSizes")
    public Object[][] pageSizes() {
        return new Object[][]{{1}, {2}, {5}};
    }

    @Test(dataProvider = "pageSizes")
    public void pageSizeLimitsTheItems(int size) {
        list("PageSize", String.valueOf(size)).then()
                .statusCode(200)
                .body("data.items.size()", equalTo(size))
                .body("data.pageSize", equalTo(size));
    }

    @Test
    public void secondPageReportsItsNumberAndPreviousPage() {
        list("Page", "2", "PageSize", "5").then()
                .statusCode(200)
                .body("data.pageNumber", equalTo(2))
                .body("data.hasPreviousPage", equalTo(true));
    }

    @Test
    public void pageBeyondTheLastReturnsEmptyItems() {
        list("Page", "9999", "PageSize", "10").then()
                .statusCode(200)
                .body("data.items", empty())
                .body("data.hasNextPage", equalTo(false));
    }

    @Test
    public void pageZeroIsTreatedAsTheFirstPage() {
        list("Page", "0", "PageSize", "2").then()
                .statusCode(200)
                .body("data.pageNumber", equalTo(1));
    }

    @Test
    public void negativePageIsTreatedAsTheFirstPage() {
        list("Page", "-1", "PageSize", "2").then()
                .statusCode(200)
                .body("data.pageNumber", equalTo(1));
    }

    @Test
    public void pageSizeZeroDoesNotFail() {
        list("PageSize", "0").then()
                .statusCode(200)
                .body("data.pageSize", greaterThanOrEqualTo(1));
    }

    @Test
    public void hugePageSizeIsCapped() {
        list("PageSize", "100000").then()
                .statusCode(200)
                .body("data.pageSize", lessThanOrEqualTo(100));
    }

    @DataProvider(name = "malformedPaging")
    public Object[][] malformedPaging() {
        return new Object[][]{{"Page", "abc"}, {"PageSize", "abc"}, {"Page", "1.5"}, {"Page", "2147483648"}};
    }

    @Test(dataProvider = "malformedPaging")
    public void malformedPagingParameterIsRejected(String name, String value) {
        ErrorAssert.rejected(list(name, value).then(), 400, ErrorAssert.INVALID_FORMAT);
    }

    @Test
    public void totalPagesMatchesTotalCount() {
        Response response = list("PageSize", "3");
        int total = response.path("data.totalCount");
        int pages = response.path("data.totalPages");
        Assert.assertEquals(pages, (int) Math.ceil(total / 3.0), "totalPages düzgün hesablanmayıb");
    }

    @Test
    public void lastPageHasNoNextPage() {
        int pages = list("PageSize", "3").path("data.totalPages");
        list("PageSize", "3", "Page", String.valueOf(pages)).then()
                .statusCode(200)
                .body("data.hasNextPage", equalTo(false));
    }

    @Test
    public void filterByExactNameReturnsOneRecord() {
        list("Filter", "name=" + seed.sortPrefix() + "-a").then()
                .statusCode(200)
                .body("data.totalCount", equalTo(1))
                .body("data.items[0].id", equalTo((int) seed.alphaId()));
    }

    @Test
    public void filterByNamePartFindsAllMatches() {
        list("Filter", prefixFilter()).then()
                .statusCode(200)
                .body("data.totalCount", equalTo(3));
    }

    @DataProvider(name = "prefixCases")
    public Object[][] prefixCases() {
        String prefix = Seed.get().sortPrefix();
        return new Object[][]{{prefix.toUpperCase()}, {prefix.toLowerCase()}};
    }

    @Test(dataProvider = "prefixCases")
    public void filterIgnoresLetterCase(String text) {
        list("Filter", "name=*" + text + "/i").then()
                .statusCode(200)
                .body("data.totalCount", equalTo(3));
    }

    @Test
    public void filterWithUpperCaseLetterIFindsTheRecord() {
        list("Filter", "name=*" + seed.salimName().toUpperCase() + "/i").then()
                .statusCode(200)
                .body("data.totalCount", equalTo(1));
    }

    @Test
    public void filterWithoutMatchReturnsEmptyResult() {
        list("Filter", "name=*zzzzqqqq" + seed.sortPrefix() + "/i").then()
                .statusCode(200)
                .body("data.totalCount", equalTo(0))
                .body("data.items", empty());
    }

    @Test
    public void filterByDescriptionFindsMatchingRecord() {
        list("Filter", prefixFilter() + ",description=*alpha/i").then()
                .statusCode(200)
                .body("data.totalCount", equalTo(1))
                .body("data.items[0].id", equalTo((int) seed.alphaId()));
    }

    @Test
    public void filterByIdReturnsThatRecord() {
        list("Filter", "id=" + seed.betaId()).then()
                .statusCode(200)
                .body("data.totalCount", equalTo(1))
                .body("data.items[0].name", equalTo(seed.sortPrefix() + "-B"));
    }

    @Test
    public void filterByIdGreaterThanReturnsOnlyNewerRecords() {
        List<Integer> ids = list("Filter", "id>" + seed.alphaId(), "PageSize", "50").then().statusCode(200).extract().path("data.items.id");
        Assert.assertFalse(ids.isEmpty(), "Nəticə boşdur");
        Assert.assertTrue(ids.stream().allMatch(id -> id > seed.alphaId()), "Bütün id-lər böyük olmalıdır: " + ids);
    }

    @Test
    public void invalidFilterExpressionIsRejectedWithClientError() {
        list("Filter", "#$garbage").then()
                .statusCode(400);
    }

    @DataProvider(name = "specialFilterTexts")
    public Object[][] specialFilterTexts() {
        return new Object[][]{{"<script>"}, {"a,b"}, {"a|b"}, {"(a)"}};
    }

    @Test(dataProvider = "specialFilterTexts")
    public void specialCharactersInFilterDoNotCauseServerError(String text) {
        list("Filter", "name=*" + text + "/i").then()
                .statusCode(200)
                .body("isSuccess", equalTo(true));
    }

    @Test
    public void orderByNameAscendingIgnoresLetterCase() {
        List<String> names = list("Filter", prefixFilter(), "OrderBy", "name").then().statusCode(200).extract().path("data.items.name");
        Assert.assertEquals(names, List.of(seed.sortPrefix() + "-a", seed.sortPrefix() + "-B", seed.sortPrefix() + "-c"));
    }

    @Test
    public void orderByNameDescendingIgnoresLetterCase() {
        List<String> names = list("Filter", prefixFilter(), "OrderBy", "name desc").then().statusCode(200).extract().path("data.items.name");
        Assert.assertEquals(names, List.of(seed.sortPrefix() + "-c", seed.sortPrefix() + "-B", seed.sortPrefix() + "-a"));
    }

    @Test
    public void orderByIdAscendingReturnsOldestFirst() {
        list("Filter", prefixFilter(), "OrderBy", "id").then()
                .statusCode(200)
                .body("data.items.id", contains((int) seed.alphaId(), (int) seed.betaId(), (int) seed.gammaId()));
    }

    @Test
    public void orderByIdDescendingReturnsNewestFirst() {
        list("Filter", prefixFilter(), "OrderBy", "id desc").then()
                .statusCode(200)
                .body("data.items.id", contains((int) seed.gammaId(), (int) seed.betaId(), (int) seed.alphaId()));
    }

    @Test
    public void invalidOrderByIsRejectedWithClientError() {
        list("OrderBy", "bogus").then()
                .statusCode(400);
    }

    @Test
    public void upperCaseOrderDirectionIsAccepted() {
        list("Filter", prefixFilter(), "OrderBy", "NAME DESC").then()
                .statusCode(200);
    }

    @Test
    public void pagingFilterAndOrderWorkTogether() {
        list("Filter", prefixFilter(), "OrderBy", "name", "Page", "2", "PageSize", "2").then()
                .statusCode(200)
                .body("data.items.size()", equalTo(1))
                .body("data.items[0].name", equalTo(seed.sortPrefix() + "-c"))
                .body("data.totalCount", equalTo(3));
    }
}
