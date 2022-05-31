import * as CheckAge from '../lib/check-age';


test('CheckAgeInGermany', () => {
    expect.assertions(1);
    expect(CheckAge.handler({Country: "Germany"})).resolves.toEqual(18);
    });

    test('CheckAgeInUSA', () => {
        expect.assertions(1);
        expect(CheckAge.handler({Country: "USA"})).resolves.toEqual(21);
        });

        test('CheckAgeInIndia', () => {
            expect.assertions(1);
            expect(CheckAge.handler({Country: "India"})).resolves.toEqual(18);
            });