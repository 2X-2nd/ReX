const request = require('supertest');
const express = require('express');
const userRoutes = require('../routes/userRoutes');

// Mock the database module
jest.mock('../db', () => {
    return {
        execute: jest.fn()
    };
});

const app = express();
app.use(express.json());
app.use('/users', userRoutes);

describe('Database Down Tests', () => {
    beforeEach(() => {
        // Clear all mocks before each test
        jest.clearAllMocks();
    });

    it('should handle database down during user registration', async () => {
        // Mock database error
        require('../db').execute.mockRejectedValueOnce(new Error('Database connection failed'));

        const response = await request(app)
            .post('/users/register')
            .send({
                google_id: '123',
                email: 'test@example.com',
                username: 'testuser',
                preferences: ['pref1'],
                latitude: 40.7128,
                longitude: -74.0060
            });

        expect(response.status).toBe(500);
        expect(response.body).toHaveProperty('error');
    });

    it('should handle database down during user retrieval', async () => {
        // Mock database error
        require('../db').execute.mockRejectedValueOnce(new Error('Database connection failed'));

        const response = await request(app)
            .get('/users/123');

        expect(response.status).toBe(500);
        expect(response.body).toHaveProperty('error');
    });

    it('should handle database down during user deletion', async () => {
        // Mock database error
        require('../db').execute.mockRejectedValueOnce(new Error('Database connection failed'));

        const response = await request(app)
            .delete('/users/123');

        expect(response.status).toBe(500);
        expect(response.body).toHaveProperty('error');
    });

    it('should handle database down during user update', async () => {
        // Mock database error
        require('../db').execute.mockRejectedValueOnce(new Error('Database connection failed'));

        const response = await request(app)
            .put('/users/123')
            .send({
                email: 'updated@example.com',
                username: 'updateduser',
                preferences: ['newpref1'],
                latitude: 41.7128,
                longitude: -75.0060
            });

        expect(response.status).toBe(500);
        expect(response.body).toHaveProperty('error');
    });
}); 
