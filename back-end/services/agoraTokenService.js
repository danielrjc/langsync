const { RtcTokenBuilder } = require('agora-access-token');

exports.generateToken = (channelName, uid, role, privilegeExpireTime, tokenType) => {
    let token;
    if (tokenType === 'userAccount') {
        token = RtcTokenBuilder.buildTokenWithAccount(process.env.APP_ID, process.env.APP_CERTIFICATE, channelName, uid, role, privilegeExpireTime);
    } else if (tokenType === 'uid') {
        token = RtcTokenBuilder.buildTokenWithUid(process.env.APP_ID, process.env.APP_CERTIFICATE, channelName, uid, role, privilegeExpireTime);
    } else {
        throw new Error('no token type');
    }
    return token;
}

