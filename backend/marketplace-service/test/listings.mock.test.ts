import request from 'supertest';
import app from '../routes/listings';
import mysql from 'mysql2';

jest.mock("mysql2", () => {
    const mockDb = {
        query: jest.fn(),
        getConnection: jest.fn((callback) => callback(null, { release: jest.fn() })),
    };

    return {
        createPool: jest.fn(() => mockDb),
        __mockDb: mockDb, // Expose it for testing
    };
});

// ✅ Access `mockDb` through `mysql2.__mockDb`
const mockDb = (mysql as any).__mockDb;

beforeEach(() => {
    jest.clearAllMocks();
    (mysql.createPool as jest.Mock).mockReturnValue(mockDb);
});
const db = mysql.createPool({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME,
    waitForConnections: true,
    connectionLimit: 10,
    queueLimit: 0
});

/**
 * Interface: POST /listings
 */
describe("Mocked: POST /listings", () => {
    // Mocked behavior: database query fails
    // Input: Missing required fields
    // Expected status code: 400
    // Expected output: Error message
    test("Missing required fields", async () => {
        const res = await request(app).post("/listings").send({});
        expect(res.status).toBe(400);
        expect(res.body).toEqual({ error: "Missing required fields" });
    });

    // Mocked behavior: database query throws an error
    // Input: Valid listing data
    // Expected status code: 500
    // Expected behavior: Error handled gracefully
    test("Database error when inserting listing", async () => {
        mockDb.query.mockImplementationOnce((sql: any, values: any, callback: (arg0: Error, arg1: null) => void) => {
            callback(new Error("Forced error"), null);
        });
        const res = await request(app).post("/listings").send({
            title: "Test Listing",
            description: "A great item",
            price: 100,
            seller_id: 1,
            images: ["img1.jpg"],
            latitude: 49.2827,
            longitude: -123.1207,
            category: "Electronics"
        });
        expect(res.status).toBe(500);
        expect(res.body).toEqual({ error: "Database error" });
    });
});

/**
 * Interface: GET /listings
 */
describe("Mocked: GET /listings", () => {
    /**
     * Mocked behavior: Simulate a database query failure.
     * Input: id=1
     * Expected status code: 500
     * Expected behavior: Returns an error message about the database error.
     */
    test("Retrieve listing by ID", async () => {
        mockDb.query.mockImplementation((sql: string | string[], values: any, callback: (arg0: null, arg1: { id: number; title: string; description: string; }[] | { image_url: string; }[]) => void) => {
            if (sql.includes("FROM listings WHERE id")) {
                callback(null, [{ id: 1, title: "Test Listing", description: "A great item" }]);
            } else if (sql.includes("FROM listing_images")) {
                callback(null, [{ image_url: "img1.jpg" }]);
            }
        });

        const res = await request(app).get("/listings").query({ id: 1 });
        expect(res.status).toBe(200);
        expect(res.body).toEqual({
            id: 1,
            title: "Test Listing",
            description: "A great item",
            images: ["img1.jpg"]
        });
    });

    /**
     * Mocked behavior: Simulate a database query failure.
     * Input: id=1
     * Expected status code: 500
     * Expected behavior: Returns an error message about the database error.
     */
    test("Database error when retrieving listing", async () => {
        mockDb.query.mockImplementation((sql: any, values: any, callback: (arg0: Error) => any) => callback(new Error("DB Error")));
        const res = await request(app).get("/listings").query({ id: 1 });
        expect(res.status).toBe(500);
        expect(res.body).toEqual({ error: "Database error" });
    });

    /**
     * Mocked behavior: No listing found in database.
     * Input: id=999
     * Expected status code: 404
     * Expected behavior: Returns an error message about the listing not being found.
     */
    test("Listing not found", async () => {
        mockDb.query.mockImplementationOnce((sql: any, values: any, callback: (arg0: null, arg1: never[]) => void) => {
            callback(null, []);
        });

        const res = await request(app).get("/listings").query({ id: 99 });
        expect(res.status).toBe(404);
        expect(res.body).toEqual({ error: "Listing not found" });
    });

    // Mocked behavior: database connection failure
    // Input: Valid listing ID
    // Expected status code: 500
    // Expected behavior: Connection error handled gracefully
    test("Database connection failure when retrieving listing", async () => {
        mockDb.getConnection.mockImplementationOnce((callback: (arg0: Error) => any) => callback(new Error("Connection failed")));
        const res = await request(app).get("/listings").query({ id: 1 });
        expect(res.status).toBe(500);
        expect(res.body).toEqual({ error: "Database error" });
    });
});

/**
 * Mocked: PUT /listings/:id
 */
describe("Mocked: PUT /listings/:id", () => {
    // Mocked behavior: Listing not found
    // Input: Invalid listing ID
    // Expected status code: 404
    // Expected behavior: Returns an error message about the listing not being found.
    test("Listing not found", async () => {
        mockDb.query.mockImplementationOnce((sql: any, values: any, callback: (arg0: null, arg1: never[]) => void) => {
            callback(null, []);
        });
        const res = await request(app).put("/listings/999").send({ title: "Updated Title" });
        expect(res.status).toBe(404);
        expect(res.body).toEqual({ error: "Listing not found" });
    });

    // Mocked behavior: database connection failure
    // Input: Valid listing info
    // Expected status code: 500
    // Expected behavior: Connection error handled gracefully
    test("Database error when updating listing", async () => {
        mockDb.query.mockImplementationOnce((sql: any, values: any, callback: (arg0: null, arg1: { id: number; }[]) => void) => {
            callback(null, [{ id: 1 }]); // Listing exists
        });
        mockDb.query.mockImplementationOnce((sql: any, values: any, callback: (arg0: Error) => void) => {
            callback(new Error("DB Error"));
        });
        const res = await request(app).put("/listings/1").send({
            title: "Test Listing",
            description: "A great item",
            price: 100,
            seller_id: 1,
            images: ["img1.jpg"],
            latitude: 49.2827,
            longitude: -123.1207,
            category: "Electronics"
        });
        expect(res.status).toBe(500);
        expect(res.body).toEqual({ error: "Database error" });
    });

    /**
     * Mocked behavior: Simulate a database query failure.
     * Input: id=1
     * Expected status code: 500
     * Expected behavior: Returns an error message about the database error.
     */
    test("Database error when updating listing", async () => {
        mockDb.query.mockImplementationOnce((sql: any, values: any, callback: (arg0: Error, arg1: null) => void) => {
            callback(new Error("DB Error"), null);
        });
        const res = await request(app).put("/listings/1");
        expect(res.status).toBe(500);
        expect(res.body).toEqual({ error: "Database error" });
    });
});

/**
 * Interface: DELETE /listings
 */
describe("Mocked: DELETE /listings", () => {
    /**
     * Mocked behavior: Simulate a database query failure.
     * Input: id=1
     * Expected status code: 500
     * Expected behavior: Returns an error message about the database error.
     */
    test("Database error when finding listing", async () => {
        mockDb.query.mockImplementationOnce((sql: any, values: any, callback: (arg0: Error, arg1: null) => void) => {
            callback(new Error("DB Error"), null);
        });
        const res = await request(app).delete("/listings/1");
        expect(res.status).toBe(500);
        expect(res.body).toEqual({ error: "Database error" });
    });

    /**
     * Mocked behavior: No listing found in database when deleting.
     * Input: id=999
     * Expected status code: 404
     * Expected behavior: Returns an error message about the listing not being found.
     */
    test("Listing not found when attempting to delete", async () => {
        mockDb.query.mockImplementationOnce((sql: any, values: any, callback: (arg0: null, arg1: { id: number; }[]) => void) => {
            callback(null, []); // Listing not exists
        });
        const res = await request(app).delete("/listings/999");
        expect(res.status).toBe(404);
        expect(res.body).toEqual({ error: "Listing not found" });
    });

    /**
     * Mocked behavior: Simulate a database query failure when deleting
     * Input: id=1
     * Expected status code: 500
     * Expected behavior: Returns an error message about the database error.
     */
    test("Database error when deleting listing", async () => {
        mockDb.query.mockImplementationOnce((sql: any, values: any, callback: (arg0: null, arg1: { id: number; }[]) => void) => {
            callback(null, [{id : 1}]); // Listing exists
        });
        mockDb.query.mockImplementationOnce((sql: any, values: any, callback: (arg0: Error, arg1: { id: number; }[]) => void) => {
            callback(new Error("DB Error"), []);
        });
        const res = await request(app).delete("/listings/1");
        expect(res.status).toBe(500);
        expect(res.body).toEqual({ error: "Database error" });
    });
});

/**
 * Mocked: GET /listings/search
 */
describe("Mocked: GET /listings/search", () => {
    // Mocked behavior: No query parameter is provided
    // Input: no query parameter
    // Expected status code: 400
    // Expected behavior: Return error message "Query parameter is required"
    test("Missing query parameter", async () => {
        const res = await request(app).get("/listings/search");
        expect(res.status).toBe(400);
        expect(res.body).toEqual({ error: "Query parameter is required" });
    });

    // Mocked behavior: The database query fails
    // Input: test
    // Expected status code: 500
    // Expected behavior: Return error message "Database error"
    test("Database error when searching listings", async () => {
        mockDb.query.mockImplementation((sql: any, values: any, callback: (arg0: Error) => any) => callback(new Error("DB Error")));
        const res = await request(app).get("/listings/search").query({ query: "test" });
        expect(res.status).toBe(500);
        expect(res.body).toEqual({ error: "Database error" });
    });

    // Mocked behavior: Database returns a successful result
    // Input: test
    // Expected status code: 200
    // Expected behavior: Return search results with images converted to an array
    test("Successful listing search", async () => {
        mockDb.query.mockImplementation((sql: any, values: any, callback: (arg0: null, arg1: { id: number; title: string; images: string; }[]) => void) => {
            callback(null, [{ id: 1, title: "Test Listing", images: "img1.jpg,img2.jpg" }]);
        });
        const res = await request(app).get("/listings/search").query({ query: "test" });
        expect(res.status).toBe(200);
        expect(res.body.results[0].images).toEqual(["img1.jpg", "img2.jpg"]);
    });
});

/**
 * Interface: GET /listings/category
 */
describe("Mocked: GET /listings/category", () => {
    test("Missing query parameter", async () => {
        // Mocked behavior: No query parameter is provided
        // Input: no input
        // Expected status code: 400
        // Expected behavior: Return error message "Query parameter is required"
        const res = await request(app).get("/listings/category");
        expect(res.status).toBe(400);
        expect(res.body).toEqual({ error: "Query parameter is required" });
    });

    test("Database error when searching listings by category", async () => {
        // Mocked behavior: The database query fails
        // Input: query=electronics
        // Expected status code: 500
        // Expected behavior: Return error message "Database error"
        mockDb.query.mockImplementation((sql: any, values: any, callback: (arg0: Error) => any) => callback(new Error("DB Error")));
        const res = await request(app).get("/listings/category").query({ query: "electronics" });
        expect(res.status).toBe(500);
        expect(res.body).toEqual({ error: "Database error" });
    });

    test("Successful category search", async () => {
        // Mocked behavior: Database returns a successful result
        // Input: query=electronics
        // Expected status code: 200
        // Expected behavior: Return search results with images converted to an array
        mockDb.query.mockImplementation((sql: any, values: any, callback: (arg0: null, arg1: { id: number; title: string; images: string; }[]) => void) => {
            callback(null, [{ id: 1, title: "Test Listing", images: "img1.jpg,img2.jpg" }]);
        });
        const res = await request(app).get("/listings/category").query({ query: "electronics" });
        expect(res.status).toBe(200);
        expect(res.body.results[0].images).toEqual(["img1.jpg", "img2.jpg"]);
    });
});