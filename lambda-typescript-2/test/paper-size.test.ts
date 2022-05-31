import * as PaperSize from '../lib/paper-size';


test('PaperSizeInGermany', () => {
    expect.assertions(1);
    expect(PaperSize.handler({Country: "Germany"})).resolves.toEqual("A4");
    });

    test('PaperSizeInUSA', () => {
        expect.assertions(1);
        expect(PaperSize.handler({Country: "USA"})).resolves.toEqual("Letter");
        });

        test('PaperSizeInIndia', () => {
            expect.assertions(1);
            expect(PaperSize.handler({Country: "India"})).resolves.toEqual("Unknown");
            });