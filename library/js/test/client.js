const assert = require('assert');

const pc = require('..');
const user = require('../src/user');
const authedclient = require('../src/authedclient')

describe('client', function() {
    const clia = pc.connect('http://localhost:8080');
    const clib = pc.connect('http://localhost:8081');

    describe('#ping()', function() {
        it('should return true if server is valid', async function() {
            const res = await clia.ping();
            assert.ok(res);
        });

        it('should return false if server is invalid', async function() {
            const res = await clib.ping();
            assert.ok(!res);
        });
    });

    const cli = clia;
    const seed = Date.now().toString();

    describe('#create_user()', function() {
        it('should return user if user doesn\'t exist', async function() {
            const res = await cli.create_user({
                'sid': seed,
                'name': seed,
                'pass': seed
            });
            assert.ok(res instanceof user);
        });

        it('should return null if user exists', async function() {
            const res = await cli.create_user({
                'sid': seed,
                'name': seed,
                'pass': seed
            });
            assert.ok(res === null);
        })
    });

    describe('#token()', function() {
        it('should return authedclient', async function() {
            const res = await cli.login({
                'sid': seed,
                'pass': seed,
            });
            assert.ok(res instanceof authedclient);
            ncli = res;
        });

        it('should return authedclient', async function() {
            const res = await cli.login({
                'sid': '',
                'pass': '',
            });
            assert.ok(res === null);
        });
    });
});
