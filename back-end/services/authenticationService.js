const { OAuth2Client } = require('google-auth-library');
const { google } = require('googleapis');
require('dotenv').config();

const client = new OAuth2Client({
    clientId: process.env.CLIENT_ID,
    clientSecret: process.env.CLIENT_SECRET
})

// ChatGPT Usage: No
// Verifies that the tokens retrieved from login and sign up are valid
async function verifyGoogleToken(idToken) {
    try {
        const ticket = await client.verifyIdToken({
            idToken: idToken,
            audience: process.env.CLIENT_ID,
        });        

        return { success: true, ticket: ticket };
    } catch (error) {
        console.error('Error verifying Google token:', error);
        return { success: false, error: 'Token verification failed' };
    }
}


// Retrieves the access token and refresh token from the authorization code
async function retrieveTokens(authorizationCode) {
    try {
        const { tokens } = await client.getToken(authorizationCode);
        return { success: true, tokens: tokens };
    } catch (error) {
        console.error('Error retrieving access code:', error);
        return { success: false, error: 'Access code retrieval failed' };
    }
}

//ChatGPT Usage: No
async function getGoogleClient(authCode){
    const tokensResponse = await retrieveTokens(authCode);

    if(!tokensResponse.success) {
        return tokensResponse;
    }

    const acccessToken  = tokensResponse.tokens.access_token;


    const auth = new google.auth.OAuth2(
        process.env.CLIENT_ID,
        process.env.CLIENT_SECRET,
    );

    auth.setCredentials({
        access_token: acccessToken,
    });

    return {success: true, auth: auth};
}

module.exports = { verifyGoogleToken, retrieveTokens, getGoogleClient };
