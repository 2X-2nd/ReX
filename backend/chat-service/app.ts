import express from 'express';
import chatRoutes from './routes/chatRoutes';
import { connectDB } from './db';
import dotenv from 'dotenv';

dotenv.config();

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Routes
app.use('/', chatRoutes);

// Database connection
connectDB();

export default app;
