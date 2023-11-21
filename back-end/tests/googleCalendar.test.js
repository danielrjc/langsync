
const request = require('supertest');
const app = require('../app'); 
const { ObjectId } = require('mongodb');
const {mockedUsers} = require('../models/__mocks__/mockedUsers');


jest.mock('googleapis');
jest.mock('google-auth-library');
jest.mock('../models/user');

describe('POST /events', () => {
    // Input: valid authCode, valid event
    // Expected status code: 200
    // Expected behaviour: return success message
    // Expected output: { success: true, message: 'Event Created' }
    test('Succesfully create event', async () => {

        const response = await request(app).post('/events').send({
            "authCode": "validAuthorizationCode",
            "event": {
                invitedUserId: mockedUsers[1]._id,
                hostUserId: mockedUsers[0]._id,
                startTime: "2020-10-20T16:00:00.000Z",
                durationMinutes: "30"
            }
        })

        expect(response.status).toBe(200);
        expect(response.body).toEqual({ success: true, message: 'Event Created' });
    });

    // Input: valid authCode, invalid event due to invalid user ids
    // Expected status code: 401
    // Expected behaviour: return error message
    // Expected output: { success: false, erro: 'Error finding host or invited user' }
    test('Creating event with invilid userId', async () => {

        const response = await request(app).post('/events').send({
            "authCode": "validAuthorizationCode",
            "event": {
                invitedUserId: mockedUsers[1]._id,
                hostUserId: new ObjectId(111),
                startTime: "2020-10-20T16:00:00.000Z",
                durationMinutes: "30"
            }
        })

        expect(response.status).toBe(401);
        expect(response.body).toEqual({ success: false, error: 'Error finding host or invited user' });
    });

    // Input: invalid authCode, valid event
    // Expected status code: 401
    // Expected behaviour: return error message
    // Expected output: { success: false, erro: 'Invalid authorization code' }
    test('Creating event fails when inserting into calendar', async () => {

        const response = await request(app).post('/events').send({
            "authCode": "invalidAuthorizationCode",
            "event": {
                invitedUserId: mockedUsers[1]._id,
                hostUserId: mockedUsers[0]._id,
                startTime: "2020-10-20T16:00:00.000Z",
                durationMinutes: "30"
            }
        })

        expect(response.status).toBe(401);
        expect(response.body).toEqual({ success: false, error: 'Invalid authorization code' });
    });

    // Input: valid authCode, valid event (But with durationMinutes = 0, this is hardcoded in our mock to throw an error when inserting into calendar)
    // Expected status code: 401
    // Expected behaviour: return error message
    // Expected output: { success: false, erro: 'Invalid authorization code' }
    test('Creating event fails when inserting into calendar', async () => {

        const response = await request(app).post('/events').send({
            "authCode": "validAuthorizationCode",
            "event": {
                invitedUserId: mockedUsers[1]._id,
                hostUserId: mockedUsers[0]._id,
                startTime: "2020-10-20T16:00:00.000Z",
                durationMinutes: "0"
            }
        })

        expect(response.status).toBe(401);
        expect(response.body).toEqual({ success: false, error: 'Error creating event' });
    });
});