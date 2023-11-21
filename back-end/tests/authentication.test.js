const request = require('supertest');
const app = require('../app'); 
const { mockedUsers, unregisteredUser, unregisteredAdmin } = require('../models/__mocks__/mockedUsers');
const {User} = require('../models/user');

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
        expect(User.findOne({email: unregisteredAdmin.email})).toBeNull();

        const response = await request(app).post('/authentication/admin-login').send({
            accessCode: '1234',
            email:unregisteredAdmin.email
        });

        expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({success: true});

        expect(User.findOne({email: unregisteredAdmin.email})).not.toBeNull();
    });

    test('Banned user', async () => {
        const response = await request(app).post('/authentication/admin-login').send({
            accessCode: '1234',
            email:mockedUsers[7].email
        });
        expect(response.statusCode).toBe(401);
        expect(response.body).toEqual({success: false, "error": "User banned"});
    });
});

describe('POST /authentication/signup', () => {
    test('Invalid Token', async () => {
        const response = await request(app).post('/authentication/signup').send({idToken: 'invalidToken'});
        expect(response.statusCode).toBe(401);
        expect(response.body).toEqual({success: false, error: 'Token verification failed'});
    });

    test('Unregistered user', async () => {
        expect(User.findOne({email: unregisteredUser.email})).toBeNull();

        const response = await request(app).post('/authentication/signup').send({idToken: 'validTokenUnregisteredUser'});
        expect(response.statusCode).toBe(200);
        expect(response.body).toEqual({success: true});

        expect(User.findOne({email: unregisteredUser.email})).not.toBeNull();
    });

    test('Registered user', async () => {
        const response = await request(app).post('/authentication/signup').send({idToken: 'validTokenRegisteredMockUser0'});
        expect(response.statusCode).toBe(401);
        expect(response.body).toEqual({success: false, "error": "User already registered"});
    });
});