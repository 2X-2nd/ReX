/**
 * @file user-service.mocks.test.js
 * @desc Tests for User Service API with mocks (Database mocking)
 */

const request = require('supertest');
const db = require('../db'); // Mock the database module

jest.mock('../db'); // Mock the database execution functions

const API_BASE_URL = "http://3.138.121.192:8080/users"; // Your actual deployed API URL

describe("🛠 User Service API Tests (With Mocks)", () => {

    /** 🟢 TEST 1: Mock successful user registration */
    test("POST /users/register → Should register a new user (mocked DB)", async () => {
        db.execute.mockResolvedValue([{ affectedRows: 1 }]); // Simulate DB insert success

        const testUser = {
            google_id: "103217936482731253672",
            email: "mocked_user@example.com",
            username: "mocked_john_doe",
            preferences: ["electronics", "furniture"],
            latitude: 49.2827,
            longitude: -123.1207
        };

        const response = await request(API_BASE_URL)
            .post("/register")
            .send(testUser);

        console.log("POST /users/register (Mocked) Response:", response.body); // Debugging

        expect(response.status).toBe(201);
        expect(response.body).toMatchObject({
            message: "User registered successfully",
            user_id: testUser.google_id
        });
    });

    /** 🔴 TEST 2: Mock database failure for user registration */
    test("POST /users/register → Should return 500 if DB insert fails", async () => {
        db.execute.mockRejectedValue(new Error("DB Insert Error")); // Simulate DB failure

        const testUser = {
            google_id: "103217936482731253673",
            email: "error_user@example.com",
            username: "error_john_doe",
            preferences: ["fashion"],
            latitude: 48.8566,
            longitude: 2.3522
        };

        const response = await request(API_BASE_URL)
            .post("/register")
            .send(testUser);

        console.log("POST /users/register (DB Error) Response:", response.body); // Debugging

        expect(response.status).toBe(500);
        expect(response.body).toHaveProperty("error", "DB Insert Error");
    });

    /** 🟢 TEST 3: Mock successful retrieval of a user */
    test("GET /users/:id → Should return user profile (mocked DB)", async () => {
        const mockedUser = {
            google_id: "103217936482731253672",
            email: "mocked_user@example.com",
            username: "mocked_john_doe",
            preferences: JSON.stringify(["electronics", "furniture"]),
            latitude: 49.2827,
            longitude: -123.1207
        };

        db.execute.mockResolvedValue([[mockedUser]]); // Simulate DB returning user data

        const response = await request(API_BASE_URL)
            .get(`/103217936482731253672`);

        console.log("GET /users/:id (Mocked) Response:", response.body); // Debugging

        expect(response.status).toBe(200);
        expect(response.body.google_id).toBe(mockedUser.google_id);
        expect(response.body.email).toBe(mockedUser.email);
        expect(response.body.username).toBe(mockedUser.username);
        expect(response.body.preferences).toEqual(["electronics", "furniture"]); // Ensure correct parsing
    });

    /** 🔴 TEST 4: Mock user not found scenario */
    test("GET /users/:id → Should return 404 if user does not exist (mocked DB)", async () => {
        db.execute.mockResolvedValue([[]]); // Simulate no user found

        const response = await request(API_BASE_URL)
            .get(`/999999999999`);

        console.log("GET /users/:id (User Not Found) Response:", response.body); // Debugging

        expect(response.status).toBe(404);
        expect(response.body).toHaveProperty("error", "User not found");
    });

    /** 🔴 TEST 5: Mock database failure for GET /users/:id */
    test("GET /users/:id → Should return 500 if DB query fails", async () => {
        db.execute.mockRejectedValue(new Error("DB Query Error")); // Simulate DB failure

        const response = await request(API_BASE_URL)
            .get(`/103217936482731253672`);

        console.log("GET /users/:id (DB Error) Response:", response.body); // Debugging

        expect(response.status).toBe(500);
        expect(response.body).toHaveProperty("error", "DB Query Error");
    });

});
