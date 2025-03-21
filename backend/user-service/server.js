import 'dotenv/config';
import express from 'express';
import userRoutes from './routes/userRoutes.js';

const app = express();

// Middleware
app.use(express.json()); // Allows JSON body parsing

// Routes
app.use('/users', userRoutes);

// Start Server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`User Service running on port ${PORT}`));

