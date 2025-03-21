const request = require('supertest');
const express = require('express');
const userRoutes = require('../routes/userRoutes');

// Mock the database module with fast responses
jest.mock('../db', () => {
    return {
        execute: jest.fn().mockImplementation(() => {
            return Promise.resolve([[{
                google_id: '123',
                email: 'test@example.com',
                username: 'testuser',
                preferences: JSON.stringify(['pref1']),
                latitude: 40.7128,
                longitude: -74.0060
            }]]);
        })
    };
});

const app = express();
app.use(express.json());
app.use('/users', userRoutes);

describe('Performance Tests', () => {
    const RESPONSE_TIME_THRESHOLD = 50; // 50ms threshold for response time

    beforeEach(() => {
        jest.clearAllMocks();
    });

    describe('Response Time Tests', () => {
        const mockUser = {
            google_id: '123',
            email: 'test@example.com',
            username: 'testuser',
            preferences: ['pref1'],
            latitude: 40.7128,
            longitude: -74.0060
        };

        it('should respond to GET request within time threshold', async () => {
            const startTime = Date.now();
            
            await request(app)
                .get('/users/123');
            
            const endTime = Date.now();
            const responseTime = endTime - startTime;
            
            expect(responseTime).toBeLessThan(RESPONSE_TIME_THRESHOLD);
        });

        it('should respond to POST request within time threshold', async () => {
            const startTime = Date.now();
            
            await request(app)
                .post('/users/register')
                .send(mockUser);
            
            const endTime = Date.now();
            const responseTime = endTime - startTime;
            
            expect(responseTime).toBeLessThan(RESPONSE_TIME_THRESHOLD);
        });

        it('should handle concurrent requests efficiently', async () => {
            const numberOfRequests = 10;
            const startTime = Date.now();
            
            // Create array of concurrent requests
            const requests = Array(numberOfRequests).fill().map(() => 
                request(app).get('/users/123')
            );
            
            // Execute all requests concurrently
            await Promise.all(requests);
            
            const endTime = Date.now();
            const totalTime = endTime - startTime;
            const averageTime = totalTime / numberOfRequests;
            
            expect(averageTime).toBeLessThan(RESPONSE_TIME_THRESHOLD);
        });

        it('should maintain performance under load', async () => {
            const numberOfRequests = 50;
            const results = [];
            
            // Execute requests sequentially and measure each response time
            for (let i = 0; i < numberOfRequests; i++) {
                const startTime = Date.now();
                await request(app).get('/users/123');
                const responseTime = Date.now() - startTime;
                results.push(responseTime);
            }
            
            // Calculate statistics
            const averageTime = results.reduce((a, b) => a + b, 0) / results.length;
            const maxTime = Math.max(...results);
            
            // Assertions
            expect(averageTime).toBeLessThan(RESPONSE_TIME_THRESHOLD);
            expect(maxTime).toBeLessThan(RESPONSE_TIME_THRESHOLD * 2); // Allow some flexibility for max time
            
            // Log performance metrics
            console.log(`Performance Test Results:
                Average Response Time: ${averageTime.toFixed(2)}ms
                Max Response Time: ${maxTime.toFixed(2)}ms
                Total Requests: ${numberOfRequests}
            `);
        });

        it('should handle rapid sequential requests', async () => {
            const requests = 20;
            const startTime = Date.now();
            
            for (let i = 0; i < requests; i++) {
                await request(app)
                    .get('/users/123');
            }
            
            const totalTime = Date.now() - startTime;
            const averageTime = totalTime / requests;
            
            expect(averageTime).toBeLessThan(RESPONSE_TIME_THRESHOLD);
            console.log(`Average time per request (sequential): ${averageTime.toFixed(2)}ms`);
        });
    });
}); 