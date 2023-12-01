const { OAuth2Client } = require('google-auth-library');
const { google } = require('googleapis');
const {User} = require('../models/user');
require('dotenv').config();

const client = new OAuth2Client({
    clientId: process.env.CLIENT_ID,
    clientSecret: process.env.CLIENT_SECRET
})

// ChatGPT Usage: No
// Verifies that the tokens retrieved from login and sign up are valid
async function verifyGoogleToken(idToken) {
    let response;
    try {
        const ticket = await client.verifyIdToken({
            idToken,
            audience: process.env.CLIENT_ID,
        });        
        response = { success: true, ticket };
        return response;
    } catch (error) {
        console.error('Error verifying Google token:', error);
        response = { success: false, error: 'Token verification failed' };
        return response;
    }
}

//ChatGPT Usage: No
// Retrieves the access token and refresh token from the authorization code
async function retrieveAccessToken(authorizationCode) {
    let access_token;
    try {
        const { tokens } = await client.getToken(authorizationCode);
        
        access_token = tokens.access_token;

        const returnObj = { success: true, access_token };
        return returnObj;
    } catch (error) {
        console.error('Error retrieving or updating tokens:', error);
        const returnObj = { success: false,  error: error.message };
        return returnObj;
    }
}

//ChatGPT Usage: No
// Use authorization code to get new authorized Google Client
async function getGoogleClient(authCode){
    const tokensResponse = await retrieveAccessToken(authCode);

    if(!tokensResponse.success) {
        return tokensResponse;
    }

    const acccessToken  = tokensResponse.access_token;
    const auth = new google.auth.OAuth2(
        process.env.CLIENT_ID,
        process.env.CLIENT_SECRET,
    );

    auth.setCredentials({
        access_token: acccessToken,
    });

    let response = {success: true, auth}
    return response;
}

//ChatGPT Usage: No
// Use authorization code to get new authorized Google Client
async function verifyUser(idToken, userId ){
    const verificationResponse = await verifyGoogleToken(idToken);
    const user = await User.findById(userId);
    let response;
    
    if(!verificationResponse.success || verificationResponse.ticket.getPayload().email !== user.email) {
        response = {success: false, error: "Error authenticating user"};
    }else{
        response = {success: true};
    }

    return response;
}


module.exports = { verifyGoogleToken, retrieveAccessToken, getGoogleClient, verifyUser };
