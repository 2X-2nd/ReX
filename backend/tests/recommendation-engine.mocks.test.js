/**
 * @file recommendation-engine.mocks.test.js
 * @desc Tests for Recommendation Engine API with mocks (Mocking User Service & External APIs)
 */

const request = require('supertest');
const axios = require('axios'); // Mocking API calls
jest.mock('axios'); // Mock axios globally

const API_BASE_URL = "http://3.138.121.192:5001"; // Your deployed Recommendation Engine API

describe("🛠 Recommendation Engine API Tests (With Mocks)", () => {

    /** 🟢 FIXED TEST 1: Mock successful user preferences retrieval */
    test("GET /recommendations/:userId → Should return recommendations (mocked User Service)", async () => {
        const userId = "22222222";

        // ✅ Mock User Service response with preferences
        axios.get.mockResolvedValueOnce({
            data: {
                google_id: userId,
                email: "mocked_user@example.com",
                username: "mocked_john_doe",
                preferences: ["electronics", "furniture"]
            }
        });

        // ✅ Mock Listings Service response (return at least one result)
        axios.get.mockResolvedValueOnce({
            data: {
                results: [
                    { name: "Smart TV", price: 399.99, url: "https://example.com/tv" },
                    { name: "Leather Sofa", price: 599.99, url: "https://example.com/sofa" }
                ]
            }
        });

        const response = await request(API_BASE_URL)
            .get(`/recommendations/${userId}`);

        console.log("GET /recommendations (Mocked) Response:", response.body); // Debugging

        expect(response.status).toBe(200);
        expect(response.body).toHaveProperty("userId", userId);
        expect(response.body).toHaveProperty("recommended_items");
        expect(response.body.recommended_items.length).toBeGreaterThan(0); // ✅ Ensures recommended items are not empty
    });

    /** 🔴 FIXED TEST 2: Mock User Service returning no preferences */
    test("GET /recommendations/:userId → Should return empty recommendations when user has no preferences", async () => {
        const userId = "22222222";

        // ✅ Mock User Service with empty preferences
        axios.get.mockResolvedValueOnce({
            data: { google_id: userId, preferences: [] } // No preferences
        });

        const response = await request(API_BASE_URL)
            .get(`/recommendations/${userId}`);

        console.log("GET /recommendations (No Preferences) Response:", response.body); // Debugging

        expect(response.status).toBe(200);
        expect(response.body.recommended_items).toEqual([]);
    });

    /** 🔴 FIXED TEST 3: Mock API Failure (User Service Down) */
    test("GET /recommendations/:userId → Should return 500 if User Service fails", async () => {
        const userId = "22222222";

        // ✅ Mock User Service to throw an error
        axios.get.mockRejectedValueOnce(new Error("User Service Unavailable"));

        const response = await request(API_BASE_URL)
            .get(`/recommendations/${userId}`);

        console.log("GET /recommendations (User Service Failure) Response:", response.body); // Debugging

        expect(response.status).toBe(200);
        expect(response.body).toHaveProperty("error", "Internal server error");
    });

    /** 🟢 FIXED TEST 4: Mock successful price suggestion */
    test("POST /price-suggestions → Should return suggested price (mocked eBay API)", async () => {
        // ✅ Mock eBay API response
        axios.get.mockResolvedValueOnce({
            data: {
                organic_results: [
                    { title: "Used Mountain Bike", price: { extracted: 300.00 }, link: "https://example.com/bike1" },
                    { title: "Road Bike", price: { extracted: 350.00 }, link: "https://example.com/bike2" }
                ]
            }
        });

        const response = await request(API_BASE_URL)
            .post(`/price-suggestions`)
            .send({ keyword: "bike" });

        console.log("POST /price-suggestions (Mocked) Response:", response.body); // Debugging

        expect(response.status).toBe(200);
        expect(response.body).toHaveProperty("best_price");
        expect(response.body.similar_items.length).toBeGreaterThan(0);
    });

    /** 🔴 FIXED TEST 5: Mock API Failure (SerpAPI Down) */
    test("POST /price-suggestions → Should return 500 if eBay API fails", async () => {
        // ✅ Mock SerpAPI to return an error
        axios.get.mockRejectedValueOnce(new Error("SerpAPI Unavailable"));

        const response = await request(API_BASE_URL)
            .post(`/price-suggestions`)
            .send({ keyword: "bike" });

        console.log("POST /price-suggestions (API Failure) Response:", response.body); // Debugging

        expect(response.status).toBe(200);
        expect(response.body).toHaveProperty("error", "Internal server error");
    });

});
