const express = require('express');
const dotenv = require('dotenv');

dotenv.config();
const app = express();

// Middleware
app.use(express.json()); // Allows JSON body parsing

// Import user routes
const userRoutes = require('./routes/userRoutes');
app.use('/users', userRoutes);

// Start Server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`User Service running on port ${PORT}`));
