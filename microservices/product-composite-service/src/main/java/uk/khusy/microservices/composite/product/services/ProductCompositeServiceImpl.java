package uk.khusy.microservices.composite.product.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import uk.khusy.api.composite.product.*;
import uk.khusy.api.core.product.Product;
import uk.khusy.api.core.recommendation.Recommendation;
import uk.khusy.api.core.review.Review;
import uk.khusy.api.exceptions.NotFoundException;
import uk.khusy.util.http.ServiceUtil;

import java.util.List;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    @Autowired
    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public ProductAggregate getProduct(int productId) {
        var product = integration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }
        var recommendations = integration.getRecommendations(productId);
        var reviews = integration.getReviews(productId);
        return createProductAggregate(product, recommendations, reviews, serviceUtil.getServiceAddress());
    }

    private ProductAggregate createProductAggregate(Product product, List<Recommendation> recommendations, List<Review> reviews, String serviceAddress) {
        // 1. Setup product info
        int productId = product.productId();
        String name = product.name();
        int weight = product.weight();

        // 2. Copy summary recommendation info, if available
        var recommendationSummaries =
                (recommendations == null) ? null : recommendations.stream()
                        .map(r -> new RecommendationSummary(r.recommendationId(), r.author(), r.rate()))
                        .toList();

        // 3. Copy summary review info, if available
        var reviewSummaries =
                (reviews == null) ? null : reviews.stream()
                        .map(r -> new ReviewSummary(r.reviewId(), r.author(), r.subject()))
                        .toList();

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.serviceAddress();
        String reviewAddress = (reviews != null && !reviews.isEmpty()) ? reviews.get(0).serviceAddress() : "";
        String recommendationAddress = (recommendations != null && !recommendations.isEmpty()) ? recommendations.get(0).serviceAddress() : "";
        var serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
