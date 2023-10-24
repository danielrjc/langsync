const express = require('express');
const router = express.Router();
const agoraTokenController = require('../controllers/agoraTokenController');

router.get('/:channel/:role/:tokentype/:uid', agoraTokenController.getRTCToken); 

module.exports = router;
