const request = require('supertest');
const app = require('../app'); // Adjust the path as needed
const {mockedUsers} = require('../models/__mocks__/mockedUsers');

jest.mock('../models/user');

describe('GET /recommendations/:id', () => {
    // Inupt: valid userId, for user with "Expert" as learning preference - mockedUsers[0]
    // Expected status code: 200
    // Expected behaviour: return list of recommended users
    // Expected output: { recommendedUsersList: [mockedUsers[3]] }
    test('Succesfully get recommendations for user with "Both" as learning preference', async () => {
        const response = await request(app).get(`/recommendations/${mockedUsers[0]._id}`);

        expect(response.status).toBe(200);

        expect(response.body.recommendedUsersList.length).toEqual(1);
        expect(response.body.recommendedUsersList[0]._id).toEqual(mockedUsers[3]._id.toString());
    });

    // Inupt: valid userId, for user with "Partner" as learning preference - mockedUsers[1]
    // Expected status code: 200
    // Expected behaviour: return list of recommended users
    // Expected output: { recommendedUsersList: [mockedUsers[2]] }
    test('Succesfully get recommendations for user with "Partner" as learning preference', async () => {
        const response = await request(app).get(`/recommendations/${mockedUsers[1]._id}`);

        expect(response.status).toBe(200);

        expect(response.body.recommendedUsersList.length).toEqual(1);
        expect(response.body.recommendedUsersList[0]._id).toEqual(mockedUsers[2]._id.toString());
    });

    // Inupt: valid userId, for user with "Both" as learning preference - mockedUsers[2]
    // Expected status code: 200
    // Expected behaviour: return list of recommended users
    // Expected output: { recommendedUsersList: [mockedUsers[1]] }
    test('Succesfully get recommendations for user with "Both" as learning preference', async () => {
        const response = await request(app).get(`/recommendations/${mockedUsers[2]._id}`);

        expect(response.status).toBe(200);

        expect(response.body.recommendedUsersList.length).toEqual(1);
        expect(response.body.recommendedUsersList[0]._id).toEqual(mockedUsers[1]._id.toString());
    });

    // Inupt: valid userId, for user who was liked by mockedUsers[0], and its ideal user is closer to mockedUsers[5] than mockedUsers[4]
    // Expected status code: 200
    // Expected behaviour: return list of recommended users ordered first by if user liked user, then by similarity
    // Expected output: { recommendedUsersList: [mockedUsers[0], mockedUsers[5], mockedUsers[4]] }, List must be in this order
    test('Succesfully get recommendations for expert user testing recommendation algorithm', async () => {
        const response = await request(app).get(`/recommendations/${mockedUsers[3]._id}`);

        expect(response.status).toBe(200);  

        expect(response.body.recommendedUsersList.length).toEqual(3);

        expect(response.body.recommendedUsersList[0]._id).toEqual(mockedUsers[0]._id.toString());
        expect(response.body.recommendedUsersList[1]._id).toEqual(mockedUsers[5]._id.toString());
        expect(response.body.recommendedUsersList[2]._id).toEqual(mockedUsers[4]._id.toString());
    });

    // Inupt: valid userId, for user who will have no recommendations due to not matching its languages selections with other users - mockedUsers[6]
    // Expected status code: 200
    // Expected behaviour: return empty list
    // Expected output: { recommendedUsersList: [] }
    test('Succesfully get recommendations for user that should have no recommendations', async () => {
        const response = await request(app).get(`/recommendations/${mockedUsers[6]._id}`);

        expect(response.status).toBe(200);  
        expect(response.body.recommendedUsersList.length).toEqual(0);
    });

    // Inupt: userId that does not exist in db
    // Expected status code: 400
    // Expected behaviour: return error message
    // Expected output: { error: 'User not found' }
    test('Get recommendation with non existent userId', async () => {
        const response = await request(app).get(`/recommendations/123`);

        expect(response.status).toBe(400);
        expect(response.body.error).toEqual('User not found');
    });

    // Inupt: userId that will throw error when searching for user in db, this is hardcoded in our mock
    // Expected status code: 500
    // Expected behaviour: return error message
    // Expected output: { error: 'User not found' }
    test('Get recommendation and get db error', async () => {
        const response = await request(app).get(`/recommendations/errorId`);

        expect(response.status).toBe(500);  
        expect(response.body.error).toEqual("User not found");
    });
});




