const request = require('supertest');
const axios = require('axios');
const MockAdapter = require('axios-mock-adapter');
const { app } = require('./index');

// Create a new instance of axios-mock-adapter
const mock = new MockAdapter(axios);

describe('Recommendation Engine Performance Tests', () => {
    // Set up test data
    const userId = '123';
    const mockUserPreferences = {
        preferences: ['laptop', 'phone', 'camera', 'headphones', 'watch']
    };

    const mockListingResponse = {
        results: Array(20).fill().map((_, i) => ({
            id: i + 1,
            title: `Test Item ${i + 1}`,
            price: (100 + i * 10).toString(),
            description: `Description for item ${i + 1}`
        }))
    };

    const mockEbayResponse = {
        organic_results: Array(50).fill().map((_, i) => ({
            title: `eBay Item ${i + 1}`,
            price: { extracted: 100 + i * 10 },
            link: `http://example.com/${i + 1}`
        }))
    };

    beforeEach(() => {
        mock.reset();
        // Set up common mocks
        mock.onGet(`http://3.138.121.192:8080/users/${userId}`)
            .reply(200, mockUserPreferences);
        
        mock.onGet(/https:\/\/nsefhqsvqf.execute-api.us-east-2.amazonaws.com\/listings\/search/)
            .reply(200, mockListingResponse);

        mock.onGet(/https:\/\/serpapi.com\/search.json/)
            .reply(200, mockEbayResponse);

        mock.onGet(/https:\/\/nsefhqsvqf.execute-api.us-east-2.amazonaws.com\/listings\?id=.*/)
            .reply(200, mockListingResponse.results[0]);
    });

    describe('Individual API Response Time Tests', () => {
        const ACCEPTABLE_RESPONSE_TIME = 200; // 200ms is our target maximum response time

        it('should generate user recommendations within acceptable time', async () => {
            const start = Date.now();
            
            await request(app)
                .get(`/recommendations/${userId}`)
                .expect(200);
            
            const responseTime = Date.now() - start;
            console.log(`User recommendations response time: ${responseTime}ms`);
            expect(responseTime).toBeLessThan(ACCEPTABLE_RESPONSE_TIME);
        });

        it('should calculate price suggestions within acceptable time', async () => {
            const start = Date.now();
            
            await request(app)
                .post('/price-suggestions')
                .send({ keyword: 'test laptop' })
                .expect(200);
            
            const responseTime = Date.now() - start;
            console.log(`Price calculation response time: ${responseTime}ms`);
            expect(responseTime).toBeLessThan(ACCEPTABLE_RESPONSE_TIME);
        });

        it('should perform market price comparison within acceptable time', async () => {
            const start = Date.now();
            
            await request(app)
                .get('/price-comparison/123')
                .expect(200);
            
            const responseTime = Date.now() - start;
            console.log(`Market comparison response time: ${responseTime}ms`);
            expect(responseTime).toBeLessThan(ACCEPTABLE_RESPONSE_TIME);
        });
    });

    describe('High Traffic Load Testing', () => {
        const NUM_REQUESTS = 50; // Number of concurrent requests to simulate
        const ACCEPTABLE_AVG_RESPONSE_TIME = 1000; // 1000ms average response time target for high load

        it('should handle multiple concurrent recommendation requests', async () => {
            const requests = Array(NUM_REQUESTS).fill().map(() => {
                const start = Date.now();
                return request(app)
                    .get(`/recommendations/${userId}`)
                    .expect(200)
                    .then(() => Date.now() - start);
            });

            const responseTimes = await Promise.all(requests);
            const avgResponseTime = responseTimes.reduce((a, b) => a + b) / responseTimes.length;
            
            console.log(`Average response time for ${NUM_REQUESTS} concurrent recommendation requests: ${avgResponseTime}ms`);
            console.log(`Max recommendation response time: ${Math.max(...responseTimes)}ms`);
            console.log(`Min recommendation response time: ${Math.min(...responseTimes)}ms`);
            
            expect(avgResponseTime).toBeLessThan(ACCEPTABLE_AVG_RESPONSE_TIME);
        });

        it('should handle multiple concurrent price calculation requests', async () => {
            const requests = Array(NUM_REQUESTS).fill().map(() => {
                const start = Date.now();
                return request(app)
                    .post('/price-suggestions')
                    .send({ keyword: 'test laptop' })
                    .expect(200)
                    .then(() => Date.now() - start);
            });

            const responseTimes = await Promise.all(requests);
            const avgResponseTime = responseTimes.reduce((a, b) => a + b) / responseTimes.length;
            
            console.log(`Average response time for ${NUM_REQUESTS} concurrent price calculations: ${avgResponseTime}ms`);
            console.log(`Max price calculation response time: ${Math.max(...responseTimes)}ms`);
            console.log(`Min price calculation response time: ${Math.min(...responseTimes)}ms`);
            
            expect(avgResponseTime).toBeLessThan(ACCEPTABLE_AVG_RESPONSE_TIME);
        });

        it('should handle multiple concurrent market comparison requests', async () => {
            const requests = Array(NUM_REQUESTS).fill().map(() => {
                const start = Date.now();
                return request(app)
                    .get('/price-comparison/123')
                    .expect(200)
                    .then(() => Date.now() - start);
            });

            const responseTimes = await Promise.all(requests);
            const avgResponseTime = responseTimes.reduce((a, b) => a + b) / responseTimes.length;
            
            console.log(`Average response time for ${NUM_REQUESTS} concurrent market comparisons: ${avgResponseTime}ms`);
            console.log(`Max market comparison response time: ${Math.max(...responseTimes)}ms`);
            console.log(`Min market comparison response time: ${Math.min(...responseTimes)}ms`);
            
            expect(avgResponseTime).toBeLessThan(ACCEPTABLE_AVG_RESPONSE_TIME);
        });
    });
}); 