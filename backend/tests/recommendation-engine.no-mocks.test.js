/**
 * @file recommendation-engine.no-mocks.test.js
 * @desc Tests for Recommendation Engine API without mocks
 */

const request = require('supertest');

const API_BASE_URL = "http://3.138.121.192:5001"; // Your actual deployed API URL

describe("🛒 Recommendation Engine API Tests (No Mocks)", () => {

    /** 🟢 TEST 1: Fetch Recommendations for a User */
    test("GET /recommendations/:userId → Should return recommended items", async () => {
        const userId = "22222222"; // Test user ID
        try {
            const response = await request(API_BASE_URL)
                .get(`/recommendations/${userId}`);

            console.log("GET /recommendations Response:", response.body); // Debugging

            expect(response.status).toBe(200);
            expect(response.body).toHaveProperty("userId", userId);
            expect(response.body).toHaveProperty("recommended_items");
            expect(Array.isArray(response.body.recommended_items)).toBe(true);
        } catch (error) {
            console.error("GET /recommendations/:userId error:", error);
            throw error;
        }
    });

    /** 🟢 TEST 2: Fetch Price Comparison for an Item */
    test("GET /price-comparison/:itemId → Should return price comparison", async () => {
        const itemId = "22"; // Test item ID
        try {
            const response = await request(API_BASE_URL)
                .get(`/price-comparison/${itemId}`);

            console.log("GET /price-comparison Response:", response.body); // Debugging

            expect(response.status).toBe(200);
            expect(response.body).toHaveProperty("original_item");
            expect(response.body.original_item).toHaveProperty("name");
            expect(response.body.original_item).toHaveProperty("price");
            expect(response.body).toHaveProperty("similar_items");
            expect(Array.isArray(response.body.similar_items)).toBe(true);
        } catch (error) {
            console.error("GET /price-comparison/:itemId error:", error);
            throw error;
        }
    });

    /** 🟢 TEST 3: Fetch Price Suggestions */
    test("POST /price-suggestions → Should return suggested price", async () => {
        const requestBody = { keyword: "bike" };

        try {
            const response = await request(API_BASE_URL)
                .post(`/price-suggestions`)
                .send(requestBody);

            console.log("POST /price-suggestions Response:", response.body); // Debugging

            expect(response.status).toBe(200);
            expect(response.body).toHaveProperty("best_price");
            expect(response.body).toHaveProperty("similar_items");
            expect(Array.isArray(response.body.similar_items)).toBe(true);
        } catch (error) {
            console.error("POST /price-suggestions error:", error);
            throw error;
        }
    });

});
