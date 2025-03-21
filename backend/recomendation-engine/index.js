import 'dotenv/config';
import express from 'express';
import axios from 'axios';

const app = express();
const PORT = process.env.PORT || 5001;

app.use(express.json());

// Base URLs for external services
const USER_SERVICE_URL = 'http://3.138.121.192:8080/users';
const LISTINGS_SERVICE_URL = 'https://nsefhqsvqf.execute-api.us-east-2.amazonaws.com/listings/search';
const SERPAPI_URL = 'https://serpapi.com/search.json';
const SERPAPI_KEY = '71bd00175fe99f2c8151b55b7c77e488ef82843058f1215cb2a9163e93e19209';

/**
 * Helper function to remove outliers using IQR method
 */
function removeOutliers(prices) {
    if (prices.length < 5) return prices;

    prices.sort((a, b) => a - b);
    const q1 = prices[Math.floor(prices.length * 0.25)];
    const q3 = prices[Math.floor(prices.length * 0.75)];
    const iqr = q3 - q1;

    const lowerBound = q1 - 1.5 * iqr;
    const upperBound = q3 + 1.5 * iqr;

    return prices.filter(price => price >= lowerBound && price <= upperBound);
}

app.get('/recommendations/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        console.log(`Received request for recommendations. User ID: ${userId}`);

        const userResponse = await axios.get(`${USER_SERVICE_URL}/${userId}`);
        const preferences = userResponse.data.preferences;

        if (!preferences || preferences.length === 0) {
            return res.json({ message: "No preferences found for user.", recommended_items: [] });
        }

        let recommendations = [];
        for (const keyword of preferences) {
            const encodedKeyword = encodeURIComponent(keyword);
            const listingURL = `${LISTINGS_SERVICE_URL}?query=${encodedKeyword}`;

            try {
                const listingResponse = await axios.get(listingURL);
                recommendations.push(...listingResponse.data.results);
            } catch (listingError) {
                console.error(`Error fetching listings for "${keyword}":`, listingError.response?.data || listingError.message);
            }
        }

        return res.json({
            userId,
            recommended_items: recommendations
        });

    } catch (error) {
        console.error("Error fetching recommendations:", error.message);
        return res.status(500).json({ error: "Internal server error" });
    }
});

app.post('/price-suggestions', async (req, res) => {
    try {
        const { keyword } = req.body;

        if (!keyword) {
            return res.status(400).json({ error: "Keyword is required" });
        }

        const apiUrl = `${SERPAPI_URL}?engine=ebay&_nkw=${encodeURIComponent(keyword)}&ebay_domain=ebay.com&api_key=${SERPAPI_KEY}&_ipg=50`;
        const ebayResponse = await axios.get(apiUrl);

        const ebayResults = ebayResponse.data.organic_results || [];

        if (ebayResults.length === 0) {
            return res.json({ best_price: null, similar_items: [] });
        }

        let extractedItems = ebayResults.map(item => ({
            name: item.title,
            price: item.price?.extracted || null,
            url: item.link
        })).filter(item => item.price !== null);

        if (extractedItems.length === 0) {
            return res.json({ best_price: null, similar_items: [] });
        }

        let validPrices = extractedItems.map(item => item.price);
        let filteredPrices = removeOutliers(validPrices);

        if (filteredPrices.length === 0) {
            filteredPrices = validPrices;
        }

        const bestPrice = filteredPrices.reduce((sum, price) => sum + price, 0) / filteredPrices.length;

        extractedItems.sort((a, b) => Math.abs(a.price - bestPrice) - Math.abs(b.price - bestPrice));
        const topItems = extractedItems.slice(0, 5);

        return res.json({
            best_price: parseFloat(bestPrice.toFixed(2)),
            similar_items: topItems
        });

    } catch (error) {
        console.error("Error fetching price suggestions:", error.message);
        return res.status(500).json({ error: "Internal server error" });
    }
});

app.get('/price-comparison/:itemId', async (req, res) => {
    try {
        const { itemId } = req.params;
        const listingUrl = `https://nsefhqsvqf.execute-api.us-east-2.amazonaws.com/listings?id=${itemId}`;
        const listingResponse = await axios.get(listingUrl);
        const listing = listingResponse.data;

        if (!listing || !listing.title || !listing.price) {
            return res.status(400).json({ error: "Invalid listing details." });
        }

        const listingPrice = parseFloat(listing.price);
        const apiUrl = `${SERPAPI_URL}?engine=ebay&_nkw=${encodeURIComponent(listing.title)}&ebay_domain=ebay.com&api_key=${SERPAPI_KEY}&_ipg=50`;
        const ebayResponse = await axios.get(apiUrl);

        const ebayResults = ebayResponse.data.organic_results || [];

        if (ebayResults.length === 0) {
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

        let extractedItems = ebayResults.map(item => ({
            name: item.title,
            price: item.price?.extracted || null,
            url: item.link
        })).filter(item => item.price !== null);

        if (extractedItems.length === 0) {
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

        let validPrices = extractedItems.map(item => item.price);
        let filteredPrices = removeOutliers(validPrices);

        if (filteredPrices.length === 0) {
            filteredPrices = validPrices;
        }

        const bestPrice = filteredPrices.reduce((sum, price) => sum + price, 0) / filteredPrices.length;

        extractedItems.sort((a, b) => Math.abs(a.price - bestPrice) - Math.abs(b.price - bestPrice));
        const topItems = extractedItems.slice(0, 5);

        const message = listingPrice < bestPrice
            ? "This item is priced below market value, it's a bargain! 🎉"
            : "It is above market asking price, check out these alternatives. 🔎";

        return res.json({
            original_item: {
                name: listing.title,
                price: listingPrice,
                url: listing.images?.[0] || "N/A"
            },
            similar_items: topItems,
            message
        });

    } catch (error) {
        console.error("Error in /price-comparison:", error.message);
        return res.status(500).json({ error: "Internal server error" });
    }
});

app.listen(PORT, () => {
    console.log(`🚀 Recommendation Service running on port ${PORT}`);
});
