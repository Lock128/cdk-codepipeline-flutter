import * as CheckAge from '../lib/check-age';


test('CheckAgeInGermany', () => {
    expect.assertions(1);
    expect(CheckAge.handler('{"queryStringParameters": {"Country": "Germany"}}')).resolves.toEqual(18);
});
test('CheckAgeInUSA', () => {
    expect.assertions(1);
    expect(CheckAge.handler("USA")).resolves.toEqual(21);
});

test('CheckAgeInIndia', () => {
    expect.assertions(1);
    expect(CheckAge.handler("India")).resolves.toEqual(18);
});