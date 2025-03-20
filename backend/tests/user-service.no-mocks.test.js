/**
 * @file user-service.no-mocks.test.js
 * @desc Tests for User Service API without mocks
 */

const request = require('supertest');

const API_BASE_URL = "http://3.138.121.192:8080/users"; // Your actual deployed API URL

// Sample user data for testing
const testUser = {
    google_id: "103217936482731253672",
    email: "user@example.com",
    username: "john_doe",
    preferences: ["electronics", "furniture"],
    latitude: 49.2827,
    longitude: -123.1207
};

let createdUserId = testUser.google_id;

describe("📝 User Service API Tests (No Mocks)", () => {

    /** 🟢 TEST 1: Register a New User */
    test("POST /users/register → Should register a new user", async () => {
        try {
            const response = await request(API_BASE_URL)
                .post("/register")
                .send(testUser);

            console.log("POST /users/register Response:", response.body); // Debugging

            expect(response.status).toBe(201);
            expect(response.body).toMatchObject({
                message: "User registered successfully",
                user_id: testUser.google_id
            });

            createdUserId = response.body.user_id; // Store user ID for further tests
        } catch (error) {
            console.error("POST /users/register error:", error);
            throw error;
        }
    });

    /** 🟢 TEST 2: Retrieve User Profile */
    test("GET /users/:id → Should retrieve the user profile", async () => {
        try {
            const response = await request(API_BASE_URL)
                .get(`/${createdUserId}`);

            console.log("GET /users Response:", response.body); // Debugging

            expect(response.status).toBe(200);
            expect(response.body.google_id).toBe(testUser.google_id);
            expect(response.body.email).toBe(testUser.email);
            expect(response.body.username).toBe(testUser.username);
            expect(response.body.preferences).toEqual(testUser.preferences);
            expect(response.body.latitude).toBeCloseTo(testUser.latitude, 4); // Match 4 decimal places
            expect(response.body.longitude).toBeCloseTo(testUser.longitude, 4);
        } catch (error) {
            console.error("GET /users/:id error:", error);
            throw error;
        }
    });

    /** 🟢 TEST 3: Update User Details */
    test("PUT /users/:id → Should update user preferences and location", async () => {
        const updatedUserData = {
            email: "newuser@example.com",
            username: "johndoe_new",
            preferences: ["books", "gaming"],
            latitude: 49.2900,
            longitude: -123.1000
        };

        try {
            const response = await request(API_BASE_URL)
                .put(`/${createdUserId}`)
                .send(updatedUserData);

            console.log("PUT /users Response:", response.body); // Debugging

            expect(response.status).toBe(200);
            expect(response.body).toMatchObject({ message: "User updated successfully" });

            // Verify update
            const checkResponse = await request(API_BASE_URL).get(`/${createdUserId}`);
            expect(checkResponse.body.email).toBe(updatedUserData.email);
            expect(checkResponse.body.username).toBe(updatedUserData.username);
            expect(checkResponse.body.preferences).toEqual(updatedUserData.preferences);
        } catch (error) {
            console.error("PUT /users/:id error:", error);
            throw error;
        }
    });

    /** 🟢 TEST 4: Delete User */
    test("DELETE /users/:id → Should delete the user", async () => {
        const response = await request(API_BASE_URL)
            .delete(`/${createdUserId}`);

        console.log("DELETE /users Response:", response.body); // Debugging

        expect(response.status).toBe(200);
        expect(response.body).toMatchObject({ message: "User deleted successfully" });

        // Verify user no longer exists
        const checkResponse = await request(API_BASE_URL).get(`/${createdUserId}`);
        expect(checkResponse.status).toBe(404); // Assuming API returns 404 for deleted users
    });

});
