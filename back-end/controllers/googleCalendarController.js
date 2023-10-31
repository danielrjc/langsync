const googleCalendarService = require('../services/googleCalendarService');

//ChatGPT Usage: No
exports.createEvent = async (req, res) => {
    const authCode = req.body.authCode;
    const event = req.body.event;
    
    const verificationResult = await googleCalendarService.createEvent(authCode, event);
    if (verificationResult.success) {
        res.status(200).json({ success: true, message: "Event Created" });
    } else {
        res.status(401).json({ success: false, error: verificationResult.error });
    }
}