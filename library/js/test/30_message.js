
const assert = require('assert');

const pc = require('..');
const user = require('../src/user');
const group = require('../src/group');
const user_relation = require('../src/user_relation');
const group_relation = require('../src/group_relation');

function mkseed() {
    var text = "";
    var possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    for (var i = 0; i < 32; i++)
    text += possible.charAt(Math.floor(Math.random() * possible.length));

    return text;
}

describe('authedclient', function() {
    let cli;
    let acli1, acli2;
    let seed1, seed2;
    let user1, user2;
    let group, seed;

    before(async function() {
        cli = pc.connect('http://localhost:8080');
        seed1 = mkseed();
        seed2 = mkseed();
        seed = mkseed();

        user1 = await cli.create_user({
            'sid': seed1, 'name': seed1, 'pass': seed1
        });

        user2 = await cli.create_user({
            'sid': seed2, 'name': seed2, 'pass': seed2
        });

        acli1 = await cli.login({
            'sid': seed1, 'pass': seed1
        });

        acli2 = await cli.login({
            'sid': seed2, 'pass': seed2
        });

        await acli1.create_user_relation(user1, user2);
        await acli2.create_user_relation(user2, user1);

        group = await acli1.create_group({
            'sid': seed,
            'name': seed
        });

        await acli1.create_group_relation(user1, group);
    })

    describe('#send_user_message()', function() {
        it('should return true if message was sent', async function() {
            const msg = 'test message';
            const res = await acli1.send_user_message(msg, user1, user2);
            assert.ok(res === true);
        });

        it('should return true if message was sent', async function() {
            const msg = 'test message';
            const res = await acli2.send_user_message(msg, user2, user1);
            assert.ok(res === true);
        });

        it('should return true if message wasn\'t sent', async function() {
            const msg = 'test message';
            const res = await acli1.send_user_message(msg, user1, user1);
            assert.ok(res === false);
        });

        it('should return true if message wasn\'t sent', async function() {
            const msg = 'test message';
            const res = await acli2.send_user_message(msg, user2, user2);
            assert.ok(res === false);
        });
    });

    describe('#send_group_message()', function() {
        it('should return true if message was sent', async function() {
            const msg = 'test message';
            const res = await acli1.send_group_message(msg, user1, group);
            assert.ok(res === true);
        });

        it('should return true if message wasn\'t sent', async function() {
            const msg = 'test message';
            const res = await acli2.send_group_message(msg, user2, group);
            assert.ok(res === false);
        });
    });

    describe('#receive_user_message()', function() {
        it('should return messages if message was sent', async function() {
            const res = await acli1.receive_user_message(user1, user2);
            assert.ok(res instanceof Array);
            assert.ok(res.length !== 0);
        });

        it('should return an empty array if message wasn\'t sent', async function() {
            const msg = 'test message';
            const res = await acli1.receive_user_message(user1, user1);
            assert.ok(res instanceof Array);
            assert.ok(res.length === 0);
        });
    });

    describe('#receive_group_message()', function() {
        it('should return messages if message was sent', async function() {
            const res = await acli1.receive_group_message(group);
            assert.ok(res instanceof Array);
            assert.ok(res.length !== 0);
        });

        it('should return an empty array if message wasn\'t sent', async function() {
            const msg = 'test message';
            const res = await acli2.receive_group_message(group);
            assert.ok(res instanceof Array);
            assert.ok(res.length === 0);
        });
    });
});
