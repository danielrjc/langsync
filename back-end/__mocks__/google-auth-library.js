const mockedUsers = require('../models/__mocks__/mockedUsers');

class MockedOAuth2Client {
    constructor({ clientId, clientSecret }) {
      this.clientId = clientId;
      this.clientSecret = clientSecret;
      // Add any necessary initialization code
    }
  
    setCredentials({ access_token }) {
      this.access_token = access_token;
      // Mocked implementation of setting credentials
    }
  
    async verifyIdToken({ idToken, audience }) {
      let email
      if (idToken === 'validToken' && audience === this.clientId) {
        email = 'mocked@email.com';
      } else if(idToken === 'validTokenUnregisteredUser'){
        email = 'unregisterd@email.com';
      } else if (idToken === 'validTokenRegisteredMockUser0'){
        email = mockedUsers[0].email;
      } else {
        throw new Error('Invalid token or audience');
      }
      return { 
        getPayload: () => ({
          sub: 'mockedUserID', 
          email: email 
      })};    }
  
    async getToken(authorizationCode) {
      if (authorizationCode === 'validAuthorizationCode') {
        return {
          tokens: {
            access_token: 'mockedAccessToken',
            refresh_token: 'mockedRefreshToken',
          },
        };
      } else {
        throw new Error('Invalid authorization code');
      }
      // Mocked implementation of token retrieval
    }
  }
  
  module.exports = {
    OAuth2Client: MockedOAuth2Client,
  };
  