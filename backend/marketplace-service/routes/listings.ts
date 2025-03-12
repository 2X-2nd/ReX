import express, {Request, Response} from 'express'
const mysql = require('mysql2')
const dotenv = require('dotenv')
const cors = require('cors')

dotenv.config()

const app = express()
app.use(express.json({ limit: '1mb'})) // Allow JSON request bodies
app.use(express.urlencoded({ limit: '1mb', extended: true }))
app.use(cors()) // Enable CORS

// Database connection
const db = mysql.createConnection({
    host: process.env.DB_HOST,
    user: process.env.DB_USER,
    password: process.env.DB_PASSWORD,
    database: process.env.DB_NAME
})


db.connect((err: any) => {
    if (err) {
        console.error('❌ Database connection failed:', err)
        return
    }
    console.log('✅ Connected to MySQL')
})

// Create a new listing
app.post('/listings', (req: Request, res: Response) => {
    const { title, description, price, seller_id, images, latitude, longitude, category } = req.body;

    if (!title || !price || !seller_id || !images || images.length === 0) {
        return res.status(400).json({ error: "Missing required fields" });
    }

    // Insert the listing first
    const sql = `INSERT INTO listings (title, description, price, seller_id, latitude, longitude, category) VALUES (?, ?, ?, ?, ?, ?, ?)`;

    db.query(sql, [title, description, price, seller_id, latitude || null, longitude || null, category || null], (err: any, result: { insertId: any }) => {
        if (err) {
            console.error("❌ Error inserting listing:", err);
            return res.status(500).json({ error: "Database error" });
        }

        const listingId = result.insertId;

        // Insert images into listing_images table
        const imageSql = "INSERT INTO listing_images (listing_id, image_url) VALUES ?";
        const imageValues = images.map((image: string) => [listingId, image]);

        db.query(imageSql, [imageValues], (err: any) => {
            if (err) {
                console.error("❌ Error inserting images:", err);
                return res.status(500).json({ error: "Database error" });
            }

            res.status(201).json({ id: listingId, message: "Listing created successfully" });
        });
    });
});

// GET /listings - Retrieve by ID or perform a search
app.get('/listings', (req: Request, res: Response) => {
    const { id } = req.query;

    if (id) {
        // Get listing details
        const sql = `SELECT * FROM listings WHERE id = ?`;

        db.query(sql, [id], (err: any, results: string | any[]) => {
            if (err) return res.status(500).json({ error: "Database error" });
            if (results.length === 0) return res.status(404).json({ error: "Listing not found" });

            const listing = results[0];

            // Fetch images separately
            const imageSql = `SELECT image_url FROM listing_images WHERE listing_id = ?`;
            db.query(imageSql, [id], (err: any, imageResults: any[]) => {
                if (err) return res.status(500).json({ error: "Database error" });

                listing.images = imageResults.map((img: { image_url: any }) => img.image_url);
                res.status(200).json(listing);
            });
        });
    } else {
        // Fetch multiple listings
        const sql = `SELECT l.*, (SELECT image_url FROM listing_images WHERE listing_id = l.id LIMIT 1) AS image FROM listings l`;

        db.query(sql, (err: any, results: any) => {
            if (err) return res.status(500).json({ error: "Something wrong" });
            res.status(200).json({ results });
        });
    }
});

// PUT /listings/:id - Update an existing listing
app.put('/listings/:id', (req: Request, res: Response) => {


    const listingId = req.params.id;
    const { title, description, price, latitude, longitude, images } = req.body;

    // Check if the listing exists before updating
    const checkSql = "SELECT * FROM listings WHERE id = ?";
    db.query(checkSql, [listingId], (err: any, results: string | any[]) => {
        if (err) {
            console.error("Error checking listing:", err)
            return res.status(500).json({ error: "Database error" })
        }

        if (results.length === 0) {
            return res.status(404).json({ error: "Listing not found" })
        }

        // Build dynamic SQL query for updating fields
        let updateFields = [];
        let values = [];

        if (title) {
            updateFields.push("title = ?")
            values.push(title)
        }
        if (description) {
            updateFields.push("description = ?")
            values.push(description)
        }
        if (price) {
            updateFields.push("price = ?")
            values.push(price)
        }
        if (latitude !== undefined) {
            updateFields.push("latitude = ?")
            values.push(latitude)
        }
        if (longitude !== undefined) {
            updateFields.push("longitude = ?")
            values.push(longitude)
        }

        if (updateFields.length > 0) {
            let updateSql = `UPDATE listings SET ${updateFields.join(", ")} WHERE id = ?`;
            values.push(listingId)

            db.query(updateSql, values, (err: any) => {
                if (err) {
                    console.error("Error updating listing:", err)
                    return res.status(500).json({ error: "Database error" })
                }
            })
        }

        // If images are provided, update them
        if (images && images.length > 0) {
            const deleteImageSql = "DELETE FROM listing_images WHERE listing_id = ?";
            db.query(deleteImageSql, [listingId], (err: any) => {
                if (err) {
                    console.error("Error deleting old images:", err)
                    return res.status(500).json({ error: "Database error" })
                }

                const insertImageSql = "INSERT INTO listing_images (listing_id, image_url) VALUES ?";
                const imageValues = images.map((image: any) => [listingId, image])

                db.query(insertImageSql, [imageValues], (err: any) => {
                    if (err) {
                        console.error("Error inserting new images:", err)
                        return res.status(500).json({ error: "Database error" })
                    }

                    res.status(200).json({ message: "Listing updated successfully" })
                })
            })
        } else {
            res.status(200).json({ message: "Listing updated successfully" })
        }
    })
})


// DELETE /listings/:id - Remove a listing from the marketplace
app.delete('/listings/:id', (req: Request, res: Response) => {
    const listingId = req.params.id;

    // Check if the listing exists before deleting
    const checkSql = "SELECT * FROM listings WHERE id = ?";
    db.query(checkSql, [listingId], (err: any, results: string | any[]) => {
        if (err) {
            console.error("Error checking listing:", err)
            return res.status(500).json({ error: "Database error" })
        }

        if (results.length === 0) {
            return res.status(404).json({ error: "Listing not found" })
        }

        // Delete the listing (Cascades to listing_images)
        const deleteSql = "DELETE FROM listings WHERE id = ?";
        db.query(deleteSql, [listingId], (err: any) => {
            if (err) {
                console.error("Error deleting listing:", err)
                return res.status(500).json({ error: "Database error" })
            }

            res.status(200).json({ message: "Listing deleted successfully" })
        })
    })
})

// Search listings by keyword
app.get('/listings/search', (req: Request, res: Response) => {
    const { query } = req.query;
    if (!query) {
        return res.status(400).json({ error: "Query parameter is required" });
    }

    const searchSql = `
        SELECT l.id, l.title, l.description, l.price, 
               GROUP_CONCAT(li.image_url) AS images
        FROM listings l
        LEFT JOIN listing_images li ON l.id = li.listing_id
        WHERE l.title LIKE ? OR l.description LIKE ?
        GROUP BY l.id
    `;
    const searchValue = `%${query}%`;

    db.query(searchSql, [searchValue, searchValue], (err: any, results: any) => {
        if (err) {
            console.error("Error searching listings:", err);
            return res.status(500).json({ error: "Database error" });
        }

        // Convert images from CSV string to an array
        results.forEach((row: any) => {
            row.images = row.images ? row.images.split(',') : [];
        });

        res.status(200).json({ results });
    });
});

// Search listings by category
app.get('/listings/category', (req: Request, res: Response) => {
    const { query } = req.query;
    if (!query) {
        return res.status(400).json({ error: "Query parameter is required" });
    }

    const searchSql = `
        SELECT l.id, l.title, l.description, l.price, 
               GROUP_CONCAT(li.image_url) AS images
        FROM listings l
        LEFT JOIN listing_images li ON l.id = li.listing_id
        WHERE l.category LIKE ?
        GROUP BY l.id
    `;
    const searchValue = `%${query}%`;

    db.query(searchSql, [searchValue], (err: any, results: any) => {
        if (err) {
            console.error("Error searching listings:", err);
            return res.status(500).json({ error: "Database error" });
        }

        // Convert images from CSV string to an array
        results.forEach((row: any) => {
            row.images = row.images ? row.images.split(',') : [];
        });

        res.status(200).json({ results });
    });
});

// **Start the Microservice**
const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
    console.log(`🚀 Listings microservice running on port ${PORT}`)
})
