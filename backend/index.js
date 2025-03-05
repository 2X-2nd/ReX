require('dotenv').config();
const express = require('express');
const axios = require('axios');

const app = express();
const PORT = process.env.PORT || 5001;

app.use(express.json()); // Middleware to parse JSON request bodies

// Base URLs for external services
const USER_SERVICE_URL = "http://3.138.121.192:8080/users";
const LISTINGS_SERVICE_URL = "https://nsefhqsvqf.execute-api.us-east-2.amazonaws.com/listings/search";
const SERPAPI_URL = "https://serpapi.com/search.json";
const SERPAPI_KEY = "71bd00175fe99f2c8151b55b7c77e488ef82843058f1215cb2a9163e93e19209";

/**
 * Helper function to remove outliers using IQR method
 */
function removeOutliers(prices) {
    if (prices.length < 5) return prices; // If not enough data, return as is.

    prices.sort((a, b) => a - b); // Sort prices in ascending order

    // Compute Q1 (25th percentile) and Q3 (75th percentile)
    const q1 = prices[Math.floor(prices.length * 0.25)];
    const q3 = prices[Math.floor(prices.length * 0.75)];
    const iqr = q3 - q1;

    // Define valid range (1.5 * IQR)
    const lowerBound = q1 - 1.5 * iqr;
    const upperBound = q3 + 1.5 * iqr;

    // Filter prices within range
    return prices.filter(price => price >= lowerBound && price <= upperBound);
}

app.get('/recommendations/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        console.log(`Received request for recommendations. User ID: ${userId}`);

        // 1️⃣ Fetch user preferences from User Service
        console.log(`Fetching user preferences from: ${USER_SERVICE_URL}/${userId}`);
        const userResponse = await axios.get(`${USER_SERVICE_URL}/${userId}`);
        console.log("User Service Response:", userResponse.data);

        const preferences = userResponse.data.preferences;
        if (!preferences || preferences.length === 0) {
            console.log(`No preferences found for user ${userId}.`);
            return res.json({ message: "No preferences found for user.", recommended_items: [] });
        }

        console.log(`User ${userId} preferences: ${JSON.stringify(preferences)}`);

        // 2️⃣ Query Listings Service for each preference
        let recommendations = [];
        // for (const keyword of preferences) {
        //     console.log(`Fetching listings for keyword: ${keyword}`);
        //     try {
        //         const listingResponse = await axios.get(`${LISTINGS_SERVICE_URL}?query=${keyword}`);
        //         console.log(`Listings found for "${keyword}":`, listingResponse.data.results);
        //         recommendations.push(...listingResponse.data.results);
        //     } catch (listingError) {
        //         console.error(`Error fetching listings for "${keyword}":`, listingError.message);
        //     }
        // }
        for (const keyword of preferences) {
            // Ensure keyword is properly encoded
            const encodedKeyword = encodeURIComponent(keyword);
            const listingURL = `${LISTINGS_SERVICE_URL}?query=${encodedKeyword}`;
        
            console.log(`📡 Fetching listings for keyword: "${keyword}"`);
            console.log(`🔗 Querying Listings Service at: ${listingURL}`);
        
            try {
                const listingResponse = await axios.get(listingURL);
                console.log(`✅ Listings found for "${keyword}":`, JSON.stringify(listingResponse.data, null, 2));
                recommendations.push(...listingResponse.data.results);
            } catch (listingError) {
                console.error(`❌ Error fetching listings for "${keyword}":`, listingError.response?.data || listingError.message);
            }
        }

        // 3️⃣ Return combined results
        console.log(`Final recommendations for user ${userId}:`, recommendations);
        return res.json({
            userId,
            recommended_items: recommendations
        });

    } catch (error) {
        console.error("Error fetching recommendations:", error.message);
        return res.status(500).json({ error: "Internal server error" });
    }
});


/**
 * 2️⃣ POST /price-suggestions
 * Suggests a price for a new listing based on similar items.
 */
app.post('/price-suggestions', async (req, res) => {
    try {
        const { keyword } = req.body;

        if (!keyword) {
            console.log("⚠️ Missing keyword in request.");
            return res.status(400).json({ error: "Keyword is required" });
        }

        console.log(`📩 Received price suggestion request for keyword: "${keyword}"`);

        // Fetch eBay listings using SerpAPI, limit results to 50
        const apiUrl = `${SERPAPI_URL}?engine=ebay&_nkw=${keyword}&ebay_domain=ebay.com&api_key=${SERPAPI_KEY}&_ipg=50`;
        console.log(`🔍 Sending request to SerpAPI: ${apiUrl}`);

        const ebayResponse = await axios.get(apiUrl);
        console.log("✅ SerpAPI Response received.");

        const ebayResults = ebayResponse.data.organic_results || [];
        console.log(`🛒 Fetched ${ebayResults.length} results from eBay.`);

        if (ebayResults.length === 0) {
            console.log(`⚠️ No results found for keyword: "${keyword}".`);
            return res.json({ best_price: null, similar_items: [] });
        }

        // Extract valid prices and URLs
        let extractedItems = ebayResults.map(item => ({
            name: item.title,
            price: item.price?.extracted || null,
            url: item.link
        })).filter(item => item.price !== null); // Ensure valid prices

        console.log(`📊 Found ${extractedItems.length} valid items with prices.`);

        if (extractedItems.length === 0) {
            console.log("⚠️ No valid prices found among retrieved items.");
            return res.json({ best_price: null, similar_items: [] });
        }

        // Remove outliers using IQR method
        let validPrices = extractedItems.map(item => item.price);
        let filteredPrices = removeOutliers(validPrices);

        console.log(`📉 Removed outliers. Using ${filteredPrices.length} prices for computation.`);

        if (filteredPrices.length === 0) {
            console.log("⚠️ All prices were outliers! Defaulting to basic average.");
            filteredPrices = validPrices;
        }

        // Compute the best price (mean of valid prices)
        const bestPrice = filteredPrices.reduce((sum, price) => sum + price, 0) / filteredPrices.length;
        console.log(`💰 Computed best price: $${bestPrice.toFixed(2)}`);

        // Select the top 5 listings based on the closest price to the computed average
        extractedItems.sort((a, b) => Math.abs(a.price - bestPrice) - Math.abs(b.price - bestPrice));
        const topItems = extractedItems.slice(0, 5);

        return res.json({
            best_price: parseFloat(bestPrice.toFixed(2)), // Return rounded price
            similar_items: topItems
        });

    } catch (error) {
        console.error("❌ Error fetching price suggestions:", error.message);
        return res.status(500).json({ error: "Internal server error" });
    }
});

//for the buyer, look at existing item and compare them to ebay similar item
app.get('/price-comparison/:itemId', async (req, res) => {
    try {
        const { itemId } = req.params;
        console.log(`🔎 Fetching price comparison for Item ID: ${itemId}`);

        // 1️⃣ Fetch Listing Details
        const listingUrl = `https://nsefhqsvqf.execute-api.us-east-2.amazonaws.com/listings?id=${itemId}`;
        console.log(`📡 Querying Listing Service: ${listingUrl}`);

        const listingResponse = await axios.get(listingUrl);
        const listing = listingResponse.data;

        console.log(`✅ Listing details retrieved: ${JSON.stringify(listing, null, 2)}`);

        if (!listing || !listing.title || !listing.price) {
            console.error("❌ Invalid listing data received.");
            return res.status(400).json({ error: "Invalid listing details." });
        }

        const listingPrice = parseFloat(listing.price);
        console.log(`💰 Listing Price: $${listingPrice}`);

        // 2️⃣ Fetch Similar Items via eBay (SerpAPI)
        console.log(`🔍 Fetching price suggestions from eBay for keyword: "${listing.title}"`);
        const apiUrl = `${SERPAPI_URL}?engine=ebay&_nkw=${encodeURIComponent(listing.title)}&ebay_domain=ebay.com&api_key=${SERPAPI_KEY}&_ipg=50`;
        console.log(`📡 Querying SerpAPI: ${apiUrl}`);

        const ebayResponse = await axios.get(apiUrl);
        console.log("✅ SerpAPI Response received.");

        const ebayResults = ebayResponse.data.organic_results || [];
        console.log(`🛒 Fetched ${ebayResults.length} results from eBay.`);

        if (ebayResults.length === 0) {
            console.log(`⚠️ No results found for keyword: "${listing.title}".`);
            return res.json({
                original_item: {
                    name: listing.title,
                    price: listingPrice,
                    url: listing.images?.[0] || "N/A"
                },
                similar_items: [],
                message: "No pricing data available for comparison."
            });
        }

        // Extract valid prices and URLs
        let extractedItems = ebayResults.map(item => ({
            name: item.title,
            price: item.price?.extracted || null,
            url: item.link
        })).filter(item => item.price !== null);

        console.log(`📊 Found ${extractedItems.length} valid items with prices.`);

        if (extractedItems.length === 0) {
            console.log("⚠️ No valid prices found among retrieved items.");
            return res.json({
                original_item: {
                    name: listing.title,
                    price: listingPrice,
                    url: listing.images?.[0] || "N/A"
                },
                similar_items: [],
                message: "No valid price recommendations found."
            });
        }

        // Remove outliers using IQR method
        let validPrices = extractedItems.map(item => item.price);
        let filteredPrices = removeOutliers(validPrices);

        console.log(`📉 Removed outliers. Using ${filteredPrices.length} prices for computation.`);

        if (filteredPrices.length === 0) {
            console.log("⚠️ All prices were outliers! Defaulting to basic average.");
            filteredPrices = validPrices;
        }

        // Compute the best price (mean of valid prices)
        const bestPrice = filteredPrices.reduce((sum, price) => sum + price, 0) / filteredPrices.length;
        console.log(`💰 Computed best price: $${bestPrice.toFixed(2)}`);

        // Select the top 5 listings based on closest price to the computed average
        extractedItems.sort((a, b) => Math.abs(a.price - bestPrice) - Math.abs(b.price - bestPrice));
        const topItems = extractedItems.slice(0, 5);

        let message;
        if (listingPrice < bestPrice) {
            message = "This item is priced below market value, it's a bargain! 🎉";
        } else {
            message = "It is above market asking price, check out these alternatives. 🔎";
        }

        const response = {
            original_item: {
                name: listing.title,
                price: listingPrice,
                url: listing.images?.[0] || "N/A"
            },
            similar_items: topItems,
            message
        };

        console.log(`🚀 Final Response: ${JSON.stringify(response, null, 2)}`);
        res.json(response);

    } catch (error) {
        console.error("❌ Error in /price-comparison:", error.message);
        return res.status(500).json({ error: "Internal server error" });
    }
});


app.listen(PORT, () => {
    console.log(`🚀 Recommendation Service running on port ${PORT}`);
});

