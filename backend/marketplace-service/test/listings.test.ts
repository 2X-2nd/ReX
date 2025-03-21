import request from 'supertest'
import mysql from 'mysql2'
import app from '../routes/listings'
require("dotenv").config();

describe("Listings API - No Mocking", () => {

    beforeAll(async () => {
        const db = mysql.createPool({
            host: process.env.DB_HOST,
            user: process.env.DB_USER,
            password: process.env.DB_PASSWORD,
            database: process.env.DB_NAME
        });
        await new Promise<void>((resolve, reject) => {
            db.query("SET FOREIGN_KEY_CHECKS=0", (err) => {
                if (err) reject(err);
                db.query("TRUNCATE TABLE listing_images", (err) => {
                    if (err) reject(err);
                    db.query("TRUNCATE TABLE listings", (err) => {
                        if (err) reject(err);
                        db.query("SET FOREIGN_KEY_CHECKS=1", (err) => {
                            if (err) reject(err);
                            resolve();
                        });
                    });
                });
            });
        });
    });

    /**
     * Input:
     * {
            "title": "Test Listing",
            "description": "This is a test description",
            "price": 100,
            "seller_id": 1,
            "images": ["http://image1.com"],
            "latitude": 10.123,
            "longitude": 20.456,
            "category": "Electronics"
        }
     * Expected status code: 201
     * Expected behavior: The listing is successfully created.
     * Expected output: id=1
     */
    test("POST /listings → Create a listing", async () => {
        const response = await request(app)
            .post("/listings")
            .send({
                title: "Test Listing",
                description: "This is a test description",
                price: 100,
                seller_id: 1,
                images: ["http://image1.com"],
                latitude: 10.123,
                longitude: 20.456,
                category: "Electronics"
            });

        expect(response.status).toBe(201);
        expect(response.body).toHaveProperty("id");
    });

    /**
     * Input: no input
     * Expected status code: 200
     * Expected behavior: Returns an array of all listings.
     * Expected output:
     * {
            "results": [
                {
                "id": 1,
                "title": "Test Listing",
                "description": "This is a test description",
                "price": 100,
                "images": ["http://image1.com"],
                "category": "Electronics"
                }
            ]
        }
     */
    test("GET /listings → Retrieve all listings", async () => {
        const response = await request(app).get("/listings");

        expect(response.status).toBe(200);
        expect(Array.isArray(response.body.results)).toBeTruthy();
    });

    /**
      * Input: id=1
      * Expected status code: 200
      * Expected behavior: Returns the details of the listing with ID 1.
      * Expected output:
      * {
            "id": 1,
            "title": "Test Listing",
            "description": "This is a test description",
            "price": 100,
            "images": ["http://image1.com"],
            "category": "Electronics"
        }
      */
    test("GET /listings?id=1 → Retrieve specific listing", async () => {
        const response = await request(app).get("/listings?id=1");

        expect(response.status).toBe(200);
        expect(response.body).toHaveProperty("title");
    });

    /**
      * Input: 
      * {
            "title": "Test Listing",
            "description": "A great item",
            "price": 100,
            "seller_id": 1,
            "images": ["img1.jpg"],
            "latitude": 49.2827,
            "longitude": -123.1207,
            "category": "Electronics"
        }
      * Expected status code: 200
      * Expected behavior: The listing is updated successfully.
      * Expected output:
      * {
            "message": "Listing updated successfully"
        }
      */
    test("PUT /listings/:id → Update a listing", async () => {
        const response = await request(app)
            .put("/listings/1")
            .send({
                title: "Test Listing",
                description: "A great item",
                price: 100,
                seller_id: 1,
                images: ["img1.jpg"],
                latitude: 49.2827,
                longitude: -123.1207,
                category: "Electronics"
            });

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("Listing updated successfully");
    });

    /**
      * Input: 
      * {
            "title": "Test Listing",
            "description": "A great item",
            "price": 100,
            "seller_id": 1,
            "latitude": 49.2827,
            "longitude": -123.1207,
            "category": "Electronics"
        }
      * Expected status code: 200
      * Expected behavior: The listing is updated successfully without modifying images.
      * Expected output:
      * {
            "message": "Listing updated successfully"
        }
      */
    test("PUT /listings/:id → Update a listing without image update", async () => {
        const response = await request(app)
            .put("/listings/1")
            .send({
                title: "Test Listing",
                description: "A great item",
                price: 100,
                seller_id: 1,
                latitude: 49.2827,
                longitude: -123.1207,
                category: "Electronics"
            });

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("Listing updated successfully");
    });

    /**
      * Input: id=1
      * Expected status code: 200
      * Expected behavior: The listing is deleted successfully
      * Expected output:
      * {
            "message": "Listing deleted successfully"
        }
      */
    test("DELETE /listings/:id → Delete a listing", async () => {
        const response = await request(app).delete("/listings/1");

        expect(response.status).toBe(200);
        expect(response.body.message).toBe("Listing deleted successfully");
    });

});
