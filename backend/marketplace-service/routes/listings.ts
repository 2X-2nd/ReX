const express = require('express')
const mysql = require('mysql2')
const dotenv = require('dotenv')
const cors = require('cors')

dotenv.config()

const app = express()
app.use(express.json()) // Allow JSON request bodies
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

// Create a new listing (Updated to include geolocation)
app.post('/listings', (req: { body: { title: any; description: any; price: any; seller_id: any; images: any; latitude: any; longitude: any; }; }, res: { status: (arg0: number) => { (): any; new(): any; json: { (arg0: { error?: string; id?: any; message?: string; }): void; new(): any; }; }; }) => {
    const { title, description, price, seller_id, images, latitude, longitude } = req.body;

    if (!title || !price || !seller_id || !images) {
        return res.status(400).json({ error: "Missing required fields" })
    }

    // Insert the listing into the database
    const sql = `
        INSERT INTO listings (title, description, price, seller_id, images, latitude, longitude) 
        VALUES (?, ?, ?, ?, ?, ?, ?)
    `;
    db.query(sql, [title, description, price, seller_id, images || null, latitude || null, longitude || null], (err: any, result: { insertId: any; }) => {
        if (err) {
            console.error("Error inserting listing:", err)
            return res.status(500).json({ error: "Database error" })
        }

        const listingId = result.insertId;
        res.status(201).json({ id: listingId, message: "Listing created successfully" })
    })
})

// GET /listings - Retrieve by ID or perform a search
app.get('/listings', (req: { query: { id: any; query: any; min_price: any; max_price: any; latitude: any; longitude: any; radius: any; }; }, res: { status: (arg0: number) => { (): any; new(): any; json: { (arg0: { error?: string; results?: any; }): void; new(): any; }; }; }) => {
    const { id, query, min_price, max_price, latitude, longitude, radius } = req.query;

    console.log("Incoming request with params:", req.query)

    if (id) {
        console.log(`Fetching listing with ID: ${id}`)
        const sql = `
            SELECT id, title, description, price, seller_id, latitude, longitude, created_at 
            FROM listings WHERE id = ?
        `;

        db.query(sql, [id], (err: any, results: string | any[]) => {
            if (err) {
                console.error("❌ Error retrieving listing:", err)
                return res.status(500).json({ error: "Database error" })
            }

            if (results.length === 0) {
                console.log("❌ No listing found for ID:", id)
                return res.status(404).json({ error: "Listing not found" })
            }

            console.log("✅ Found listing:", results[0])

            const listing = results[0];

            // Fetch images for the listing
            const imageSql = "SELECT image_url FROM listing_images WHERE listing_id = ?";
            db.query(imageSql, [id], (err: any, imageResults: any[]) => {
                if (err) {
                    console.error("❌ Error retrieving images:", err)
                    return res.status(500).json({ error: "Database error" })
                }

                listing.images = imageResults.map((img: { image_url: any; }) => img.image_url)

                console.log("✅ Final response:", listing)
                res.status(200).json(listing)
            })
        })
        return;
    }

    // Search query
    let sql = `
        SELECT l.id, l.title, l.description, l.price, l.seller_id, l.latitude, l.longitude, l.created_at, 
               (SELECT image_url FROM listing_images WHERE listing_id = l.id LIMIT 1) AS image 
        FROM listings l
        WHERE 1=1
    `;

    let values = [];

    if (query) {
        sql += " AND (LOWER(l.title) LIKE LOWER(?) OR LOWER(l.description) LIKE LOWER(?))";
        values.push(`%${query}%`, `%${query}%`)
        console.log("🔍 Searching for query:", query)
    }

    // Price range filter
    if (min_price) {
        sql += " AND l.price >= ?";
        values.push(min_price)
        console.log(`🔍 Filtering by min_price: ${min_price}`)
    }
    if (max_price) {
        sql += " AND l.price <= ?";
        values.push(max_price)
        console.log(`🔍 Filtering by max_price: ${max_price}`)
    }

    // Geolocation filter
    if (latitude && longitude && radius) {
        sql += ` AND (6371 * acos(cos(radians(?)) * cos(radians(l.latitude)) * 
                 cos(radians(l.longitude) - radians(?)) + sin(radians(?)) * sin(radians(l.latitude)))) < ?`;
        values.push(latitude, longitude, latitude, radius)
        console.log(`🔍 Filtering by location: lat=${latitude}, lon=${longitude}, radius=${radius}`)
    }

    console.log("🟢 Final SQL Query:", sql)
    console.log("🟢 SQL Values:", values)

    db.query(sql, values, (err: any, results: string | any[]) => {
        if (err) {
            console.error("❌ Error searching listings:", err)
            return res.status(500).json({ error: "Database error" })
        }

        if (results.length === 0) {
            console.log("❌ No listings match search criteria")
            return res.status(404).json({ error: "No listings match your search" })
        }

        console.log("✅ Found listings:", results)
        res.status(200).json({ results })
    })
})


// // GET /listings/search - Search for listings by keyword, price range, or location
// app.get('/listings/search', (req, res) => {
//     const { query, min_price, max_price, latitude, longitude, radius } = req.query;

//     //debugging
//     console.log("🟢 Received a request to /listings/search")
//     console.log("Query parameters:", req.query)

//     let sql = `
//         SELECT l.id, l.title, l.description, l.price, l.seller_id, l.latitude, l.longitude, l.created_at, 
//                (SELECT image_url FROM listing_images WHERE listing_id = l.id LIMIT 1) AS image 
//         FROM listings l
//         WHERE 1=1
//     `;

//     let values = [];

//     // Keyword search (title or description)
//     if (query) {
//         sql += " AND (l.title LIKE ? OR l.description LIKE ?)";
//         values.push(`%${query}%`, `%${query}%`)
//     }

//     // Price range filter
//     if (min_price) {
//         sql += " AND l.price >= ?";
//         values.push(min_price)
//     }
//     if (max_price) {
//         sql += " AND l.price <= ?";
//         values.push(max_price)
//     }

//     // Geolocation filter (Search listings within a radius)
//     if (latitude && longitude && radius) {
//         sql += ` AND (6371 * acos(cos(radians(?)) * cos(radians(l.latitude)) * 
//                  cos(radians(l.longitude) - radians(?)) + sin(radians(?)) * sin(radians(l.latitude)))) < ?`;
//         values.push(latitude, longitude, latitude, radius)
//     }

//     //debugging
//     console.log("Executing SQL:", sql)
//     console.log("With values:", values)


//     db.query(sql, values, (err, results) => {
//         if (err) {
//             console.error("Error searching listings:", err)
//             return res.status(500).json({ error: "Database error" })
//         }

//         if (results.length === 0) {
//             return res.status(404).json({ error: "No listings match your search" })
//         }

//         res.status(200).json({ results })
//     })
// })



// // GET /listings/:id - Retrieve a specific listing by ID
// app.get('/listings/:id', (req, res) => {
//         //debug
//         console.log("🟢 Received a request to /listings/id")
//     const listingId = req.params.id;

//     // First, get listing details
//     const sql = `
//         SELECT id, title, description, price, seller_id, latitude, longitude, created_at 
//         FROM listings WHERE id = ?
//     `;

//     db.query(sql, [listingId], (err, results) => {
//         if (err) {
//             console.error("Error retrieving listing:", err)
//             return res.status(500).json({ error: "Database error" })
//         }

//         if (results.length === 0) {
//             return res.status(404).json({ error: "Listing not found" })
//         }

//         const listing = results[0];

//         // Then, get associated images
//         const imageSql = "SELECT image_url FROM listing_images WHERE listing_id = ?";
//         db.query(imageSql, [listingId], (err, imageResults) => {
//             if (err) {
//                 console.error("Error retrieving images:", err)
//                 return res.status(500).json({ error: "Database error" })
//             }

//             // Add images to listing object
//             listing.images = imageResults.map(img => img.image_url)

//             res.status(200).json(listing)
//         })
//     })
// })

// PUT /listings/:id - Update an existing listing
app.put('/listings/:id', (req: { params: { id: any; }; body: { title: any; description: any; price: any; latitude: any; longitude: any; images: any; }; }, res: { status: (arg0: number) => { (): any; new(): any; json: { (arg0: { error?: string; message?: string; }): void; new(): any; }; }; }) => {


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
app.delete('/listings/:id', (req: { params: { id: any; }; }, res: { status: (arg0: number) => { (): any; new(): any; json: { (arg0: { error?: string; message?: string; }): void; new(): any; }; }; }) => {
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



// **Start the Microservice**
const PORT = process.env.SERVICE_PORT || 5000;
app.listen(PORT, () => {
    console.log(`🚀 Listings microservice running on port ${PORT}`)
})
