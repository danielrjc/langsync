const request = require('supertest');
const app = require('../app'); 
const mockedUsers = require('../models/__mocks__/mockedUsers');

jest.mock('googleapis');
jest.mock('google-auth-library');
jest.mock('../models/user');


describe('POST /authentication/login', () => {
    test('Invalid Token', async () => {
        const response = await request(app).post('/authentication/login').send({idToken: 'invalidToken'});
        expect(response.statusCode).toBe(401);
        expect(response.body).toEqual({success: false, error: 'Token verification failed'});
    });

    test('Unregistered user', async () => {
        const response = await request(app).post('/authentication/login').send({idToken: 'validTokenUnregisteredUser'});
        expect(response.statusCode).toBe(401);
        expect(response.body).toEqual({success: false, error: 'User not registered'});
    });

    test('Registered user', async () => {
        const response = await request(app).post('/authentication/login').send({idToken: 'validTokenRegisteredMockUser0'});
        expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({success: true, userId: mockedUsers[0]._id.toString()});
    });
});

describe('POST /authentication/admin-login', () => {
    test('Invalid access token', async () => {
        const response = await request(app).post('/authentication/admin-login').send({
            accessCode: 'invalidToken',
            email:'email'
        });
        expect(response.statusCode).toBe(401);
        expect(response.body).toEqual({success: false, error: 'Invalid access code'});
    });

    test('Unregistered user', async () => {
        const response = await request(app).post('/authentication/admin-login').send({
            accessCode: '1234',
            email:'newuser@admin.com'
        });

        expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({success: true});
    });

    // test('Registered user', async () => {
    //     const response = await request(app).post('/authentication/admin-login').send({
    //         accessCode: 'invalidToken',
    //         email:'email'
    //     });
    //     expect(response.statusCode).toBe(200);
    //     expect(response.body).toEqual({success: true, userId: mockedUsers[0]._id.toString()});
    // });
});