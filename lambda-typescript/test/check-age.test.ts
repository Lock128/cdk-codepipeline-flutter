import * as CheckAge from '../lib/check-age';


test('CheckAgeInGermany', () => {
    const input = {
        "queryStringParameters": {
            "Country": "Germany"
        }
    }
    const output = {
            "body": "18",
            "statusCode": 200
    }
    expect(CheckAge.handler(input)).toEqual(output);
});
test('CheckAgeInUSA', () => {
    const input = {
        "queryStringParameters": {
            "Country": "USA"
        }
    }
    const output = {
            "body": "21",
            "statusCode": 200
    }
    expect(CheckAge.handler(input)).toEqual(output);
});

test('CheckAgeInIndia', () => {
    const input = {
        "queryStringParameters": {
            "Country": "India"
        }
    }
    const output = {
            "body": "-1",
            "statusCode": 200
    }
    expect(CheckAge.handler(input)).toEqual(output);
});

test('CheckAgeInSweden', () => {
    const input = {
        "queryStringParameters": {
            "Country": "Sweden"
        }
    }
    const output = {
            "body": "18",
            "statusCode": 200
    }
    expect(CheckAge.handler(input)).toEqual(output);
});