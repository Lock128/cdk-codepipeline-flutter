import * as PaperSize from '../lib/paper-size';


test('PaperSizeInGermany', () => {
    const input = {
        "queryStringParameters": {
            "Country": "Germany"
        }
    }
    const output = {
            "body": "A4",
            "statusCode": 200
    }
    expect(PaperSize.handler(input)).resolves.toEqual(output);
});

test('PaperSizeInUSA', () => {
    const input = {
        "queryStringParameters": {
            "Country": "USA"
        }
    }
    const output = {
            "body": "Letter",
            "statusCode": 200
    }
    expect(PaperSize.handler(input)).resolves.toEqual(output);
});

test('PaperSizeInIndia', () => {

    const input = {
        "queryStringParameters": {
            "Country": "India"
        }
    }
    const output = {
            "body": "Unknown",
            "statusCode": 200
    }
    expect(PaperSize.handler(input)).resolves.toEqual(output);
});