package uk.khusy.microservices.core.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import uk.khusy.api.core.review.Review;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class ReviewServiceApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void getReviewsByProductId() {
        int productId = 1;

        ResponseEntity<List<Review>> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/review?productId=" + productId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().size()).isEqualTo(3);
        assertThat(responseEntity.getBody().get(0).productId()).isEqualTo(productId);
    }

    @Test
    public void getReviewsMissingParameter() {

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/review",
                HttpMethod.GET,
                null,
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).contains("Required parameter 'productId' is not present.");
    }

    @Test
    public void getReviewsInvalidParameter() {

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/review?productId=no-integer",
                HttpMethod.GET,
                null,
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(responseEntity.getBody()).contains("Failed to convert value of type");
    }

    @Test
    public void getReviewsNotFound() {

        int productIdNotFound = 213;

        ResponseEntity<List<Review>> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/review?productId=" + productIdNotFound,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().size()).isEqualTo(0);
    }

    @Test
    public void getReviewsInvalidParameterNegativeValue() {

        int productIdInvalid = -1;

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "http://localhost:" + port + "/review?productId=" + productIdInvalid,
                HttpMethod.GET,
                null,
                String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(responseEntity.getBody()).contains("Invalid productId: " + productIdInvalid);
    }
}
