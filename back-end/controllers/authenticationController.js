const { captureRejectionSymbol } = require('johnny-five/lib/board');
const authenticationServices = require('../services/authenticationService');
const userServices = require('../services/userService');

exports.handleLogin = async (req, res) => {
    const idToken = req.body.idToken;
    console.log("Login with token " + idToken);

    const verificationResult = await authenticationServices.verifyGoogleToken(idToken);

    if (verificationResult.success) {
        console.log("Verification successful");
        const payload = verificationResult.ticket.getPayload();
        const result = await userServices.findUserByEmail(payload.email);
        if (result.success) {
            console.log("User found");
            res.status(200).json({ success: true, user: result.user });
        }else{
            console.log("User not found");
            console.log(result);
            res.status(401).json(result);
        }
    } else {
        console.log("Verification failed");
        res.status(401).json({ success: false, message: verificationResult.error });
    }
};

exports.handleSignup = async (req, res) => {
    const idToken = req.body.idToken;

    const verificationResult = await authenticationServices.verifyGoogleToken(idToken);

    if (verificationResult.success) {
        const result = await userServices.findUnregistredOrCreateUser(verificationResult.ticket);
        if (result.success) {
            res.status(200).json({ success: true, user: result.user });
        }else{
            res.status(401).json({ success: false, error: result.error });
        }
    } else {
        res.status(401).json({ success: false, error: verificationResult.error });
    }
};
