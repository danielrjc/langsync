const express = require('express');
const router = express.Router();
const authenticationController = require('../controllers/authenticationController');

// Routes
router.post('/login', authenticationController.handleLogin);
router.post('/signup', authenticationController.handleSignup);

module.exports = router;