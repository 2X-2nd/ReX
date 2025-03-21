import dotenv = require('dotenv/config');
require('dotenv');
import express = require('express');
require('express');
import userRoutes = require('./routes/userRoutes.js');
require("userRoutes");


const app = express();

// Middleware
app.use(express.json()); // Allows JSON body parsing

// Routes
app.use('/users', userRoutes);

// Start Server
const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`User Service running on port ${PORT}`));

