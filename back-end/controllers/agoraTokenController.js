// matchingController.js
const agoraTokenService = require('../services/agoraTokenService'); 
const { RtcRole } = require('agora-access-token');
require('dotenv').config()

exports.getRTCToken = (req, resp) => { 
    resp.header('Access-Control-Allow-Origin', '*');
    const channelName = req.params.channel;
    if (!channelName) {
        return resp.status(500).json({ 'error': 'channel is required' });
    }

    let uid = req.params.uid;
    if(!uid || uid === '') {
        return resp.status(500).json({ 'error': 'uid is required' });
    }

    // get role
    let role;
    if (req.params.role === 'publisher') {
        role = RtcRole.PUBLISHER;
    } else if (req.params.role === 'audience') {
        role = RtcRole.SUBSCRIBER
    } else {
        return resp.status(500).json({ 'error': 'role is incorrect' });
    }

    // get token expiration time
    let expireTime = req.query.expiry;
    if (!expireTime || expireTime === '') {
        expireTime = 3600;
    } else {
        expireTime = parseInt(expireTime, 10);
    }
    const currentTime = Math.floor(Date.now() / 1000);
    const privilegeExpireTime = currentTime + expireTime;

    // get token type
    const tokenType = req.params.tokentype 
    if(tokenType !== 'uid' && tokenType !== 'userAccount') { 
        return resp.status(500).json({ 'error': 'toke type is incorrect' });
    }

    const token = agoraTokenService.generateToken(channelName, uid, role, privilegeExpireTime, tokenType);

    return resp.json({ 'agoraToken': token });
};